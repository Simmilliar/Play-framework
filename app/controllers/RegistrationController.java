package controllers;

import controllers.actions.AuthorizationCheckAction;
import controllers.utils.MailerService;
import controllers.utils.SessionsManager;
import controllers.utils.Utils;
import io.ebean.Ebean;
import models.data.Users;
import models.forms.RegistrationForm;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.libs.mailer.MailerClient;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import tyrex.services.UUID;

import javax.inject.Inject;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@With(AuthorizationCheckAction.class)
public class RegistrationController extends Controller
{
	private final FormFactory formFactory;
	private final MailerClient mailerClient;
	private final Utils utils;
	private final SessionsManager sessionsManager;

	@Inject
	public RegistrationController(FormFactory formFactory, MailerClient mailerClient, Utils utils, SessionsManager sessionsManager)
	{
		this.formFactory = formFactory;
		this.mailerClient = mailerClient;
		this.utils = utils;
		this.sessionsManager = sessionsManager;
	}

	public Result registration()
	{
		return ok(views.html.registration.render(formFactory.form(RegistrationForm.class)));
	}

	public Result register()
	{
		Form<RegistrationForm> registrationForm = formFactory.form(RegistrationForm.class).bindFromRequest();
		if (registrationForm.hasErrors())
		{
			return badRequest(views.html.registration.render(registrationForm));
		}

		RegistrationForm registrationData = registrationForm.get();

		Users user = Ebean.find(Users.class)
				.where()
				.eq("email", registrationData.getEmail())
				.findOne();
		if (user != null && user.isConfirmed())
		{
			registrationData.addError(new ValidationError("email", "This e-mail is already registered."));
		}

		if (registrationForm.hasErrors())
		{
			return badRequest(views.html.registration.render(registrationForm));
		}
		else
		{
			if (user == null)
			{
				user = new Users();
			}
			user.setName(registrationData.getName());
			user.setEmail(registrationData.getEmail());
			user.setAvatarUrl(routes.Assets.versioned(
					new Assets.Asset(Utils.DEFAULT_AVATAR_ASSET)
			).url()); // solved todo it's better, but please, use java constants
			user.setFacebookId(0);

			user.setPasswordSalt("" + ThreadLocalRandom.current().nextLong());
			user.setPasswordHash(utils.hashString(registrationData.getPassword(), user.getPasswordSalt()));

			user.setConfirmed(false);
			String confirmationKey = UUID.create();
			user.setConfirmationKeyHash(utils.hashString(confirmationKey, confirmationKey));
			user.setConfirmationKeyExpirationDate(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));

			try
			{
				String confirmationBodyText = String.format(Utils.EMAIL_CONFIRMATION,
						routes.RegistrationController.confirmEmail(confirmationKey).absoluteURL(request()));
				new MailerService(mailerClient)
						.sendEmail(user.getEmail(), "Registration confirmation.", confirmationBodyText);
				flash().put("notification", "We'll send you an e-mail to confirm your registration.");
			}
			catch (Exception e)
			{
				registrationData.addError(new ValidationError("email", "Unable to send confirmation email."));
				return internalServerError(views.html.registration.render(registrationForm));
			}

 			user.save();

			return redirect(routes.HomeController.index());
		}
	}

	public Result confirmEmail(String key)
	{
		Users user = Ebean.find(Users.class)
				.where()
				.and()
				.eq("confirmation_key_hash", utils.hashString(key, key))
				.eq("confirmed", false)
				.gt("confirmation_key_expiration_date", System.currentTimeMillis())
				.endAnd()
				.findOne();
		if (user != null && request().header("User-Agent").isPresent())
		{
			user.setConfirmed(true);
			user.setConfirmationKeyHash("");
			user.setConfirmationKeyExpirationDate(System.currentTimeMillis());
			user.save();

			flash().put("notification", "You were successfully registered!");

			String sessionToken = sessionsManager.registerSession(
					sessionsManager.AUTH_TYPE_PASSWORD,
					request().header("User-Agent").get(), user.getUserId()
			);
			response().setCookie(
					Http.Cookie.builder("session_token", sessionToken)
					.withMaxAge(Duration.ofMillis(sessionsManager.TOKEN_LIFETIME))
					.build()
			);
		}
		return redirect(routes.HomeController.index());
	}
}
package controllers;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
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
	private final Config config = ConfigFactory.load();

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
		RegistrationForm registrationData = registrationForm.get();

		// solved todo here has to be checking if user exists
		if (Ebean.find(Users.class)
				.where()
				.eq("email", registrationData.email)
				.eq("confirmed", true)
				.findOne() != null)
		{
			registrationData.errors.add(new ValidationError("email", "This e-mail is already registered."));
		}

		if (registrationForm.hasErrors())
		{
			return badRequest(views.html.registration.render(registrationForm));
		}
		else
		{
			Users user = Ebean.find(Users.class, registrationData.email);
			if (user == null)
			{
				user = new Users();
			}
			user.name = registrationData.name;
			user.email = registrationData.email;

			user.passwordSalt = "" + ThreadLocalRandom.current().nextInt();
			user.passwordHash = utils.hashString(registrationData.password, user.passwordSalt);

			String confirmationKey = System.currentTimeMillis() + "" + ThreadLocalRandom.current().nextInt();
			user.confirmationKeyHash = utils.hashString(confirmationKey, confirmationKey);
			user.confirmationKeyExpirationDate = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1);

			user.avatarUrl = routes.Assets.versioned(
					new Assets.Asset(config.getString("DEFAULT_AVATAR_ASSET"))
			).url(); // solved todo move to some constant

			user.confirmed = false;
			try
			{
				String confirmationBodyText = String.format(config.getString("EMAIL_CONFIRMATION"),
						routes.RegistrationController.confirmEmail(confirmationKey).absoluteURL(request()));
				new MailerService(mailerClient)
						.sendEmail(user.email, "Registration confirmation.", confirmationBodyText);
				// solved todo move to where you actually send notification
				flash().put("notification", "We'll send you an e-mail to confirm your registration.");
			}
			catch (Exception e)
			{
				e.printStackTrace();
				registrationData.errors.add(new ValidationError("email", "Unable to send confirmation email."));
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
			user.confirmed = true;
			user.confirmationKeyHash = "";
			user.confirmationKeyExpirationDate = System.currentTimeMillis();
			user.save();

			flash().put("notification", "You were successfully registered!");

			String sessionToken = sessionsManager.registerSession(
					request().header("User-Agent").get(), user.email
			);
			response().setCookie(
					Http.Cookie.builder("session_token", sessionToken)
					.withMaxAge(Duration.ofSeconds(sessionsManager.TOKEN_LIFETIME))
					.build()
			);
		}
		return redirect(routes.HomeController.index());
	}
}
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
import play.libs.mailer.MailerClient;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;

import javax.inject.Inject;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@With(AuthorizationCheckAction.class)
public class RegistrationController extends Controller
{
	final private FormFactory formFactory;
	final private MailerClient mailerClient;

	@Inject
	public RegistrationController(FormFactory formFactory, MailerClient mailerClient)
	{
		this.formFactory = formFactory;
		this.mailerClient = mailerClient;
	}

	public Result registration()
	{
		return ok(views.html.registration.render(formFactory.form(RegistrationForm.class)));
	}

	public Result register()
	{
		Form<RegistrationForm> form = formFactory.form(RegistrationForm.class).bindFromRequest();
		if (form.hasErrors())
		{
			return badRequest(views.html.registration.render(form));
		}
		else
		{
			RegistrationForm rf = form.get();
			Users user = Ebean.find(Users.class, rf.email);
			if (user == null)
				user = new Users();
			user.name = rf.name;
			user.email = rf.email;
			user.passwordSalt = "" + ThreadLocalRandom.current().nextInt();
			user.passwordHash = Utils.hashString(
					new StringBuilder(rf.password)
							.insert(rf.password.length() / 2, user.passwordSalt)
							.toString()
			); // solved todo it's a good practice to have hash from salt + password
			user.confirmationKey = Utils.hashString(user.email + System.currentTimeMillis());
			user.avatarUrl = routes.Assets.versioned(new Assets.Asset("images/default_avatar.jpg")).url(); // solved todo move to some constant

			user.confirmed = false;
			try
			{
				String confirmationBodyText = String.format(Utils.EMAIL_CONFIRMATION, request().host(), user.confirmationKey);
				new MailerService(mailerClient)
						.sendEmail(user.email, "Registration confirmation.", confirmationBodyText);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return internalServerError(views.html.registration.render(form));
			}

			// solved todo add to db rule to override existing record

			user.save();

			// solved todo move to where you actually send notification
			flash().put("notification", "We'll send you an e-mail to confirm your registration.");

			return redirect(routes.HomeController.index());
		}
	}

	public Result confirmEmail(String key)
	{
		Users user = Ebean.find(Users.class).where()
				.eq("confirmation_key", key)
				.eq("confirmed", false)
				.findOne();
		if (user != null && request().header("User-Agent").isPresent())
		{
			user.confirmed = true;
			user.save();
			flash().put("notification", "You were successfully registered!");
			String sessionToken = SessionsManager.registerSession(
					request().header("User-Agent").get(), user.email);
			response().setCookie(Http.Cookie.builder("session_token", sessionToken)
					.withMaxAge(Duration.ofSeconds(SessionsManager.TOKEN_LIFETIME))
					.withPath("/")
					.withSecure(false)
					.withHttpOnly(true)
					.withSameSite(Http.Cookie.SameSite.STRICT)
					.build()
			);
		}
		return redirect(routes.HomeController.index());
	}
}
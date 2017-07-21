package controllers;

import com.typesafe.config.ConfigFactory;
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

import javax.inject.Inject;
import java.time.Duration;

import static controllers.utils.SessionsManager.userAuthorized;

public class RegistrationController extends Controller
{
	final private FormFactory formFactory;
	final private MailerClient mailerClient;
	private final Utils utils;

	@Inject
	public RegistrationController(FormFactory formFactory, MailerClient mailerClient, Utils utils)
	{
		this.formFactory = formFactory;
		this.mailerClient = mailerClient;
		this.utils = utils;
	}

	public Result registration()
	{
		if (userAuthorized(request()))
		{
			return redirect(routes.HomeController.index());
		}
		else
		{
			return ok(views.html.registration.render(formFactory.form(RegistrationForm.class)));
		}
	}

	public Result register()
	{
		if (!userAuthorized(request()))
		{
			Form<RegistrationForm> form = formFactory.form(RegistrationForm.class).bindFromRequest();
			if (form.hasErrors())
			{
				return badRequest(views.html.registration.render(form));
			}
			else
			{
				Users user = new Users();
				user.name = form.get().name;
				user.email = form.get().email;
				user.passwordHash = Utils.hashString(form.get().password); // todo it's a good practice to have hash from salt + password
				user.confirmationKey = Utils.hashString(user.email + System.currentTimeMillis());
				user.avatarUrl = "https://lelakisihat.com/wp-content/uploads/2016/09/avatar.jpg"; // todo move to some constant

				if (ConfigFactory.load().getBoolean("EMAIL_CONFIRMATION_REQUIRED"))
				{
					user.confirmed = false;
					try
					{
						String confirmationBodyText = String.format(Utils.EMAIL_CONFIRMATION, user.confirmationKey);
						new MailerService(mailerClient)
								.sendEmail(user.email, "Registration confirmation.", confirmationBodyText);
					}
					catch (Exception e)
					{
						e.printStackTrace();
						return internalServerError(views.html.registration.render(form));
					}
				}
				else
				{
					user.confirmed = true;
				}

				// todo add to db rule to override existing record
				Users oldUser = Ebean.find(Users.class, user.email);
				if (oldUser != null)
				{
					Ebean.delete(oldUser);
				}

				user.save();

				utils.setNotification(response(), "We'll send you an e-mail to confirm your registration.");
			}
		}
		return redirect(routes.HomeController.index());
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
			utils.setNotification(response(), "You were successfully registered!");
			String sessionToken = SessionsManager.registerSession(
					request().header("User-Agent").get(), user.email);
			response().setCookie(Http.Cookie.builder("session_token", sessionToken)
					.withMaxAge(Duration.ofSeconds(SessionsManager.TOKEN_LIFETIME))
					.withPath("/")
					.withDomain(ConfigFactory.load().getString("COOKIE_DOMAIN"))
					.withSecure(false)
					.withHttpOnly(true)
					.withSameSite(Http.Cookie.SameSite.STRICT)
					.build()
			);
		}
		return redirect(routes.HomeController.index());
	}
}
package controllers;

import controllers.utils.MailerService;
import controllers.utils.SessionsManager;
import controllers.utils.Utils;
import io.ebean.Ebean;
import models.data.User;
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

	@Inject
	public RegistrationController(FormFactory formFactory, MailerClient mailerClient)
	{
		this.formFactory = formFactory;
		this.mailerClient = mailerClient;
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
				User user = new User();
				user.name = form.get().name;
				user.email = form.get().email;
				user.confirmed = false;
				user.passwordHash = Utils.hashString(form.get().password);
				user.confirmationKey = Utils.hashString(user.email + System.currentTimeMillis());

				try
				{
					String confirmationBodyText = String.format(Utils.EMAIL_CONFIRMATION, user.confirmationKey);
					new MailerService(mailerClient)
							.sendEmail(user.email, "Registration confirmation.", confirmationBodyText);
				}
				catch (Exception e)
				{
					return internalServerError(views.html.registration.render(form));
				}

				User oldUser = Ebean.find(User.class, user.email);
				if (oldUser != null)
				{
					Ebean.delete(oldUser);
				}

				user.save();
			}
		}
		return redirect(routes.HomeController.index());
	}

	public Result confirmEmail(String key)
	{
		User user = Ebean.find(User.class).where()
				.eq("confirmation_key", key)
				.eq("confirmed", false)
				.findOne();
		if (user != null)
		{
			user.confirmed = true;
			user.save();
			String sessionToken = SessionsManager.registerSession(
					request().getHeader("User-Agent"), user.email);
			response().setCookie(Http.Cookie.builder("session_token", sessionToken)
					.withMaxAge(Duration.ofSeconds(SessionsManager.TOKEN_LIFETIME))
					.withPath("/")
					.withDomain("localhost")
					.withSecure(false)
					.withHttpOnly(true)
					.withSameSite(Http.Cookie.SameSite.STRICT)
					.build()
			);
		}
		return redirect(routes.HomeController.index());
	}
}
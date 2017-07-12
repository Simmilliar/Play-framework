package controllers;

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

public class RegistrationController extends Controller {

	final private FormFactory formFactory;
	final private MailerClient mailerClient;

	@Inject
	public RegistrationController(FormFactory formFactory, MailerClient mailerClient) {
		this.formFactory = formFactory;
		this.mailerClient = mailerClient;
	}

	public Result registration() {
		if (request().cookies().get("session_token") != null &&
				SessionsManager.checkSession(request().cookies().get("session_token").value())) {
			return redirect(routes.HomeController.index());
		} else {
			return ok(views.html.registration.render(formFactory.form(RegistrationForm.class)));
		}
	}

	public Result register() {
		if (request().cookies().get("session_token") == null ||
				!SessionsManager.checkSession(request().cookies().get("session_token").value())) {
			Form<RegistrationForm> registrationForm = formFactory.form(RegistrationForm.class).bindFromRequest();
			if (registrationForm.hasErrors()) {
				return badRequest(views.html.registration.render(registrationForm));
			} else {
				User user = new User();
				user.name = registrationForm.get().name;
				user.email = registrationForm.get().email;
				user.confirmed = false;
				user.passwordHash = Utils.hashString(registrationForm.get().password);
				user.confirmationKey = Utils.hashString(user.passwordHash);
				user.save();

				String confirmationBodyText = "To complete your registration you need to confirm your e-mail address " +
						"by following this link: http://localhost:9000/emailconfirm?key=" + user.confirmationKey;
				new MailerService(mailerClient).sendEmail(user.email, "Email confirmation", confirmationBodyText);
			}
		}
		return redirect(routes.HomeController.index());
	}

	public Result confirmEmail(String key) {
		User user = Ebean.find(User.class).where().eq("confirmation_key", key).findOne();
		if (user != null && !user.confirmed) {
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
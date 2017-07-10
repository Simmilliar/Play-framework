package controllers;

import models.forms.RegistrationForm;
import models.data.User;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

public class RegistrationController extends Controller {

	final private FormFactory formFactory;

	@Inject
	public RegistrationController(FormFactory formFactory) {
		this.formFactory = formFactory;
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
				user.passwordHash = Utils.hashString(registrationForm.get().password);
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
		}
		return redirect(routes.HomeController.index());
	}
}
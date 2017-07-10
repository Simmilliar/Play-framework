package controllers;

import models.forms.AuthorizationForm;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.time.Duration;

public class AuthorizationController extends Controller {

	private final FormFactory formFactory;

	@Inject
	public AuthorizationController(FormFactory formFactory) {
		this.formFactory = formFactory;
	}

	public Result authorization() {
		if (request().cookies().get("session_token") != null &&
				SessionsManager.checkSession(request().cookies().get("session_token").value())) {
			return redirect(routes.HomeController.index());
		} else {
			return ok(views.html.authorization.render(formFactory.form(AuthorizationForm.class)));
		}
	}

	public Result authorize() {
		if (request().cookies().get("session_token") == null ||
				!SessionsManager.checkSession(request().cookies().get("session_token").value())) {
			Form<AuthorizationForm> loginForm = formFactory.form(AuthorizationForm.class).bindFromRequest();
			if (loginForm.hasErrors()) {
				return badRequest(views.html.authorization.render(loginForm));
			} else {
				String sessionToken = SessionsManager.registerSession(
						request().getHeader("User-Agent"), loginForm.get().email);
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

	public Result logout() {
		if (request().cookies().get("session_token") != null &&
				SessionsManager.checkSession(request().cookies().get("session_token").value())) {
			SessionsManager.unregisterSession(request().cookies().get("session_token").value());
			response().discardCookie("session_token");
		}
		return redirect(routes.HomeController.index());
	}
}
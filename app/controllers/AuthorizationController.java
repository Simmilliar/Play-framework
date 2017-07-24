package controllers;

import controllers.actions.AuthorizationCheckAction;
import controllers.utils.SessionsManager;
import models.forms.AuthorizationForm;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;

import javax.inject.Inject;
import java.time.Duration;

@With(AuthorizationCheckAction.class)
public class AuthorizationController extends Controller
{
	private final FormFactory formFactory;

	@Inject
	public AuthorizationController(FormFactory formFactory)
	{
		this.formFactory = formFactory;
	}

	public Result authorization()
	{
		return ok(views.html.authorization.render(formFactory.form(AuthorizationForm.class)));
	}

	public Result authorize()
	{
		Form<AuthorizationForm> form = formFactory.form(AuthorizationForm.class).bindFromRequest();
		if (form.hasErrors())
		{
			return badRequest(views.html.authorization.render(form));
		}
		else if (request().header("User-Agent").isPresent())
		{
			String sessionToken = SessionsManager.registerSession(
					request().header("User-Agent").get(), form.get().email);
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

	public Result logout()
	{
		SessionsManager.unregisterSession(request().cookies().get("session_token").value());
		response().discardCookie("session_token");
		return redirect(routes.HomeController.index());
	}
}
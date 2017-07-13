package controllers;

import controllers.utils.SessionsManager;
import models.forms.AuthorizationForm;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.time.Duration;

import static controllers.utils.SessionsManager.userAuthorized;

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
		if (userAuthorized(request()))
		{
			return redirect(routes.HomeController.index());
		}
		else
		{
			return ok(views.html.authorization.render(formFactory.form(AuthorizationForm.class)));
		}
	}

	public Result authorize()
	{
		if (!userAuthorized(request()))
		{
			Form<AuthorizationForm> form = formFactory.form(AuthorizationForm.class).bindFromRequest();
			if (form.hasErrors())
			{
				return badRequest(views.html.authorization.render(form));
			}
			else
			{
				String sessionToken = SessionsManager.registerSession(
						request().getHeader("User-Agent"), form.get().email);
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

	public Result logout()
	{
		if (userAuthorized(request()))
		{
			SessionsManager.unregisterSession(request().cookies().get("session_token").value());
			response().discardCookie("session_token");
		}
		return redirect(routes.HomeController.index());
	}
}
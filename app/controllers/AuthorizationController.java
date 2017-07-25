package controllers;

import controllers.actions.AuthorizationCheckAction;
import controllers.utils.SessionsManager;
import controllers.utils.Utils;
import io.ebean.Ebean;
import models.data.Users;
import models.forms.AuthorizationForm;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
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
	private final Utils utils;
	private final SessionsManager sessionsManager;

	@Inject
	public AuthorizationController(FormFactory formFactory, Utils utils, SessionsManager sessionsManager)
	{
		this.formFactory = formFactory;
		this.utils = utils;
		this.sessionsManager = sessionsManager;
	}

	public Result authorization()
	{
		return ok(views.html.authorization.render(formFactory.form(AuthorizationForm.class)));
	}

	public Result authorize()
	{
		Form<AuthorizationForm> authorizationForm = formFactory.form(AuthorizationForm.class).bindFromRequest();
		AuthorizationForm authorizationData = authorizationForm.get();

		Users foundedUser = Ebean.find(Users.class, authorizationData.email);
		if (foundedUser == null || !foundedUser.confirmed)
		{
			authorizationData.errors.add(new ValidationError("email", "Unregistered user."));
		}
		else
		{
			String hash = utils.hashString(authorizationData.password, foundedUser.passwordSalt);
			if (!foundedUser.passwordHash.equals(hash))
			{
				authorizationData.errors.add(new ValidationError("password", "Wrong password."));
			}
		}

		if (authorizationForm.hasErrors())
		{
			return badRequest(views.html.authorization.render(authorizationForm));
		}
		else if (request().header("User-Agent").isPresent())
		{
			String sessionToken = sessionsManager.registerSession(
					request().header("User-Agent").get(), authorizationData.email
			);
			response().setCookie(
					Http.Cookie.builder("session_token", sessionToken)
					.withMaxAge(Duration.ofMillis(sessionsManager.TOKEN_LIFETIME))
					.build()
			);
		}
		return redirect(routes.HomeController.index());
	}

	public Result logout()
	{
		sessionsManager.unregisterSession(request().cookies().get("session_token").value());
		response().discardCookie("session_token");
		return redirect(routes.HomeController.index());
	}
}
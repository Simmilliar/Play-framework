package controllers;

import controllers.actions.AuthorizationCheckAction;
import controllers.repositories.UsersRepository;
import controllers.utils.SessionsManager;
import controllers.utils.Utils;
import models.Users;
import play.data.DynamicForm;
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
	private final Utils utils;
	private final SessionsManager sessionsManager;
	private final UsersRepository usersRepository;

	@Inject
	public AuthorizationController(FormFactory formFactory, Utils utils, SessionsManager sessionsManager,
								   UsersRepository usersRepository)
	{
		this.formFactory = formFactory;
		this.utils = utils;
		this.sessionsManager = sessionsManager;
		this.usersRepository = usersRepository;
	}

	public Result authorization()
	{
		return ok(views.html.authorization.render(formFactory.form()));
	}

	public Result authorize()
	{
		DynamicForm authorizationForm = formFactory.form().bindFromRequest();

		String email = authorizationForm.get("email");
		String password = authorizationForm.get("password");

		Users user = null;

		//SECTION BEGIN: Checking
		if (email == null || password == null)
		{
			return badRequest(views.html.authorization.render(authorizationForm.withError("", "Missing fields.")));
		}
		if (!utils.isEmailValid(email))
		{
			return badRequest(views.html.authorization.render(authorizationForm.withError("email", "Invalid e-mail address.")));
		}
		user = usersRepository.findByEmail(email);
		if (user == null || !user.isConfirmed())
		{
			return badRequest(views.html.authorization.render(authorizationForm.withError("email", "Unregistered user.")));
		}
		String hash = utils.hashString(password, user.getPasswordSalt());
		if (!user.getPasswordHash().equals(hash))
		{
			return badRequest(views.html.authorization.render(authorizationForm.withError("password", "Wrong password.")));
		}
		//SECTION END: Checking

		String sessionToken = sessionsManager.registerSession(sessionsManager.AUTH_TYPE_PASSWORD, user.getUserId());
		response().setCookie(Http.Cookie.builder("session_token", sessionToken)
				.withMaxAge(Duration.ofMillis(sessionsManager.TOKEN_LIFETIME)).build());
		return redirect(routes.HomeController.index());
	}

	public Result logout()
	{
		sessionsManager.unregisterSession(request().cookies().get("session_token").value());
		response().discardCookie("session_token");
		return redirect(routes.HomeController.index());
	}
}
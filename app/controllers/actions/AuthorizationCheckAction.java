package controllers.actions;

import controllers.SessionRepository;
import controllers.routes;
import models.Session;
import models.Users;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class AuthorizationCheckAction extends play.mvc.Action.Simple
{
	private final SessionRepository sessionRepository;

	private final List<String> unauthorizedOnly = Arrays.asList("/registration", "/emailconfirm", "/login",
			"/facebook_auth", "/twitter_auth", "/twitter_auth/continue", "/forgotpassword", "/changepassword");
	private final List<String> forAll = Arrays.asList("/");

	@Inject
	public AuthorizationCheckAction(SessionRepository sessionRepository)
	{
		this.sessionRepository = sessionRepository;
	}

	public CompletionStage<Result> call(Http.Context ctx)
	{
		Users user = null;

		Http.Cookie cookie = ctx.request().cookies().get("session_token");
		if (cookie != null)
		{
			Session session = sessionRepository.findByToken(cookie.value());
			if (session != null && session.getExpirationDate() > System.currentTimeMillis())
			{
				user = session.getUser();
			}
		}

		ctx.args.put("user", user);
		if (forAll.contains(ctx.request().path()))
		{
			return delegate.call(ctx);
		}
		else if ((user == null) == (unauthorizedOnly.contains(ctx.request().path())))
		{
			return delegate.call(ctx);
		}
		else
		{
			return CompletableFuture.completedFuture(redirect(routes.HomeController.index()));
		}
	}
}
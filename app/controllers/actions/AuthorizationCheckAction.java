package controllers.actions;

import controllers.routes;
import io.ebean.Ebean;
import models.data.Session;
import models.data.Users;
import play.libs.typedmap.TypedKey;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class AuthorizationCheckAction extends play.mvc.Action.Simple
{
	public static final TypedKey<Users> USER = TypedKey.create("user");

	private final List<String> unauthorizedOnly = Arrays.asList(
			"/registration",
			"/emailconfirm",
			"/login",
			"/forgotpassword",
			"/changepassword"
	);

	private final List<String> forAll = Arrays.asList("/");

	public CompletionStage<Result> call(Http.Context ctx)
	{
		Users user = null;

		Http.Cookie cookie = ctx.request().cookies().get("session_token");
		if (cookie != null)
		{
			Session session = Ebean.find(Session.class, cookie.value());
			if (session != null && session.expirationDate > System.currentTimeMillis())
			{
				user = session.user;
			}
		}

		ctx = ctx.withRequest(ctx.request().addAttr(USER, user));
		if (forAll.contains(ctx.request().path()))
		{
			return delegate.call(ctx);
		}
		else
		{
			if (user != null && unauthorizedOnly.contains(ctx.request().path()))
			{
				return CompletableFuture.completedFuture(redirect(routes.HomeController.index()));
			}
			else
			{
				return delegate.call(ctx);
			}
		}
	}
}
package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.actions.AuthorizationCheckAction;
import controllers.utils.SessionsManager;
import io.ebean.Ebean;
import models.data.Users;
import models.forms.FacebookAuthForm;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;

import javax.inject.Inject;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@With(AuthorizationCheckAction.class)
public class FacebookAuthController extends Controller
{
	private final FormFactory formFactory;
	private final SessionsManager sessionsManager;
	private final WSClient wsClient;
	private final HttpExecutionContext httpExecutionContext;

	@Inject
	public FacebookAuthController(FormFactory formFactory, SessionsManager sessionsManager,
								  WSClient wsClient, HttpExecutionContext httpExecutionContext)
	{
		this.formFactory = formFactory;
		this.sessionsManager = sessionsManager;
		this.wsClient = wsClient;
		this.httpExecutionContext = httpExecutionContext;
	}

	public CompletionStage<Result> facebookAuth()
	{
		Form<FacebookAuthForm> facebookAuthForm = formFactory.form(FacebookAuthForm.class).bindFromRequest();
		if (facebookAuthForm.hasErrors())
		{
			return supplyAsync(() -> badRequest(""));
		}
		else
		{
			WSRequest request = wsClient.url("https://graph.facebook.com/me")
					.setQueryString("fields=email,name,picture{url}&access_token=" +
							facebookAuthForm.get().getAccessToken());

			return request.get().thenApplyAsync(wsr -> redirect(routes.HomeController.index())
					.withCookies(authorization(wsr.asJson())), httpExecutionContext.current()
			);
		}
	}

	private Http.Cookie authorization(JsonNode jsonNode)
	{
		// todo WTF is going on here???
		Users user = Ebean.find(Users.class)
				.where()
				.eq("facebook_id", jsonNode.get("id").asLong())
				.findOne();
		if (user == null)
		{
			if (jsonNode.has("email"))
			{
				user = Ebean.find(Users.class)
						.where()
						.eq("email", jsonNode.get("email").asText())
						.findOne();
			}
			if (user == null)
			{
				user = new Users();

				user.setName(jsonNode.get("name").asText());
				user.setEmail(jsonNode.has("email") ? jsonNode.get("email").asText() : "");
				user.setAvatarUrl(jsonNode.get("picture").get("data").get("url").asText());
				user.setFacebookId(jsonNode.get("id").asLong());
				user.setTwitterId(0);

				user.setPasswordSalt("" + ThreadLocalRandom.current().nextLong());
				user.setPasswordHash("");

				user.setConfirmed(true);
				user.setConfirmationKeyHash("");
				user.setConfirmationKeyExpirationDate(System.currentTimeMillis());

				user.save();

				flash().put("notification", "You were successfully registered!");
			}
		}

		String sessionToken = sessionsManager.registerSession(
				sessionsManager.AUTH_TYPE_FACEBOOK,
				request().header("User-Agent").get(), user.getUserId()
		);
		return Http.Cookie.builder("session_token", sessionToken)
				.withMaxAge(Duration.ofMillis(sessionsManager.TOKEN_LIFETIME))
				.build();
	}
}
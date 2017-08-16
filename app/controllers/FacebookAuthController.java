package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.actions.AuthorizationCheckAction;
import controllers.utils.SessionsManager;
import models.Users;
import play.data.DynamicForm;
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
	private final UsersRepository usersRepository;

	@Inject
	public FacebookAuthController(FormFactory formFactory, SessionsManager sessionsManager,
								  WSClient wsClient, HttpExecutionContext httpExecutionContext,
								  UsersRepository usersRepository)
	{
		this.formFactory = formFactory;
		this.sessionsManager = sessionsManager;
		this.wsClient = wsClient;
		this.httpExecutionContext = httpExecutionContext;
		this.usersRepository = usersRepository;
	}

	public CompletionStage<Result> facebookAuth()
	{
		DynamicForm facebookAuthForm = formFactory.form().bindFromRequest();

		String accessToken = facebookAuthForm.get("accessToken");
		String expiresIn = facebookAuthForm.get("expiresIn");
		String signedRequest = facebookAuthForm.get("signedRequest");
		String userID = facebookAuthForm.get("userID");

		if (accessToken == null || expiresIn == null || signedRequest == null || userID == null)
		{
			return supplyAsync(() -> badRequest(""));
		}
		else
		{
			WSRequest request = wsClient.url("https://graph.facebook.com/me")
					.setQueryString("fields=email,name,picture{url}&access_token=" + accessToken);

			return request.get().thenApplyAsync(wsr -> redirect(routes.HomeController.index())
					.withCookies(authorization(wsr.asJson())), httpExecutionContext.current()
			);
		}
	}

	private Http.Cookie authorization(JsonNode jsonNode)
	{
		Users user = usersRepository.findByFacebookId(jsonNode.get("id").asLong());
		if (user == null)
		{
			if (jsonNode.has("email"))
			{
				user = usersRepository.findByEmail(jsonNode.get("email").asText());
			}
			if (user == null)
			{
				user = new Users();

				user.setName(jsonNode.get("name").asText());
				user.setEmail(jsonNode.has("email") ? jsonNode.get("email").asText() : "");
				user.setAvatarUrl(jsonNode.get("picture").get("data").get("url").asText());
				user.setFacebookId(jsonNode.get("id").asLong());
				user.setTwitterId(-1L * System.currentTimeMillis());

				user.setPasswordSalt("" + ThreadLocalRandom.current().nextLong());
				user.setPasswordHash("");

				user.setConfirmed(true);
				user.setConfirmationKeyHash("");
				user.setConfirmationKeyExpirationDate(System.currentTimeMillis());

				usersRepository.saveUser(user);

				flash().put("notification", "You were successfully registered!");
			}
		}

		String sessionToken = sessionsManager.registerSession(sessionsManager.AUTH_TYPE_FACEBOOK, user.getUserId());
		return Http.Cookie.builder("session_token", sessionToken)
				.withMaxAge(Duration.ofMillis(sessionsManager.TOKEN_LIFETIME)).build();
	}
}
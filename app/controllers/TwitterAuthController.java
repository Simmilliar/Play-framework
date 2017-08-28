package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import controllers.actions.AuthorizationCheckAction;
import controllers.repositories.UsersRepository;
import controllers.utils.SessionsUtils;
import models.Users;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import sun.misc.BASE64Encoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static play.mvc.Controller.*;
import static play.mvc.Results.redirect;

@With(AuthorizationCheckAction.class)
public class TwitterAuthController
{
	private final WSClient wsClient;
	private final Config config = ConfigFactory.load();
	private final HttpExecutionContext httpExecutionContext;
	private final SessionsUtils sessionsUtils;
	private final UsersRepository usersRepository;

	@Inject
	public TwitterAuthController(WSClient wsClient, HttpExecutionContext httpExecutionContext,
								 SessionsUtils sessionsUtils, UsersRepository usersRepository)
	{
		this.wsClient = wsClient;
		this.httpExecutionContext = httpExecutionContext;
		this.sessionsUtils = sessionsUtils;
		this.usersRepository = usersRepository;
	}

	public CompletionStage<Result> twitterAuth()
	{
		Map<String, String> parameters = new TreeMap<>();
		String route = routes.TwitterAuthController.authorization("", "").absoluteURL(request());
		parameters.put("oauth_callback", route.substring(0, route.indexOf('?')));
		return twitterRequest("POST", "https://api.twitter.com/oauth/request_token", "", parameters)
				.post("").thenApplyAsync(
						wsr -> redirect("https://api.twitter.com/oauth/authenticate?" +
								wsr.getBody().substring(0, wsr.getBody().indexOf('&'))),
						httpExecutionContext.current()
				);
	}

	public CompletionStage<Result> authorization(String oauthToken, String oauthVerifier)
	{
		Map<String, String> parameters = new TreeMap<>();
		parameters.put("oauth_token", oauthToken);
		parameters.put("oauth_verifier", oauthVerifier);

		return twitterRequest("POST", "https://api.twitter.com/oauth/access_token", "", parameters)
				.post("").thenApplyAsync(
						wsr -> {
							Map<String, String> responseValues = new TreeMap<>();
							for (String param : wsr.getBody().split("&"))
							{
								String[] keyval = param.split("=");
								responseValues.put(keyval[0], keyval[1]);
							}
							Users user = usersRepository.findByTwitterId(Long.parseLong(responseValues.get("user_id")));
							if (user == null)
							{
								parameters.clear();
								parameters.put("oauth_token", responseValues.get("oauth_token"));
								parameters.put("include_email", "true");
								WSResponse wsResponse = null;
								try
								{
									wsResponse = twitterRequest("GET", "https://api.twitter.com/1.1/account/verify_credentials.json?include_email=true",
											responseValues.get("oauth_token_secret"), parameters)
											.get().toCompletableFuture().get();
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
								JsonNode jsonNode = wsResponse.asJson();

								if (jsonNode.has("email") && jsonNode.get("email") != null)
								{
									user = usersRepository.findByEmail(jsonNode.get("email").asText());
								}
								if (user == null || !user.isConfirmed())
								{
									if (user == null)
									{
										user = new Users();
									}

									user.setName(jsonNode.get("name").asText());
									user.setEmail(jsonNode.has("email") ? jsonNode.get("email").asText() : "");
									user.setAvatarUrl(jsonNode.get("profile_image_url").asText());
									user.setFacebookId(-1L * System.currentTimeMillis());
									user.setTwitterId(jsonNode.get("id").asLong());

									user.setPasswordSalt("" + ThreadLocalRandom.current().nextLong());
									user.setPasswordHash("");

									user.setConfirmed(true);
									user.setConfirmationKeyHash("");
									user.setConfirmationKeyExpirationDate(System.currentTimeMillis());

									usersRepository.saveUser(user);

									flash().put("notification", "You were successfully registered!");
								}
							}

							String sessionToken = sessionsUtils.registerSession(sessionsUtils.AUTH_TYPE_TWITTER, user.getUserId());
							response().setCookie(Http.Cookie.builder("session_token", sessionToken)
									.withMaxAge(java.time.Duration.ofMillis(sessionsUtils.TOKEN_LIFETIME)).build()
							);

							return redirect(routes.HomeController.index());
						},
						httpExecutionContext.current()
				);
	}

	private WSRequest twitterRequest(String method, String url, String tokenSecret, Map<String, String> parameters)
	{
		//Add OAuth parameters
		parameters.put("oauth_consumer_key", config.getString("twitterConsumerKey"));
		parameters.put("oauth_nonce", new BASE64Encoder()
				.encode(("" + ThreadLocalRandom.current().nextLong()).getBytes())
				.replaceAll("[0-9/+=]+", "")
		);
		parameters.put("oauth_signature_method", "HMAC-SHA1");
		parameters.put("oauth_timestamp", "" + (System.currentTimeMillis() / 1000L));
		parameters.put("oauth_version", "1.0");

		//Build parameters string
		String parameterString = String.join("&",
				parameters.entrySet().stream().map(
						entry -> entry.getKey() + "=" + throwlessURLEncoder(entry.getValue())
				).collect(Collectors.toList())
		);

		//Build base string
		String baseString = String.format("%s&%s&%s", method,
				throwlessURLEncoder(url.substring(0, url.indexOf('?') > 0 ? url.indexOf('?') : url.length())),
				throwlessURLEncoder(parameterString));

		//Build signing key
		String signingKey = String.format("%s&%s", config.getString("twitterConsumerSecret"), tokenSecret) ;

		//Sign
		byte[] hmacData = null;
		try
		{
			SecretKeySpec secretKey = new SecretKeySpec(signingKey.getBytes("US-ASCII"), "HmacSHA1");
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(secretKey);
			hmacData = mac.doFinal(baseString.getBytes("US-ASCII"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		parameters.put("oauth_signature", new BASE64Encoder().encode(hmacData));

		//Build header
		String authorizationHeader = "OAuth " + String.join(", ",
				parameters.entrySet().stream()
						.filter(entry -> entry.getKey().startsWith("oauth_"))
						.map(entry -> String.format("%s=\"%s\"", entry.getKey(), throwlessURLEncoder(entry.getValue())))
						.collect(Collectors.toList())
		);

		return wsClient.url(url).addHeader("Authorization", authorizationHeader);
	}

	private String throwlessURLEncoder(String s)
	{
		try
		{
			return URLEncoder.encode(s, "US-ASCII");
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
			return "";
		}
	}
}
package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import controllers.actions.AuthorizationCheckAction;
import controllers.utils.SessionsManager;
import io.ebean.Ebean;
import models.data.Users;
import play.Logger;
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
	private final SessionsManager sessionsManager;

	@Inject
	public TwitterAuthController(WSClient wsClient, HttpExecutionContext httpExecutionContext, SessionsManager sessionsManager)
	{
		this.wsClient = wsClient;
		this.httpExecutionContext = httpExecutionContext;
		this.sessionsManager = sessionsManager;
	}

	public CompletionStage<Result> twitterAuth()
	{
		Map<String, String> parameters = new TreeMap<>();
		parameters.put("oauth_callback", "http://localhost:9000/twitter_auth/continue");
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
							Users user = Ebean.find(Users.class)
									.where()
									.eq("twitter_id", Long.parseLong(responseValues.get("user_id")))
									.findOne();
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
									user.setAvatarUrl(jsonNode.get("profile_image_url").asText());
									user.setFacebookId(-1L * System.currentTimeMillis());
									user.setTwitterId(jsonNode.get("id").asLong());

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
									sessionsManager.AUTH_TYPE_TWITTER,
									request().header("User-Agent").get(), user.getUserId()
							);
							response().setCookie(Http.Cookie.builder("session_token", sessionToken)
									.withMaxAge(java.time.Duration.ofMillis(sessionsManager.TOKEN_LIFETIME))
									.build()
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

		for (Map.Entry<String, String> entry : parameters.entrySet())
		{
			Logger.debug(entry.getKey() + "=" + entry.getValue());
		}

		//Build parameters string
		String parameterString = String.join("&",
				parameters.entrySet().stream().map(
						entry -> entry.getKey() + "=" + throwlessURLEncoder(entry.getValue())
				).collect(Collectors.toList())
		);

		Logger.debug(parameterString);

		//Build base string
		String baseString = String.format("%s&%s&%s", method,
				throwlessURLEncoder(url.substring(0, url.indexOf('?') > 0 ? url.indexOf('?') : url.length())),
				throwlessURLEncoder(parameterString));

		Logger.debug(baseString);

		//Build signing key
		String signingKey = String.format("%s&%s", config.getString("twitterConsumerSecret"), tokenSecret) ;

		Logger.debug(signingKey);

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

		Logger.debug(authorizationHeader);

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
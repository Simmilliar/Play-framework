package controllers;

import io.ebean.Ebean;
import models.data.Sessions;
import models.data.User;
import models.forms.AuthorizationForm;
import models.forms.ProfileEditorForm;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class ApiController extends Controller {

	private final FormFactory formFactory;

	@Inject
	public ApiController(FormFactory formFactory) {
		this.formFactory = formFactory;
	}

	public Result authorize(String email, String password) {
		Map<String, String> authorizationData = new HashMap<>();
		authorizationData.put("email", email);
		authorizationData.put("password", password);
		Form<AuthorizationForm> loginForm = formFactory.form(AuthorizationForm.class).bind(authorizationData);
		if (loginForm.hasErrors()) {
			return badRequest("{\"type\":\"request_errors\",\"errors\":" + loginForm.errorsAsJson() + "}");
		} else {
			String sessionToken = SessionsManager.registerSession(
					request().getHeader("User-Agent"), loginForm.get().email);
			return ok("{\"type\":\"session_token\",\"token\":\"" + sessionToken + "\"}");
		}
	}

	public Result unauthorize(String sessionToken) {
		if (SessionsManager.checkSession(sessionToken)) {
			SessionsManager.unregisterSession(sessionToken);
			return ok("{\"type\":\"response\",\"response\":\"OK\"}");
		} else {
			return badRequest("{\"type\":\"request_errors\",\"errors\":{\"session_token\":[\"Invalid session token.\"]}}");
		}
	}

	public Result userlist(String sessionToken) {
		if (SessionsManager.checkSession(sessionToken)) {
			return ok("{\"type\":\"list_user\",\"list\":" + Json.toJson(Ebean.createSqlQuery("SELECT name, email FROM User").findList()) + "}");
		} else {
			return badRequest("{\"type\":\"request_errors\",\"errors\":{\"session_token\":[\"Invalid session token.\"]}}");
		}
	}

	public Result editProfile(String newName, String newPassword, String sessionToken) {
		if (SessionsManager.checkSession(sessionToken)) {
			Map<String, String> data = new HashMap<>();
			data.put("name", newName);
			data.put("password", newPassword);
			data.put("passwordConfirm", newPassword);
			Form<ProfileEditorForm> form = formFactory.form(ProfileEditorForm.class).bind(data);
			if (form.hasErrors()) {
				return badRequest("{\"type\":\"request_errors\",\"errors\":" + form.errorsAsJson() + "}");
			} else {
				User user = Ebean.find(User.class, Ebean.find(Sessions.class, sessionToken).user.email);
				boolean needToSave = false;
				if (!user.name.equals(newName)) {
					user.name = newName;
					needToSave = true;
				}
				if (!newPassword.isEmpty()) {
					user.passwordHash = Utils.hashString(newPassword);
					needToSave = true;
				}
				if (needToSave) {
					user.save();
				}
				return ok("{\"type\":\"response\",\"response\":\"OK\"}");
			}
		} else {
			return badRequest("{\"type\":\"request_errors\",\"errors\":{\"session_token\":[\"Invalid session token.\"]}}");
		}
	}
}
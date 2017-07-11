package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.ebean.Ebean;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import models.forms.AuthorizationForm;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiController extends Controller {

	private final FormFactory formFactory;

	@Inject
	public ApiController(FormFactory formFactory) {
		this.formFactory = formFactory;
	}

	public Result usersList() {
		SqlQuery query = Ebean.createSqlQuery("SELECT name, email FROM User");
		List<SqlRow> result = query.findList();
		JsonNode json = Json.toJson(result);
		return ok(json);
	}

	public Result authorize(String email, String password) {
		Map<String, String> authorizationData = new HashMap<>();
		authorizationData.put("email", email);
		authorizationData.put("password", password);
		Form<AuthorizationForm> loginForm = formFactory.form(AuthorizationForm.class).bind(authorizationData);
		if (loginForm.hasErrors()) {
			return badRequest(loginForm.errorsAsJson());
		} else {
			String sessionToken = SessionsManager.registerSession(
					request().getHeader("User-Agent"), loginForm.get().email);
			return ok(sessionToken);
		}
	}

	public Result unauthorize(String sessionToken) {
		SessionsManager.unregisterSession(sessionToken);
		return ok("");
	}

	public Result userlist(String sessionToken) {
		if (SessionsManager.checkSession(sessionToken)){
			return ok(Json.toJson(Ebean.createSqlQuery("SELECT name, email FROM User").findList()));
		} else {
			return badRequest("{type: \"error\", body: { message: \"Invalid session token.\" } }");
		}
	}
}
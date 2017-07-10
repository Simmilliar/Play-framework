package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.ebean.Ebean;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

public class ApiController extends Controller {
	public Result usersList() {
		SqlQuery query = Ebean.createSqlQuery("SELECT name, email FROM User");
		List<SqlRow> result = query.findList();
		JsonNode json = Json.toJson(result);
		return ok(json);
	}
}
package controllers;

import controllers.actions.AuthorizationCheckAction;
import io.ebean.Ebean;
import models.data.Users;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;

import java.util.List;

@With(AuthorizationCheckAction.class)
public class HomeController extends Controller
{
	public Result index()
	{
		if (request().attrs().get(AuthorizationCheckAction.USER) != null)
		{
			List<Users> users = Ebean.find(Users.class).where()
					.eq("confirmed", true)
					.findList();
			return ok(views.html.userlist.render(users));
		}
		else
		{
			return ok(views.html.index.render());
		}
	}
}
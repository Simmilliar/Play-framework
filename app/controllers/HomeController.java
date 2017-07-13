package controllers;

import io.ebean.Ebean;
import models.data.User;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

import static controllers.utils.SessionsManager.userAuthorized;

public class HomeController extends Controller
{
	public Result index()
	{
		if (userAuthorized(request()))
		{
			List<User> users = Ebean.find(User.class).findList();
			return ok(views.html.userlist.render(users));
		}
		else
		{
			return ok(views.html.index.render());
		}
	}
}
package controllers;

import io.ebean.Ebean;
import models.data.Users;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

import static controllers.utils.SessionsManager.userAuthorized;

public class HomeController extends Controller
{
	public Result index()
	{
		Result result;
		if (userAuthorized(request()))
		{
			List<Users> users = Ebean.find(Users.class).where()
					.eq("confirmed", true)
					.findList();
			result = ok(views.html.userlist.render(users));
		}
		else
		{
			// solved todo it's better to redirect to login screen from here
			// not sure. there can be (or even should be) some welcome info.
			result = ok(views.html.index.render());
		}
		// solved todo why to set up empty message here?
		// for now there's no need for this
		return result;
	}
}
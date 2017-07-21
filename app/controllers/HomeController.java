package controllers;

import controllers.utils.Utils;
import io.ebean.Ebean;
import models.data.Users;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.List;

import static controllers.utils.SessionsManager.userAuthorized;

public class HomeController extends Controller
{
	private final Utils utils;

	@Inject
	public HomeController(Utils utils)
	{
		this.utils = utils;
	}

	public Result index()
	{
		Result result;
		if (userAuthorized(request()))
		{
			List<Users> users = Ebean.find(Users.class).findList();
			result = ok(views.html.userlist.render(users, utils.getNotification(request())));
		}
		else
		{
			// solved todo it's better to redirect to login screen from here
			// not sure. there can be (or even should be) some welcome info.
			result = ok(views.html.index.render(utils.getNotification(request())));
		}
		// solved todo why to set up empty message here?
		// cause we need to show notif only once. so after showing we remove it.
		// i can only add this if clause to make it better
		if (!utils.getNotification(request()).equals(""))
		{
			utils.setNotification(response(), "", request().host());
		}
		return result;
	}
}
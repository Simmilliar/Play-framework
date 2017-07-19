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
			result = ok(views.html.index.render(utils.getNotification(request())));
		}
		utils.setNotification(response(), "");
		return result;
	}
}
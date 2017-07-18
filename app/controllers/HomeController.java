package controllers;

import akka.actor.ActorSystem;
import controllers.utils.Utils;
import io.ebean.Ebean;
import models.data.User;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.List;

import static controllers.utils.SessionsManager.userAuthorized;

public class HomeController extends Controller
{
	private final Utils utils;

	@Inject
	public HomeController(ActorSystem system, Utils utils)
	{
		/*ActorRef sessionsCleaner = system.actorOf(SessionsCleaner.getProps());
		system.scheduler().schedule(
				Duration.create(0, TimeUnit.MILLISECONDS),
				Duration.create(7, TimeUnit.DAYS),
				sessionsCleaner,
				new SessionsCleanerProtocol.SayClear(),
				system.dispatcher(),
				sessionsCleaner
		);*/
		this.utils = utils;
	}

	public Result index()
	{
		Result result;
		if (userAuthorized(request()))
		{
			List<User> users = Ebean.find(User.class).findList();
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
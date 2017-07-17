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
	@Inject
	public HomeController(ActorSystem system)
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
	}

	public Result index()
	{
		Result result;
		if (userAuthorized(request()))
		{
			List<User> users = Ebean.find(User.class).findList();
			result = ok(views.html.userlist.render(users, Utils.getNotification(request())));
		}
		else
		{
			result = ok(views.html.index.render(Utils.getNotification(request())));
		}
		Utils.setNotification(response(), "");
		return result;
	}
}
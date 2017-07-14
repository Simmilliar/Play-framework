package controllers;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import controllers.scheduled.SessionsCleaner;
import controllers.scheduled.SessionsCleanerProtocol;
import io.ebean.Ebean;
import models.data.User;
import play.mvc.Controller;
import play.mvc.Result;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static controllers.utils.SessionsManager.userAuthorized;

public class HomeController extends Controller
{
	@Inject
	public HomeController(ActorSystem system)
	{
		ActorRef sessionsCleaner = system.actorOf(SessionsCleaner.getProps());
		system.scheduler().schedule(
				Duration.create(0, TimeUnit.MILLISECONDS),
				Duration.create(7, TimeUnit.DAYS),
				sessionsCleaner,
				new SessionsCleanerProtocol.SayClear(),
				system.dispatcher(),
				sessionsCleaner
		);
	}

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
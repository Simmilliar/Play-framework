package controllers.scheduled;

import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import io.ebean.Ebean;
import models.data.Session;
import models.data.Users;

public class SessionsCleaner extends UntypedAbstractActor
{
	public static Props getProps()
	{
		return Props.create(SessionsCleaner.class);
	}

	public void onReceive(Object msg) throws Exception
	{
		if (msg instanceof SessionsCleanerProtocol.SayClear)
		{
			System.out.println("[info] Expired sessions and unconfirmed users cleanup...");
			Ebean.deleteAll(
					Ebean.find(Session.class).where()
							.le("expiration_date", System.currentTimeMillis() / 1000L)
							.findList()
			);
			Ebean.deleteAll(
					Ebean.find(Users.class).where()
							.eq("confirmed", false)
							.findList()
			);
			System.out.println("[info] Expired sessions and unconfirmed users cleanup success!");
		}
	}
}
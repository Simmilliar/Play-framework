package controllers;

import controllers.actions.AuthorizationCheckAction;
import models.Users;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;

import javax.inject.Inject;
import java.util.List;

@With(AuthorizationCheckAction.class)
public class HomeController extends Controller
{
	private final UsersRepository usersRepository;

	@Inject
	public HomeController(UsersRepository usersRepository)
	{
		this.usersRepository = usersRepository;
	}

	public Result index()
	{
		if (request().attrs().get(AuthorizationCheckAction.USER) != null)
		{
			List<Users> users = usersRepository.usersList();
			return ok(views.html.userlist.render(users));
		}
		else
		{
			return ok(views.html.index.render());
		}
	}
}
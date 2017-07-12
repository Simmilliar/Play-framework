package controllers;

import io.ebean.Ebean;
import models.data.Sessions;
import models.forms.AuthorizationForm;
import models.data.User;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.twirl.api.Content;

import javax.inject.Inject;
import java.time.Duration;
import java.util.List;

import static controllers.SessionsManager.userAuthorized;

public class HomeController extends Controller {

	public Result index() {
		if (userAuthorized(request())){
			List<User> users = Ebean.find(User.class).findList();
			return ok(views.html.userlist.render(users));
		} else {
			return ok(views.html.index.render());
		}
    }
}
package controllers;

import io.ebean.Ebean;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import models.data.Sessions;
import models.data.User;
import models.forms.ProfileEditorForm;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static controllers.SessionsManager.userAuthorized;

public class ProfileEditorController extends Controller {

	private final FormFactory formFactory;

	@Inject
	public ProfileEditorController(FormFactory formFactory) {
		this.formFactory = formFactory;
	}

	public Result profileEditor() {
		if (userAuthorized(request())) {
			SqlQuery sqlQuery = Ebean.createSqlQuery(
					"SELECT User.name, User.email " +
							"FROM User JOIN Sessions ON User.email = Sessions.user_email " +
							"WHERE Sessions.token = :token");
			sqlQuery.setParameter("token", request().cookies().get("session_token").value());
			SqlRow sqlRow = sqlQuery.findOne();
			if (sqlRow != null) {
				Map<String, String> data = new HashMap<>();
				data.put("email", sqlRow.getString("email"));
				data.put("name", sqlRow.getString("name"));
				Form<ProfileEditorForm> form = formFactory.form(ProfileEditorForm.class).bind(data);
				return ok(views.html.editprofile.render(form));
			} else {
				return redirect(routes.HomeController.index());
			}
		} else {
			return redirect(routes.HomeController.index());
		}
	}

	public Result edit() {
		Form<ProfileEditorForm> form = formFactory.form(ProfileEditorForm.class).bindFromRequest();
		if (form.hasErrors()) {
			return badRequest(views.html.editprofile.render(form));
		} else if (userAuthorized(request()) &&
				Ebean.find(Sessions.class, request().cookies().get("session_token").value()).user.email.equals(form.get().email)) {
			ProfileEditorForm profileEditorForm = form.get();
			User user = Ebean.find(User.class, profileEditorForm.email);
			boolean needToSave = false;
			if (!user.name.equals(profileEditorForm.name)) {
				user.name = profileEditorForm.name;
				needToSave = true;
			}
			if (!profileEditorForm.password.isEmpty()) {
				user.passwordHash = Utils.hashString(profileEditorForm.password);
				needToSave = true;
			}
			if (needToSave) {
				user.save();
			}
			return redirect(routes.HomeController.index());
		} else {
			return badRequest(views.html.editprofile.render(form));
		}
	}
}

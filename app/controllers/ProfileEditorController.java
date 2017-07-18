package controllers;

import controllers.utils.Utils;
import io.ebean.Ebean;
import models.data.Session;
import models.data.Users;
import models.forms.ProfileEditorForm;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static controllers.utils.SessionsManager.userAuthorized;

public class ProfileEditorController extends Controller
{
	private final FormFactory formFactory;
	private final Utils utils;

	@Inject
	public ProfileEditorController(FormFactory formFactory, Utils utils)
	{
		this.formFactory = formFactory;
		this.utils = utils;
	}

	public Result profileEditor()
	{
		if (userAuthorized(request()))
		{
			Session session = Ebean.find(Session.class).where()
					.eq("token", request().cookies().get("session_token").value())
					.findOne();

			Map<String, String> data = new HashMap<>();
			data.put("email", session.user.email);
			data.put("name", session.user.name);
			Form<ProfileEditorForm> form = formFactory.form(ProfileEditorForm.class).bind(data);

			return ok(views.html.editprofile.render(form));
		}
		return redirect(routes.HomeController.index());
	}

	public Result edit()
	{
		if (userAuthorized(request()))
		{
			Form<ProfileEditorForm> form = formFactory.form(ProfileEditorForm.class).bindFromRequest();
			if (form.hasErrors())
			{
				return badRequest(views.html.editprofile.render(form));
			}
			else
			{
				Users user = Ebean.find(Session.class).where()
						.eq("token", request().cookies().get("session_token").value())
						.findOne().user;
				boolean needToSave = false;
				if (!user.name.equals(form.get().name))
				{
					user.name = form.get().name;
					needToSave = true;
				}
				if (!form.get().password.isEmpty())
				{
					user.passwordHash = Utils.hashString(form.get().password);
					needToSave = true;
				}
				if (needToSave)
				{
					user.save();
					utils.setNotification(response(), "Your profile info successfully changed.");
				}
			}
		}
		return redirect(routes.HomeController.index());
	}
}
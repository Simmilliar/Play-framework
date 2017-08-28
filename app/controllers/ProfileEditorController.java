package controllers;

import controllers.actions.AuthorizationCheckAction;
import controllers.utils.FileUploader;
import controllers.utils.Utils;
import models.Users;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;

import javax.inject.Inject;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@With(AuthorizationCheckAction.class)
public class ProfileEditorController extends Controller
{
	private final FormFactory formFactory;
	private final Utils utils;
	private final UsersRepository usersRepository;
	private final FileUploader fileUploader;

	@Inject
	public ProfileEditorController(FormFactory formFactory, Utils utils, UsersRepository usersRepository,
								   FileUploader fileUploader)
	{
		this.formFactory = formFactory;
		this.utils = utils;
		this.usersRepository = usersRepository;
		this.fileUploader = fileUploader;
	}

	public Result profileEditor()
	{
		Users user = ((Users)ctx().args.get("user"));

		Map<String, String> data = new HashMap<>();
		data.put("email", user.getEmail());
		data.put("name", user.getName());
		data.put("avatarUrl", user.getAvatarUrl());
		DynamicForm form = formFactory.form().bind(data);

		return ok(views.html.editprofile.render(form));
	}

	public Result edit()
	{
		Users user = ((Users)ctx().args.get("user"));

		DynamicForm profileEditorForm = formFactory.form().bindFromRequest();
		String name = profileEditorForm.get("name");
		String password = profileEditorForm.get("password");
		String passwordConfirm = profileEditorForm.get("passwordConfirm");

		String avatarUrl = null;

		//SECTION BEGIN: Checking
		if (name == null || password == null || passwordConfirm == null ||
				request().body().asMultipartFormData().getFile("avatarFile") == null)
		{
			return badRequest(views.html.editprofile.render(profileEditorForm.withError("", "Missing fields.")));
		}
		else
		{
			Http.MultipartFormData.FilePart avatarFilePart =
					request().body().asMultipartFormData().getFile("avatarFile");
			if (!name.matches(Utils.REGEX_NAME))
			{
				return badRequest(views.html.editprofile.render(profileEditorForm.withError("name", "Invalid name.")));
			}
			else if (password.length() > 0 && password.length() < 8)
			{
				return badRequest(views.html.editprofile.render(
						profileEditorForm.withError("password", "Password must be at least 8 symbols long.")));
			}
			else if (!passwordConfirm.equals(password))
			{
				return badRequest(views.html.editprofile.render(
						profileEditorForm.withError("passwordConfirm", "Passwords does not match.")));
			}
			else
			{
				if (((File)avatarFilePart.getFile()).length() > 0)
				{
					avatarUrl = fileUploader.uploadImageAndCropSquared((File)avatarFilePart.getFile(), 200);
					if (avatarUrl == null)
					{
						return badRequest(views.html.editprofile.render(
								profileEditorForm.withError("avatarFile", "Unable to read file as image.")));
					}
				}
			}
		}
		//SECTION END: Checking

		boolean needToSave = false;
		if (avatarUrl != null)
		{
			user.setAvatarUrl(avatarUrl);
			needToSave = true;
		}
		if (!user.getName().equals(name))
		{
			user.setName(name);
			needToSave = true;
		}
		if (!password.isEmpty())
		{
			user.setPasswordSalt("" + ThreadLocalRandom.current().nextInt());
			user.setPasswordHash(utils.hashString(password, user.getPasswordSalt()));
			needToSave = true;
		}
		if (needToSave)
		{
			usersRepository.saveUser(user);
			flash().put("notification", "Your profile info successfully changed.");
		}

		return redirect(routes.HomeController.index());
	}
}
package controllers;

import controllers.actions.AuthorizationCheckAction;
import controllers.utils.ImageMagickService;
import controllers.utils.Utils;
import models.S3File;
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
	private final ImageMagickService imageMagickService;
	private final S3FileRepository s3FileRepository;
	private final UsersRepository usersRepository;

	@Inject
	public ProfileEditorController(FormFactory formFactory, Utils utils, ImageMagickService imageMagickService,
								   S3FileRepository s3FileRepository, UsersRepository usersRepository)
	{
		this.formFactory = formFactory;
		this.utils = utils;
		this.imageMagickService = imageMagickService;
		this.s3FileRepository = s3FileRepository;
		this.usersRepository = usersRepository;
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
		String email = profileEditorForm.get("email");
		String password = profileEditorForm.get("password");
		String passwordConfirm = profileEditorForm.get("passwordConfirm");
		Http.MultipartFormData.FilePart avatarFilePart =
				request().body().asMultipartFormData().getFile("avatarFile");

		//SECTION BEGIN: Checking
		if (name == null || email == null || password == null || passwordConfirm == null || avatarFilePart == null)
		{
			profileEditorForm = profileEditorForm.withError("", "Missing fields.");
		}
		else
		{
			if (!name.matches(Utils.REGEX_NAME))
			{
				profileEditorForm = profileEditorForm.withError("name", "Invalid name.");
			}
			else if (password.length() > 0 && password.length() < 8)
			{
				profileEditorForm = profileEditorForm.withError("password", "Password must be at least 8 symbols long.");
			}
			else if (!passwordConfirm.equals(password))
			{
				profileEditorForm = profileEditorForm.withError("passwordConfirm", "Passwords does not match.");
			}
			else
			{
				if (((File)avatarFilePart.getFile()).length() > 0)
				{
					if (!imageMagickService.cropImageSquared(((File) avatarFilePart.getFile()).getAbsolutePath(), 200))
					{
						profileEditorForm = profileEditorForm.withError("avatarFile", "Unable to read file as image.");
					}
				}
			}
		}
		if (profileEditorForm.hasErrors())
		{
			return badRequest(views.html.editprofile.render(profileEditorForm));
		}
		//SECTION END: Checking

		boolean needToSave = false;
		if (((File)avatarFilePart.getFile()).length() > 0)
		{
			S3File s3File = new S3File();
			s3File.file = (File) avatarFilePart.getFile();
			s3FileRepository.saveFile(s3File);

			user.setAvatarUrl(s3File.getUrl());
			needToSave = true;
		}
		if (!user.getName().equals(name))
		{
			user.setName(name);
			needToSave = true;
		}
		if (password.isEmpty())
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
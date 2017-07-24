package controllers;

import controllers.actions.AuthorizationCheckAction;
import controllers.utils.Utils;
import models.data.S3File;
import models.data.Users;
import models.forms.ProfileEditorForm;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import play.data.Form;
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

	@Inject
	public ProfileEditorController(FormFactory formFactory)
	{
		this.formFactory = formFactory;
	}

	public Result profileEditor()
	{
		Users user = request().attrs().get(AuthorizationCheckAction.USER);
		Map<String, String> data = new HashMap<>();
		data.put("email", user.email);
		data.put("name", user.name);
		data.put("avatarUrl", user.avatarUrl);
		Form<ProfileEditorForm> form = formFactory.form(ProfileEditorForm.class).bind(data);

		return ok(views.html.editprofile.render(form));
	}

	public Result edit()
	{
		Users user = request().attrs().get(AuthorizationCheckAction.USER);
		Form<ProfileEditorForm> form = formFactory.form(ProfileEditorForm.class).bindFromRequest();
		Http.MultipartFormData.FilePart avatarFilePart =
				request().body().asMultipartFormData().getFile("avatarFile");
		if (avatarFilePart != null && ((File)avatarFilePart.getFile()).length() > 0)
		{
			try
			{
				ConvertCmd convertCmd = new ConvertCmd();
				IMOperation imOperation = new IMOperation();
				imOperation.addImage(((File) avatarFilePart.getFile()).getAbsolutePath());
				imOperation.resize(100, 100, '^');
				imOperation.gravity("Center");
				imOperation.crop(100, 100, 0, 0);
				imOperation.addImage(((File) avatarFilePart.getFile()).getAbsolutePath());
				convertCmd.run(imOperation);
				form.get().avatarFileIsValid = true;
			}
			catch (Exception e)
			{
				form.get().avatarFileIsValid = false;
			}
		}
		if (form.hasErrors())
		{
			return badRequest(views.html.editprofile.render(form));
		}
		else
		{
			ProfileEditorForm pef = form.get();
			boolean needToSave = false;
			if (avatarFilePart != null && ((File)avatarFilePart.getFile()).length() > 0)
			{
				S3File s3File = new S3File();
				s3File.file = (File) avatarFilePart.getFile();
				s3File.save();

				user.avatarUrl = s3File.getUrl();
				needToSave = true;
			}
			if (!user.name.equals(pef.name))
			{
				user.name = pef.name;
				needToSave = true;
			}
			if (!pef.password.isEmpty())
			{
				user.passwordSalt = "" + ThreadLocalRandom.current().nextInt();
				user.passwordHash = Utils.hashString(
						new StringBuilder(pef.password)
								.insert(pef.password.length() / 2, user.passwordSalt)
								.toString()
				);
				needToSave = true;
			}
			if (needToSave)
			{
				user.save();
				flash().put("notification", "Your profile info successfully changed.");
			}
			return redirect(routes.HomeController.index());
		}
	}
}
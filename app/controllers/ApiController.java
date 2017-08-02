package controllers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import controllers.utils.SessionsManager;
import controllers.utils.Utils;
import io.ebean.Ebean;
import models.data.S3File;
import models.data.Session;
import models.data.Users;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

public class ApiController extends Controller
{
	private final SessionsManager sessionsManager;
	private final Utils utils;

	@Inject
	public ApiController(SessionsManager sessionsManager, Utils utils)
	{
		this.sessionsManager = sessionsManager;
		this.utils = utils;
	}

	public Result authorize(String email, String password)
	{
		Multimap<String, String> errors = ArrayListMultimap.create();
		Users foundedUser = null;

		if (!email.matches(Utils.REGEX_EMAIL))
		{
			errors.put("email", "Invalid e-mail address.");
		}
		else
		{
			foundedUser = Ebean.find(Users.class, email);
			if (foundedUser == null || !foundedUser.isConfirmed())
			{
				errors.put("email", "Unregistered user.");
			}
			else
			{
				String hash = utils.hashString(password, foundedUser.getPasswordSalt());
				if (!foundedUser.getPasswordHash().equals(hash))
				{
					errors.put("password", "Wrong password.");
				}
				else if (!request().header("User-Agent").isPresent())
				{
					errors.put("user-agent", "Can't find User-Agent request header which is required for authorization.");
				}
			}
		}

		if (errors.isEmpty())
		{
			String sessionToken = sessionsManager.registerSession(
					"api",
					request().header("User-Agent").get(), foundedUser.getUserId()
			);
			return ok(Json.toJson(sessionToken));
		}
		else
		{
			return badRequest(Json.toJson(errors));
		}
	}

	public Result unauthorize(String sessionToken)
	{
		Multimap<String, String> errors = ArrayListMultimap.create();

		Session session = Ebean.find(Session.class, sessionToken);
		if (session == null || session.isExpired())
		{
			errors.put("session_token", "Invalid session token.");
		}
		if (errors.isEmpty())
		{
			sessionsManager.unregisterSession(sessionToken);
			return ok("");
		}
		else
		{
			return badRequest(Json.toJson(errors.asMap()));
		}
	}

	public Result usersList(String sessionToken)
	{
		Multimap<String, String> errors = ArrayListMultimap.create();

		Session session = Ebean.find(Session.class, sessionToken);
		if (session == null || session.isExpired())
		{
			errors.put("session_token", "Invalid session token.");
		}
		if (errors.isEmpty())
		{
			return ok(Ebean.json().toJson(
					Ebean.find(Users.class)
							.select("name, email, avatarUrl")
							.where()
							.eq("confirmed", true)
							.findList()
			));
		}
		else
		{
			return badRequest(Json.toJson(errors.asMap()));
		}
	}

	public Result editProfile(String newName, String newPassword, String sessionToken)
	{
		Multimap<String, String> errors = ArrayListMultimap.create();

		Session session = Ebean.find(Session.class, sessionToken);
		if (session == null || session.isExpired())
		{
			errors.put("session_token", "Invalid session token.");
		}
		else
		{
			if (!newName.isEmpty() && !newName.matches(Utils.REGEX_NAME))
			{
				errors.put("name", "Invalid name.");
			}
			if (!newPassword.isEmpty() && newPassword.length() < 8)
			{
				errors.put("password", "Password must be at least 8 symbols long.");
			}
		}

		if (errors.isEmpty())
		{
			Users user = session.getUser();
			boolean needToSave = false;
			if (!newName.isEmpty() && !user.getName().equals(newName))
			{
				user.setName(newName);
				needToSave = true;
			}
			if (!newPassword.isEmpty())
			{
				user.setPasswordSalt("" + ThreadLocalRandom.current().nextInt());
				user.setPasswordHash(utils.hashString(newPassword, user.getPasswordSalt()));
				needToSave = true;
			}
			if (needToSave)
			{
				user.save();
			}
			return ok("");
		}
		else
		{
			return badRequest(Json.toJson(errors.asMap()));
		}
	}

	public Result editProfileAvatar(String sessionToken)
	{
		Multimap<String, String> errors = ArrayListMultimap.create();
		Http.MultipartFormData.FilePart avatarFilePart = null;

		Session session = Ebean.find(Session.class, sessionToken);
		if (session == null || session.isExpired())
		{
			errors.put("session_token", "Invalid session token.");
		}
		else
		{

			avatarFilePart = request().body().asMultipartFormData().getFile("avatarFile");

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
				}
				catch (Exception e)
				{
					errors.put("avatarFile", "Unable to read file as image.");
				}
			}
			else
			{
				errors.put("avatarFile", "No file provided.");
			}
		}

		if (errors.isEmpty())
		{
			S3File s3File = new S3File();
			s3File.file = (File) avatarFilePart.getFile();
			s3File.save();

			Users user = session.getUser();
			user.setAvatarUrl(s3File.getUrl());
			user.save();

			return ok("");
		}
		else
		{
			return badRequest(Json.toJson(errors.asMap()));
		}
	}
}
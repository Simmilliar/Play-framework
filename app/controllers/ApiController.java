package controllers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import controllers.repositories.SessionRepository;
import controllers.repositories.UsersRepository;
import controllers.utils.FileUploadUtils;
import controllers.utils.SessionsUtils;
import controllers.utils.Utils;
import io.ebean.Ebean;
import io.ebean.text.PathProperties;
import io.ebean.text.json.JsonWriteOptions;
import models.Session;
import models.Users;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ApiController extends Controller
{
	private final UsersRepository usersRepository;
	private final SessionRepository sessionRepository;
	private final SessionsUtils sessionsUtils;
	private final Utils utils;
	private final FileUploadUtils fileUploadUtils;

	@Inject
	public ApiController(UsersRepository usersRepository, SessionRepository sessionRepository,
						 SessionsUtils sessionsUtils, Utils utils, FileUploadUtils fileUploadUtils)
	{
		this.usersRepository = usersRepository;
		this.sessionRepository = sessionRepository;
		this.sessionsUtils = sessionsUtils;
		this.utils = utils;
		this.fileUploadUtils = fileUploadUtils;
	}

	public Result authorize(String email, String password)
	{
		Multimap<String, String> errors = ArrayListMultimap.create();
		Users foundedUser = null;

		if (utils.isEmailValid(email))
		{
			errors.put("email", "Invalid e-mail address.");
		}
		else
		{
			foundedUser = usersRepository.findByEmail(email);
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
			}
		}

		if (errors.isEmpty())
		{
			String sessionToken = sessionsUtils.registerSession(sessionsUtils.AUTH_TYPE_API,
					foundedUser.getUserId());
			return ok(Json.toJson(sessionToken));
		}
		else
		{
			return badRequest(Json.toJson(errors.asMap()));
		}
	}

	public Result unauthorize(String sessionToken)
	{
		Multimap<String, String> errors = ArrayListMultimap.create();

		Session session = sessionRepository.findByToken(sessionToken);
		if (session == null || session.isExpired())
		{
			errors.put("session_token", "Invalid session token.");
		}
		if (errors.isEmpty())
		{
			sessionsUtils.unregisterSession(sessionToken);
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

		Session session = sessionRepository.findByToken(sessionToken);
		if (session == null || session.isExpired())
		{
			errors.put("session_token", "Invalid session token.");
		}
		if (errors.isEmpty())
		{
			List<Users> users = usersRepository.usersList();

			JsonWriteOptions jwo = new JsonWriteOptions();
			jwo.setPathProperties(PathProperties.parse("(name, email, avatarUrl)"));

			return ok(Ebean.json().toJson(users, jwo));
		}
		else
		{
			return badRequest(Json.toJson(errors.asMap()));
		}
	}

	public Result editProfile(String newName, String newPassword, String sessionToken)
	{
		Multimap<String, String> errors = ArrayListMultimap.create();

		Session session = sessionRepository.findByToken(sessionToken);
		if (session == null || session.isExpired())
		{
			errors.put("session_token", "Invalid session token.");
		}
		else
		{
			if (!newName.isEmpty() && !utils.isNameValid(newName))
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
		Http.MultipartFormData.FilePart avatarFilePart;
		String newAvatarUrl = null;

		Session session = sessionRepository.findByToken(sessionToken);
		if (session == null || session.isExpired())
		{
			errors.put("session_token", "Invalid session token.");
		}
		else
		{

			avatarFilePart = request().body().asMultipartFormData().getFile("avatarFile");

			if (avatarFilePart != null && ((File)avatarFilePart.getFile()).length() > 0)
			{
				newAvatarUrl = fileUploadUtils.uploadImageAndCropSquared((File)avatarFilePart.getFile(), 200);
				if (newAvatarUrl == null)
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
			Users user = session.getUser();
			user.setAvatarUrl(newAvatarUrl);
			user.save();

			return ok("");
		}
		else
		{
			return badRequest(Json.toJson(errors.asMap()));
		}
	}
}
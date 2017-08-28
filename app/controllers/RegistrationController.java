package controllers;

import controllers.actions.AuthorizationCheckAction;
import controllers.repositories.UsersRepository;
import controllers.utils.MailerUtils;
import controllers.utils.SessionsUtils;
import controllers.utils.Utils;
import models.Users;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;

import javax.inject.Inject;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@With(AuthorizationCheckAction.class)
public class RegistrationController extends Controller {
	private final FormFactory formFactory;
	private final MailerUtils mailerUtils;
	private final Utils utils;
	private final SessionsUtils sessionsUtils;
	private final UsersRepository usersRepository;

	@Inject
	public RegistrationController(FormFactory formFactory, MailerUtils mailerUtils, Utils utils,
								  SessionsUtils sessionsUtils, UsersRepository usersRepository) {
		this.formFactory = formFactory;
		this.mailerUtils = mailerUtils;
		this.utils = utils;
		this.sessionsUtils = sessionsUtils;
		this.usersRepository = usersRepository;
	}

	public Result registration() {
		return ok(views.html.registration.render(formFactory.form()));
	}

	public Result register() {
		DynamicForm registrationForm = formFactory.form().bindFromRequest();

		String name = registrationForm.get("name");
		String email = registrationForm.get("email");
		String password = registrationForm.get("password");
		String passwordConfirm = registrationForm.get("passwordConfirm");

		Users user = null;

		//SECTION BEGIN: Checking
		if (name == null || email == null || password == null || passwordConfirm == null) {
			return badRequest(views.html.registration.render(
					registrationForm.withError("", "Missing fields.")));
		}
		if (!utils.isNameValid(name)) {
			return badRequest(views.html.registration.render(
					registrationForm.withError("name", "Invalid name.")));
		}
		if (!utils.isEmailValid(email)) {
			return badRequest(views.html.registration.render(
					registrationForm.withError("email", "Invalid e-mail address.")));
		}
		if (password.length() < 8) {
			return badRequest(views.html.registration.render(
					registrationForm.withError("password", "Password must be at least 8 symbols long.")));
		}
		if (!password.equals(passwordConfirm)) {
			return badRequest(views.html.registration.render(
					registrationForm.withError("passwordConfirm", "Passwords does not match.")));
		}

		user = usersRepository.findByEmail(email);
		if (user != null && user.isConfirmed()) {
			return badRequest(views.html.registration.render(
					registrationForm.withError("email", "This e-mail is already registered.")));
		}
		//SECTION END: Checking

		if (user == null) {
			user = new Users();
		}
		user.setName(name);
		user.setEmail(email);
		user.setAvatarUrl(routes.Assets.versioned(new Assets.Asset(Utils.DEFAULT_AVATAR_ASSET)).url());
		user.setFacebookId(-1L * System.currentTimeMillis());
		user.setTwitterId(-1L * System.currentTimeMillis());

		user.setPasswordSalt("" + ThreadLocalRandom.current().nextLong());
		user.setPasswordHash(utils.hashString(password, user.getPasswordSalt()));

		user.setConfirmed(false);
		String confirmationKey = UUID.randomUUID().toString();
		user.setConfirmationKeyHash(utils.hashString(confirmationKey, confirmationKey));
		user.setConfirmationKeyExpirationDate(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));

		try {
			String confirmationBodyText = String.format(Utils.EMAIL_CONFIRMATION,
					routes.RegistrationController.confirmEmail(confirmationKey).absoluteURL(request()));
			mailerUtils.sendEmail(user.getEmail(), "Registration confirmation.", confirmationBodyText);
			flash().put("notification", "We'll send you an e-mail to confirm your registration.");
		} catch (Exception e) {
			return internalServerError(views.html.registration.render(registrationForm.withError("email", "Unable to send confirmation email.")));
		}

		usersRepository.saveUser(user);

		return redirect(routes.HomeController.index());
	}

	public Result confirmEmail(String key) {
		Users user = usersRepository.findUnconfirmedByConfirmationKey(key);
		if (user != null) {
			user.setConfirmed(true);
			user.setConfirmationKeyHash("");
			user.setConfirmationKeyExpirationDate(System.currentTimeMillis());
			usersRepository.saveUser(user);

			flash().put("notification", "You were successfully registered!");

			String sessionToken = sessionsUtils.registerSession(sessionsUtils.AUTH_TYPE_PASSWORD, user.getUserId());
			response().setCookie(Http.Cookie.builder("session_token", sessionToken)
					.withMaxAge(Duration.ofMillis(sessionsUtils.TOKEN_LIFETIME)).build());
		}
		return redirect(routes.HomeController.index());
	}
}
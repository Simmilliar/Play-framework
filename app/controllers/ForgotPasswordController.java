package controllers;

import controllers.actions.AuthorizationCheckAction;
import controllers.utils.MailerService;
import controllers.utils.Utils;
import models.Users;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@With(AuthorizationCheckAction.class)
public class ForgotPasswordController extends Controller {
	private final FormFactory formFactory;
	private final MailerService mailerService;
	private final Utils utils;
	private final UsersRepository usersRepository;

	@Inject
	public ForgotPasswordController(FormFactory formFactory, MailerService mailerService, Utils utils,
									UsersRepository usersRepository) {
		this.formFactory = formFactory;
		this.mailerService = mailerService;
		this.utils = utils;
		this.usersRepository = usersRepository;
	}

	public Result forgotPassword() {
		return ok(views.html.forgotpassword.render(formFactory.form()));
	}

	public Result sendForgotMail() {
		DynamicForm forgotPasswordForm = formFactory.form().bindFromRequest();

		String email = forgotPasswordForm.get("email");

		Users user = null;

		//SECTION BEGIN: Checking
		if (email == null) {
			return badRequest(views.html.forgotpassword.render(
					forgotPasswordForm.withError("", "Missing fields.")));
		} else {
			if (!email.matches(Utils.REGEX_EMAIL)) {
				return badRequest(views.html.forgotpassword.render(
						forgotPasswordForm.withError("email", "Invalid e-mail address.")));
			} else {
				user = usersRepository.findByEmail(email);
				if (user == null || !user.isConfirmed()) {
					return badRequest(views.html.forgotpassword.render(
							forgotPasswordForm.withError("email", "Unregistered user.")));
				}
			}
		}
		//SECTION END: Checking

		String confirmationKey = UUID.randomUUID().toString();
		user.setConfirmationKeyHash(utils.hashString(confirmationKey, confirmationKey));
		user.setConfirmationKeyExpirationDate(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));

		try {
			String confirmationBodyText = String.format(Utils.EMAIL_PASSWORD_CHANGE,
					routes.ForgotPasswordController.changingPassword(confirmationKey).absoluteURL(request()));
			mailerService.sendEmail(email, "Change password.", confirmationBodyText);
			flash().put("notification", "We'll sen you an e-mail to change your password.");
		} catch (Exception e) {
			return internalServerError(views.html.forgotpassword.render(
					forgotPasswordForm.withError("email", "Unable to send confirmation email.")));
		}

		usersRepository.saveUser(user);

		return redirect(routes.HomeController.index());
	}

	public Result changingPassword(String key) {
		if (usersRepository.findConfirmedByConfirmationKey(key) != null) {
			return ok(views.html.changepassword.render(formFactory.form(), key));
		} else {
			return redirect(routes.HomeController.index());
		}
	}

	public Result changePassword(String key) {
		Users user = usersRepository.findConfirmedByConfirmationKey(key);

		if (user != null) {
			DynamicForm changePasswordForm = formFactory.form().bindFromRequest();

			String password = changePasswordForm.get("password");
			String passwordConfirm = changePasswordForm.get("passwordConfirm");

			//SECTION BEGIN: Checking
			if (password == null || passwordConfirm == null) {
				return badRequest(views.html.changepassword.render(
						changePasswordForm.withError("", "Missing fields."), key));
			} else {
				if (password.length() < 8) {
					return badRequest(views.html.changepassword.render(changePasswordForm.withError("password",
							"Password must be at least 8 symbols long."), key));
				} else if (!passwordConfirm.equals(password)) {
					return badRequest(views.html.changepassword.render(changePasswordForm.withError("password",
							"Passwords does not match."), key));
				}
			}
			//SECTION END: Checking

			user.setPasswordSalt("" + ThreadLocalRandom.current().nextLong());
			user.setPasswordHash(utils.hashString(password, user.getPasswordSalt()));

			user.setConfirmationKeyHash("");
			user.setConfirmationKeyExpirationDate(System.currentTimeMillis());

			usersRepository.saveUser(user);

			flash().put("notification", "Password had been changed successfully.");

			return redirect(routes.AuthorizationController.authorization());
		} else {
			return redirect(routes.HomeController.index());
		}
	}
}
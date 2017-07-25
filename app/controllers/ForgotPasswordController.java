package controllers;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import controllers.actions.AuthorizationCheckAction;
import controllers.utils.MailerService;
import controllers.utils.Utils;
import io.ebean.Ebean;
import models.data.Users;
import models.forms.ChangePasswordForm;
import models.forms.ForgotPasswordForm;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;

import javax.inject.Inject;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@With(AuthorizationCheckAction.class)
public class ForgotPasswordController extends Controller
{
	private final FormFactory formFactory;
	private final MailerService mailerService;
	private final Utils utils;
	private final Config config = ConfigFactory.load();

	@Inject
	public ForgotPasswordController(FormFactory formFactory, MailerService mailerService, Utils utils)
	{
		this.formFactory = formFactory;
		this.mailerService = mailerService;
		this.utils = utils;
	}

	public Result forgotPassword()
	{
		return ok(views.html.forgotpassword.render(formFactory.form(ForgotPasswordForm.class)));
	}

	public Result sendForgotMail()
	{
		Form<ForgotPasswordForm> forgotPasswordForm = formFactory.form(ForgotPasswordForm.class).bindFromRequest();
		ForgotPasswordForm forgotPasswordData = forgotPasswordForm.get();

		Users user = Ebean.find(Users.class, forgotPasswordData.email);
		if (user == null || !user.isConfirmed())
		{
			forgotPasswordData.errors.add(new ValidationError("email", "No registered user with this e-mail."));
		}

		if (forgotPasswordForm.hasErrors())
		{
			return badRequest(views.html.forgotpassword.render(forgotPasswordForm));
		}
		else
		{
			String confirmationKey = System.currentTimeMillis() + "" + ThreadLocalRandom.current().nextInt();
			user.setConfirmationKeyHash(utils.hashString(confirmationKey, confirmationKey));
			user.setConfirmationKeyExpirationDate(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));

			try
			{
				String confirmationBodyText = String.format(config.getString("EMAIL_PASSWORD_CHANGE"),
						routes.ForgotPasswordController.changingPassword(confirmationKey).absoluteURL(request()));
				mailerService.sendEmail(forgotPasswordData.email, "Change password.", confirmationBodyText);
				flash().put("notification", "We'll sen you an e-mail to change your password.");
			}
			catch (Exception e)
			{
				e.printStackTrace();
				forgotPasswordData.errors.add(new ValidationError("email", "Unable to send confirmation email."));
				return internalServerError(views.html.forgotpassword.render(forgotPasswordForm));
			}

			user.save();

			return redirect(routes.HomeController.index());
		}
	}

	public Result changingPassword(String key)
	{
		if (Ebean.find(Users.class)
				.where()
				.and()
				.eq("confirmation_key_hash", utils.hashString(key, key))
				.eq("confirmed", true)
				.gt("confirmation_key_expiration_date", System.currentTimeMillis())
				.endAnd()
				.findOne() != null)
		{
			return ok(views.html.changepassword.render(formFactory.form(ChangePasswordForm.class), key));
		}
		else
		{
			return redirect(routes.HomeController.index());
		}
	}

	public Result changePassword(String key)
	{
		Users user = Ebean.find(Users.class)
				.where()
				.and()
				.eq("confirmation_key_hash", utils.hashString(key, key))
				.eq("confirmed", false)
				.gt("confirmation_key_expiration_date", System.currentTimeMillis())
				.endAnd()
				.findOne();
		if (user != null)
		{
			Form<ChangePasswordForm> changePasswordForm = formFactory.form(ChangePasswordForm.class).bindFromRequest();
			ChangePasswordForm changePasswordData = changePasswordForm.get();
			if (changePasswordForm.hasErrors())
			{
				return badRequest(views.html.changepassword.render(changePasswordForm, key));
			}
			else
			{
				user.setPasswordSalt("" + ThreadLocalRandom.current().nextInt());
				user.setPasswordHash(utils.hashString(changePasswordData.password, user.getPasswordSalt()));

				user.setConfirmationKeyHash("");
				user.setConfirmationKeyExpirationDate(System.currentTimeMillis());

				user.save();

				flash().put("notification", "Password had been changed successfully.");

				return redirect(routes.AuthorizationController.authorization());
			}
		}
		else
		{
			return redirect(routes.HomeController.index());
		}
	}
}
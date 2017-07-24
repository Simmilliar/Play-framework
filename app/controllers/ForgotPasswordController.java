package controllers;

import controllers.actions.AuthorizationCheckAction;
import controllers.utils.MailerService;
import controllers.utils.Utils;
import io.ebean.Ebean;
import models.data.Users;
import models.forms.ChangePasswordForm;
import models.forms.ForgotPasswordForm;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;

import javax.inject.Inject;
import java.util.concurrent.ThreadLocalRandom;

@With(AuthorizationCheckAction.class)
public class ForgotPasswordController extends Controller
{
	private final FormFactory formFactory;
	private final MailerService mailerService;

	@Inject
	public ForgotPasswordController(FormFactory formFactory, MailerService mailerService)
	{
		this.formFactory = formFactory;
		this.mailerService = mailerService;
	}

	public Result forgotPassword()
	{
		return ok(views.html.forgotpassword.render(formFactory.form(ForgotPasswordForm.class)));
	}

	public Result sendForgotMail()
	{
		Form<ForgotPasswordForm> form = formFactory.form(ForgotPasswordForm.class).bindFromRequest();
		if (form.hasErrors())
		{
			return badRequest(views.html.forgotpassword.render(form));
		}
		else
		{
			Users user = Ebean.find(Users.class, form.get().email);
			user.confirmationKey = Utils.hashString(user.confirmationKey + System.currentTimeMillis());
			user.save();
			String confirmationBodyText = String.format(Utils.EMAIL_PASSWORD_CHANGE, request().host(), user.confirmationKey);
			mailerService.sendEmail(form.get().email, "Change password.", confirmationBodyText);
			flash().put("notification", "We'll sen you an e-mail to change your password.");

			return redirect(routes.HomeController.index());
		}
	}

	public Result changingPassword(String key)
	{
		if (Ebean.find(Users.class).where().eq("confirmation_key", key).findOne() != null)
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
		Users user = Ebean.find(Users.class).where().eq("confirmation_key", key).findOne();
		if (user != null)
		{
			Form<ChangePasswordForm> form = formFactory.form(ChangePasswordForm.class).bindFromRequest();
			if (form.hasErrors())
			{
				return badRequest(views.html.changepassword.render(form, key));
			}
			else
			{
				ChangePasswordForm cpf = form.get();
				user.passwordSalt = "" + ThreadLocalRandom.current().nextInt();
				user.passwordHash = Utils.hashString(
						new StringBuilder(cpf.password)
								.insert(cpf.password.length() / 2, user.passwordSalt)
								.toString()
				);
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
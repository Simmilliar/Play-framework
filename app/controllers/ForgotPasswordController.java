package controllers;

import controllers.utils.MailerService;
import controllers.utils.Utils;
import io.ebean.Ebean;
import models.data.User;
import models.forms.ChangePasswordForm;
import models.forms.ForgotPasswordForm;
import play.data.Form;
import play.data.FormFactory;
import play.libs.mailer.MailerClient;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

import static controllers.utils.SessionsManager.userAuthorized;

public class ForgotPasswordController extends Controller
{
	private final FormFactory formFactory;
	private final MailerClient mailerClient;

	@Inject
	public ForgotPasswordController(FormFactory formFactory, MailerClient mailerClient)
	{
		this.formFactory = formFactory;
		this.mailerClient = mailerClient;
	}

	public Result forgotPassword()
	{
		if (!userAuthorized(request()))
		{
			return ok(views.html.forgotpassword.render(formFactory.form(ForgotPasswordForm.class)));
		}
		else
		{
			return redirect(routes.HomeController.index());
		}
	}

	public Result sendForgotMail()
	{
		if (!userAuthorized(request()))
		{
			Form<ForgotPasswordForm> form = formFactory.form(ForgotPasswordForm.class).bindFromRequest();
			if (form.hasErrors())
			{
				return badRequest(views.html.forgotpassword.render(form));
			}
			else
			{
				User user = Ebean.find(User.class, form.get().email);
				user.confirmationKey = Utils.hashString(user.confirmationKey + System.currentTimeMillis());
				user.save();
				String confirmationBodyText = String.format(Utils.EMAIL_PASSWORD_CHANGE, user.confirmationKey);
				new MailerService(mailerClient)
						.sendEmail(form.get().email, "Change password.", confirmationBodyText);
			}
		}
		return redirect(routes.HomeController.index());
	}

	public Result changingPassword(String key)
	{
		if (!userAuthorized(request()) &&
				Ebean.find(User.class).where().eq("confirmation_key", key).findOne() != null)
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
		User user = Ebean.find(User.class).where().eq("confirmation_key", key).findOne();
		if (!userAuthorized(request()) && user != null)
		{
			Form<ChangePasswordForm> form = formFactory.form(ChangePasswordForm.class).bindFromRequest();
			if (form.hasErrors())
			{
				return badRequest(views.html.changepassword.render(form, key));
			}
			else
			{
				user.passwordHash = Utils.hashString(form.get().password);
				user.save();
				return redirect(routes.AuthorizationController.authorization());
			}
		}
		else
		{
			return redirect(routes.HomeController.index());
		}
	}
}
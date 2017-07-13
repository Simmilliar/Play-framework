package controllers;

import io.ebean.Ebean;
import models.data.User;
import models.forms.ChangePasswordForm;
import models.forms.ForgottPasswordForm;
import play.data.Form;
import play.data.FormFactory;
import play.libs.mailer.MailerClient;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

import static controllers.SessionsManager.userAuthorized;

public class ForgottPasswordController extends Controller {

	private final FormFactory formFactory;
	private final MailerClient mailerClient;

	@Inject
	public ForgottPasswordController(FormFactory formFactory, MailerClient mailerClient) {
		this.formFactory = formFactory;
		this.mailerClient = mailerClient;
	}

	public Result forgottPassword() {
		if (!userAuthorized(request())) {
			return ok(views.html.forgottpassword.render(formFactory.form(ForgottPasswordForm.class)));
		} else {
			return redirect(routes.HomeController.index());
		}
	}

	public Result sendForgottMail() {
		if (!userAuthorized(request())) {
			Form<ForgottPasswordForm> form = formFactory.form(ForgottPasswordForm.class).bindFromRequest();
			if (form.hasErrors()) {
				return badRequest(views.html.forgottpassword.render(form));
			} else {
				User user = Ebean.find(User.class, form.get().email);
				user.confirmationKey = Utils.hashString(user.confirmationKey + System.currentTimeMillis());
				user.save();
				String confirmationBodyText = "You can change your password " +
						"by following this link: http://localhost:9000/changepassword?key=" + user.confirmationKey +
						"\nIf you don't want to do this, just ignore this e-mail.";
				new MailerService(mailerClient).sendEmail(form.get().email, "Changing email", confirmationBodyText);
			}
		}
		return redirect(routes.HomeController.index());
	}

	public Result changingPassword(String key) {
		if (!userAuthorized(request()) && Ebean.find(User.class).where().eq("confirmation_key", key).findOne() != null){
			return ok(views.html.changepassword.render(formFactory.form(ChangePasswordForm.class), key));
		} else {
			return redirect(routes.HomeController.index());
		}
	}

	public Result changePassword(String key) {
		User user = Ebean.find(User.class).where().eq("confirmation_key", key).findOne();
		if (!userAuthorized(request()) && user != null) {
			Form<ChangePasswordForm> form = formFactory.form(ChangePasswordForm.class).bindFromRequest();
			if (form.hasErrors()) {
				return badRequest(views.html.changepassword.render(form, key));
			} else {
				user.passwordHash = Utils.hashString(form.get().password);
				user.save();
				return redirect(routes.AuthorizationController.authorization());
			}
		} else {
			return redirect(routes.HomeController.index());
		}
	}
}
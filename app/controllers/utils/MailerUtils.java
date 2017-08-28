package controllers.utils;

import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;

import javax.inject.Inject;

public class MailerUtils
{
	private MailerClient mailerClient;

	@Inject
	public MailerUtils(MailerClient mailerClient)
	{
		this.mailerClient = mailerClient;
	}

	public void sendEmail(String recipient, String subject, String bodyText)
	{
		Email email = new Email()
				.setSubject(subject)
				.setFrom("Play! <play.framework@meta.ua>")
				.addTo(recipient)
				.setBodyText(bodyText);
		mailerClient.send(email);
	}
}
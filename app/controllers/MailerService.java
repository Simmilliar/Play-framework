package controllers;

import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;

import javax.inject.Inject;

public class MailerService {
	private MailerClient mailerClient;

	@Inject
	public MailerService(MailerClient mailerClient) {
		this.mailerClient = mailerClient;
	}

	public void sendEmail(String recipient, String subject, String bodyText) {
		Email email = new Email()
				.setSubject(subject)
				.setFrom("Play! <play.framework@meta.ua>")
				.addTo(recipient)
				.setBodyText(bodyText);
		mailerClient.send(email);
	}
}
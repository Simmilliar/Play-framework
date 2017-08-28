package controllers;

import controllers.repositories.SessionRepository;
import controllers.repositories.UsersRepository;
import controllers.utils.MailerUtils;
import controllers.utils.SessionsUtils;
import models.Users;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Result;
import play.test.WithApplication;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;
import static play.test.Helpers.*;

public class RegistrationControllerFormTest extends WithApplication {

	private final UsersRepository mockUsersRepository = mock(UsersRepository.class);

	@Override
	protected Application provideApplication() {
		Users mockUnconfirmedUser = mock(Users.class);
		Users mockConfirmedUser = mock(Users.class);

		when(mockConfirmedUser.isConfirmed()).thenReturn(true);
		when(mockUnconfirmedUser.isConfirmed()).thenReturn(false);
		when(mockUsersRepository.findByEmail("registered@email.com")).thenReturn(mockConfirmedUser);
		when(mockUsersRepository.findByEmail("not-confirmed@email.com")).thenReturn(mockUnconfirmedUser);
		when(mockUsersRepository.findByEmail("valid@email.com")).thenReturn(null);

		return new GuiceApplicationBuilder()
				.overrides(bind(SessionRepository.class).toInstance(mock(SessionRepository.class)))
				.overrides(bind(MailerUtils.class).toInstance(mock(MailerUtils.class)))
				.overrides(bind(SessionsUtils.class).toInstance(mock(SessionsUtils.class)))
				.overrides(bind(UsersRepository.class).toInstance(mockUsersRepository))
				.build();
	}

	@Test
	public void registration_success() {
		Map<String, String> formData = new HashMap<>();
		formData.put("name", "Valid Name");
		formData.put("email", "valid@email.com");
		formData.put("password", "longEnoughPassword");
		formData.put("passwordConfirm", "longEnoughPassword");

		Result result = route(app, fakeRequest()
				.method(POST)
				.bodyForm(formData)
				.uri(routes.RegistrationController.register().url()));
		assertEquals(SEE_OTHER, result.status());
	}

	@Test
	public void registration_failed_name_missing() {
		Map<String, String> formData = new HashMap<>();
		//formData.put("name", "");
		formData.put("email", "valid@email.com");
		formData.put("password", "longEnoughPassword");
		formData.put("passwordConfirm", "longEnoughPassword");

		Result result = route(app, fakeRequest()
				.method(POST)
				.bodyForm(formData)
				.uri(routes.RegistrationController.register().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Missing fields."));
	}

	@Test
	public void registration_failed_name_invalid() {
		Map<String, String> formData = new HashMap<>();
		formData.put("name", "!nval1d Name");
		formData.put("email", "valid@email.com");
		formData.put("password", "longEnoughPassword");
		formData.put("passwordConfirm", "longEnoughPassword");

		Result result = route(app, fakeRequest()
				.method(POST)
				.bodyForm(formData)
				.uri(routes.RegistrationController.register().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Invalid name."));
	}

	@Test
	public void registration_failed_email_missing() {
		Map<String, String> formData = new HashMap<>();
		formData.put("name", "Valid Name");
		//formData.put("email", "valid@email.com");
		formData.put("password", "longEnoughPassword");
		formData.put("passwordConfirm", "longEnoughPassword");

		Result result = route(app, fakeRequest()
				.method(POST)
				.bodyForm(formData)
				.uri(routes.RegistrationController.register().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Missing fields."));
	}

	@Test
	public void registration_failed_email_invalid() {
		Map<String, String> formData = new HashMap<>();
		formData.put("name", "Valid Name");
		formData.put("email", "invalid@email");
		formData.put("password", "longEnoughPassword");
		formData.put("passwordConfirm", "longEnoughPassword");

		Result result = route(app, fakeRequest()
				.method(POST)
				.bodyForm(formData)
				.uri(routes.RegistrationController.register().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Invalid e-mail address."));
	}

	@Test
	public void registration_failed_email_already_registered() {
		Map<String, String> formData = new HashMap<>();
		formData.put("name", "Valid Name");
		formData.put("email", "registered@email.com");
		formData.put("password", "longEnoughPassword");
		formData.put("passwordConfirm", "longEnoughPassword");

		Result result = route(app, fakeRequest()
				.method(POST)
				.bodyForm(formData)
				.uri(routes.RegistrationController.register().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("This e-mail is already registered."));
	}

	@Test
	public void registration_success_email_registered_but_unconfirmed() {
		Map<String, String> formData = new HashMap<>();
		formData.put("name", "Valid Name");
		formData.put("email", "not-confirmed@email.com");
		formData.put("password", "longEnoughPassword");
		formData.put("passwordConfirm", "longEnoughPassword");

		Result result = route(app, fakeRequest()
				.method(POST)
				.bodyForm(formData)
				.uri(routes.RegistrationController.register().url()));
		assertEquals(SEE_OTHER, result.status());
	}

	@Test
	public void registration_failed_password_missing() {
		Map<String, String> formData = new HashMap<>();
		formData.put("name", "Valid Name");
		formData.put("email", "valid@email.com");
		//formData.put("password", "longEnoughPassword");
		formData.put("passwordConfirm", "longEnoughPassword");

		Result result = route(app, fakeRequest()
				.method(POST)
				.bodyForm(formData)
				.uri(routes.RegistrationController.register().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Missing fields."));
	}

	@Test
	public void registration_failed_password_short() {
		Map<String, String> formData = new HashMap<>();
		formData.put("name", "Valid Name");
		formData.put("email", "valid@email.com");
		formData.put("password", "shrtpwd");
		formData.put("passwordConfirm", "shrtpwd");

		Result result = route(app, fakeRequest()
				.method(POST)
				.bodyForm(formData)
				.uri(routes.RegistrationController.register().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Password must be at least 8 symbols long."));
	}

	@Test
	public void registration_failed_password_confirm_missing() {
		Map<String, String> formData = new HashMap<>();
		formData.put("name", "Valid Name");
		formData.put("email", "valid@email.com");
		formData.put("password", "longEnoughPassword");
		//formData.put("passwordConfirm", "longEnoughPassword");

		Result result = route(app, fakeRequest()
				.method(POST)
				.bodyForm(formData)
				.uri(routes.RegistrationController.register().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Missing fields."));
	}

	@Test
	public void registration_failed_password_confirm_not_match() {
		Map<String, String> formData = new HashMap<>();
		formData.put("name", "Valid Name");
		formData.put("email", "valid@email.com");
		formData.put("password", "longEnoughPassword");
		formData.put("passwordConfirm", "shortEnoughPassword");

		Result result = route(app, fakeRequest()
				.method(POST)
				.bodyForm(formData)
				.uri(routes.RegistrationController.register().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Passwords does not match."));
	}
}

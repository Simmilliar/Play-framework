package controllers;

import controllers.utils.MailerService;
import controllers.utils.SessionsManager;
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
				.overrides(bind(MailerService.class).toInstance(mock(MailerService.class)))
				.overrides(bind(SessionsManager.class).toInstance(mock(SessionsManager.class)))
				.overrides(bind(UsersRepository.class).toInstance(mockUsersRepository))
				.build();
	}

	@Test
	public void formValid() {
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
	public void formNameMissing() {
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
	public void formNameInvalid() {
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
	public void formEmailMissing() {
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
	public void formEmailInvalid() {
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
	public void formEmailRegistered() {
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
	public void formEmailUnconfirmed() {
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
	public void formPasswordMissing() {
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
	public void formPasswordInvalid() {
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
	public void formPasswordConfirmMissing() {
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
	public void formPasswordConfirmInvalid() {
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

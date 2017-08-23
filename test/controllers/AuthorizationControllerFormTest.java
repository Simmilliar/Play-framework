package controllers;

import controllers.utils.SessionsManager;
import controllers.utils.Utils;
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
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.SEE_OTHER;
import static play.test.Helpers.*;

public class AuthorizationControllerFormTest extends WithApplication {

	private SessionsManager mockSessionsManager;
	private UsersRepository mockUsersRepository;

	@Override
	protected Application provideApplication() {
		mockSessionsManager = mock(SessionsManager.class);
		mockUsersRepository = mock(UsersRepository.class);

		return new GuiceApplicationBuilder()
				.overrides(bind(SessionsManager.class).toInstance(mockSessionsManager))
				.overrides(bind(UsersRepository.class).toInstance(mockUsersRepository))
				.build();
	}

	@Test
	public void formValid() {
		Users mockUser = mock(Users.class);
		when(mockUser.isConfirmed()).thenReturn(true);
		when(mockUser.getPasswordSalt()).thenReturn("12345678");
		when(mockUser.getPasswordHash()).thenReturn(new Utils().hashString("longEnoughPassword", "12345678"));
		when(mockUsersRepository.findByEmail("valid@email.com")).thenReturn(mockUser);

		Map<String, String> formData = new HashMap<>();
		formData.put("email", "valid@email.com");
		formData.put("password", "longEnoughPassword");

		Result result = route(app, fakeRequest()
				.method(POST)
				.bodyForm(formData)
				.uri(routes.AuthorizationController.authorize().url()));
		assertEquals(SEE_OTHER, result.status());
	}

	@Test
	public void formEmailMissing() {
		Map<String, String> formData = new HashMap<>();
		//formData.put("email", "valid@email.com");
		formData.put("password", "longEnoughPassword");

		Result result = route(app, fakeRequest()
				.method(POST)
				.bodyForm(formData)
				.uri(routes.AuthorizationController.authorize().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Missing fields."));
	}

	@Test
	public void formEmailInvalid() {
		Map<String, String> formData = new HashMap<>();
		formData.put("email", "invalid@email");
		formData.put("password", "longEnoughPassword");

		Result result = route(app, fakeRequest()
				.method(POST)
				.bodyForm(formData)
				.uri(routes.AuthorizationController.authorize().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Invalid e-mail address."));
	}

	@Test
	public void formEmailUnregistered() {
		when(mockUsersRepository.findByEmail("unregistered@email.com")).thenReturn(null);

		Map<String, String> formData = new HashMap<>();
		formData.put("email", "unregistered@email.com");
		formData.put("password", "longEnoughPassword");

		Result result = route(app, fakeRequest()
				.method(POST)
				.bodyForm(formData)
				.uri(routes.AuthorizationController.authorize().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Unregistered user."));
	}

	@Test
	public void formEmailUnconfirmed() {
		Users mockUser = mock(Users.class);
		when(mockUser.isConfirmed()).thenReturn(false);
		when(mockUsersRepository.findByEmail("unregistered@email.com")).thenReturn(mockUser);

		Map<String, String> formData = new HashMap<>();
		formData.put("email", "unregistered@email.com");
		formData.put("password", "longEnoughPassword");

		Result result = route(app, fakeRequest()
				.method(POST)
				.bodyForm(formData)
				.uri(routes.AuthorizationController.authorize().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Unregistered user."));
	}

	@Test
	public void formPasswordMissing() {
		Map<String, String> formData = new HashMap<>();
		formData.put("email", "valid@email.com");
		//formData.put("password", "longEnoughPassword");

		Result result = route(app, fakeRequest()
				.method(POST)
				.bodyForm(formData)
				.uri(routes.AuthorizationController.authorize().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Missing fields."));
	}

	@Test
	public void formPasswordInvalid() {
		Users mockUser = mock(Users.class);
		when(mockUser.isConfirmed()).thenReturn(true);
		when(mockUser.getPasswordSalt()).thenReturn("12345678");
		when(mockUser.getPasswordHash()).thenReturn(new Utils().hashString("longEnoughPassword", "12345678"));
		when(mockUsersRepository.findByEmail("valid@email.com")).thenReturn(mockUser);

		Map<String, String> formData = new HashMap<>();
		formData.put("email", "valid@email.com");
		formData.put("password", "invalidPassword");

		Result result = route(app, fakeRequest()
				.method(POST)
				.bodyForm(formData)
				.uri(routes.AuthorizationController.authorize().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Wrong password."));
	}
}
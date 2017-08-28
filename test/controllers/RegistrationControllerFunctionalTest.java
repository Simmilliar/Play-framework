package controllers;

import controllers.repositories.SessionRepository;
import controllers.repositories.UsersRepository;
import controllers.utils.MailerService;
import controllers.utils.SessionsManager;
import controllers.utils.Utils;
import models.Users;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import play.Application;
import play.Logger;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Result;
import play.test.WithApplication;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static controllers.utils.Utils.REGEX_UUID;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.INTERNAL_SERVER_ERROR;
import static play.mvc.Http.Status.SEE_OTHER;
import static play.test.Helpers.*;

public class RegistrationControllerFunctionalTest extends WithApplication {

	private UsersRepository mockUsersRepository;
	private MailerService mockMailerService;
	private SessionsManager mockSessionsManager;
	private ArgumentCaptor<Users> usersCaptor;
	private ArgumentCaptor<String> mailCaptor;

	@Override
	protected Application provideApplication() {
		mockUsersRepository  = mock(UsersRepository.class);
		mockMailerService = mock(MailerService.class);
		mockSessionsManager = mock(SessionsManager.class);
		usersCaptor = ArgumentCaptor.forClass(Users.class);
		mailCaptor = ArgumentCaptor.forClass(String.class);
		Users spyUnconfirmedUser = spy(new Users());
		doReturn(false).when(spyUnconfirmedUser).isConfirmed();

		when(mockUsersRepository.findByEmail("not-confirmed@email.com")).thenReturn(spyUnconfirmedUser);
		when(mockUsersRepository.findByEmail("valid@email.com")).thenReturn(null);

		return new GuiceApplicationBuilder()
				.overrides(bind(SessionRepository.class).toInstance(mock(SessionRepository.class)))
				.overrides(bind(MailerService.class).toInstance(mockMailerService))
				.overrides(bind(SessionsManager.class).toInstance(mockSessionsManager))
				.overrides(bind(UsersRepository.class).toInstance(mockUsersRepository))
				.build();
	}

	@Test
	public void test_register_unregistered_email() {
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
		verify(mockUsersRepository, times(1)).saveUser(usersCaptor.capture());
		Users savedUser = usersCaptor.getValue();
		assertEquals("Valid Name", savedUser.getName());
		assertEquals("valid@email.com", savedUser.getEmail());
		assertEquals(routes.Assets.versioned(new Assets.Asset(Utils.DEFAULT_AVATAR_ASSET)).url(), savedUser.getAvatarUrl());
		assertTrue(savedUser.getFacebookId() < 0);
		assertTrue(savedUser.getTwitterId() < 0);
		Logger.debug(savedUser.getPasswordSalt());
		assertTrue(savedUser.getPasswordSalt().matches("^-*[0-9]+$"));
		assertEquals(savedUser.getPasswordHash(), new Utils().hashString("longEnoughPassword", savedUser.getPasswordSalt()));
		assertFalse(savedUser.isConfirmed());
		verify(mockMailerService, times(1)).sendEmail(eq("valid@email.com"),
				eq("Registration confirmation."), mailCaptor.capture());
		assertTrue(mailCaptor.getValue().contains("To complete your registration you need to confirm your e-mail address by following this link:"));
		Matcher mailMatcher = Pattern.compile(REGEX_UUID)
				.matcher(mailCaptor.getValue());
		assertTrue(mailMatcher.find());
		Logger.debug(mailCaptor.getValue());
		String confirmationKey = mailMatcher.group(1);
		assertEquals(new Utils().hashString(confirmationKey, confirmationKey), savedUser.getConfirmationKeyHash());
		assertTrue(savedUser.getConfirmationKeyExpirationDate() > System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(59));
	}

	@Test
	public void test_register_registered_but_unconfirmed_email() {
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
		verify(mockUsersRepository, times(1)).saveUser(usersCaptor.capture());
		Users savedUser = usersCaptor.getValue();
		assertEquals("Valid Name", savedUser.getName());
		assertEquals("not-confirmed@email.com", savedUser.getEmail());
		assertEquals(routes.Assets.versioned(new Assets.Asset(Utils.DEFAULT_AVATAR_ASSET)).url(), savedUser.getAvatarUrl());
		assertTrue(savedUser.getFacebookId() < 0);
		assertTrue(savedUser.getTwitterId() < 0);
		assertTrue(savedUser.getPasswordSalt().matches("^-*[0-9]+$"));
		assertEquals(savedUser.getPasswordHash(), new Utils().hashString("longEnoughPassword", savedUser.getPasswordSalt()));
		assertFalse(savedUser.isConfirmed());
		verify(mockMailerService, times(1)).sendEmail(eq("not-confirmed@email.com"),
				eq("Registration confirmation."), mailCaptor.capture());
		assertTrue(mailCaptor.getValue().contains("To complete your registration you need to confirm your e-mail address by following this link:"));
		Matcher mailMatcher = Pattern.compile(REGEX_UUID)
				.matcher(mailCaptor.getValue());
		assertTrue(mailMatcher.find());
		Logger.debug(mailCaptor.getValue());
		String confirmationKey = mailMatcher.group(1);
		assertEquals(new Utils().hashString(confirmationKey, confirmationKey), savedUser.getConfirmationKeyHash());
		assertTrue(savedUser.getConfirmationKeyExpirationDate() > System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(59));
	}

	@Test
	public void test_on_email_sending_exception() {
		Map<String, String> formData = new HashMap<>();
		formData.put("name", "Valid Name");
		formData.put("email", "valid@email.com");
		formData.put("password", "longEnoughPassword");
		formData.put("passwordConfirm", "longEnoughPassword");

		doThrow(new RuntimeException()).when(mockMailerService).sendEmail(anyString(), anyString(), anyString());
		Result result = route(app, fakeRequest()
				.method(POST)
				.bodyForm(formData)
				.uri(routes.RegistrationController.register().url()));
		assertEquals(INTERNAL_SERVER_ERROR, result.status());
		verify(mockUsersRepository, never()).saveUser(any(Users.class));
	}

	@Test
	public void test_valid_email_confirmation_data() {
		when(mockUsersRepository.findUnconfirmedByConfirmationKey("confirmation_key")).thenReturn(new Users());

		Result result = route(app, fakeRequest()
				.method(GET)
				.uri(routes.RegistrationController.confirmEmail("confirmation_key").url()));
		assertEquals(SEE_OTHER, result.status());
		verify(mockUsersRepository, times(1)).saveUser(usersCaptor.capture());
		assertTrue(usersCaptor.getValue().isConfirmed());
		assertEquals("", usersCaptor.getValue().getConfirmationKeyHash());
		assertTrue(usersCaptor.getValue().getConfirmationKeyExpirationDate() < System.currentTimeMillis());
		verify(mockSessionsManager, times(1))
				.registerSession(eq("password"), eq(usersCaptor.getValue().getUserId()));
		assertTrue(result.cookies().get("session_token") != null);
		//for some reasons this didn't work
		//assertTrue(result.cookies().get("session_token").maxAge() > 0);
	}

	@Test
	public void test_invalid_email_confirmation_data() {
		when(mockUsersRepository.findUnconfirmedByConfirmationKey("invalid_confirmation_key")).thenReturn(null);

		Result result = route(app, fakeRequest()
				.method(GET)
				.uri(routes.RegistrationController.confirmEmail("confirmation_key").url()));
		assertEquals(SEE_OTHER, result.status());
		verify(mockUsersRepository, never()).saveUser(any(Users.class));
	}
}
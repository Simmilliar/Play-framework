package controllers;

import controllers.repositories.SessionRepository;
import controllers.repositories.UsersRepository;
import controllers.utils.SessionsManager;
import controllers.utils.Utils;
import models.Session;
import models.Users;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.SEE_OTHER;
import static play.test.Helpers.*;

public class AuthorizationControllerFunctionalTest extends WithApplication {

	private SessionsManager mockSessionsManager;
	private SessionRepository mockSessionRepository;
	private UsersRepository mockUsersRepository;

	@Override
	protected Application provideApplication() {
		mockSessionRepository = mock(SessionRepository.class);
		mockSessionsManager = mock(SessionsManager.class);
		mockUsersRepository = mock(UsersRepository.class);

		return new GuiceApplicationBuilder()
				.overrides(bind(SessionRepository.class).toInstance(mockSessionRepository))
				.overrides(bind(SessionsManager.class).toInstance(mockSessionsManager))
				.overrides(bind(UsersRepository.class).toInstance(mockUsersRepository))
				.build();
	}

	@Test
	public void authorization_verifying(){
		Users mockUser = mock(Users.class);
		when(mockUser.isConfirmed()).thenReturn(true);
		when(mockUser.getUserId()).thenReturn(UUID.randomUUID());
		when(mockUser.getPasswordSalt()).thenReturn("12345678");
		when(mockUser.getPasswordHash()).thenReturn(new Utils().hashString("longEnoughPassword", "12345678"));
		when(mockUsersRepository.findByEmail("valid@email.com")).thenReturn(mockUser);
		when(mockSessionsManager.registerSession(anyString(), any(UUID.class))).thenReturn("active_token");

		Map<String, String> formData = new HashMap<>();
		formData.put("email", "valid@email.com");
		formData.put("password", "longEnoughPassword");

		Result result = route(app, fakeRequest()
				.method(POST)
				.bodyForm(formData)
				.uri(routes.AuthorizationController.authorize().url()));
		assertEquals(SEE_OTHER, result.status());
		verify(mockSessionsManager, times(1)).registerSession("password", mockUser.getUserId());
		assertTrue(result.cookies().get("session_token").value().equals("active_token"));
	}

	@Test
	public void logout_verifying(){
		Session mockSession = mock(Session.class);
		when(mockSession.getExpirationDate()).thenReturn(System.currentTimeMillis() + 1000000L);
		when(mockSession.getUser()).thenReturn(new Users());
		when(mockSessionRepository.findByToken("active_token")).thenReturn(mockSession);

		Result result = route(app, fakeRequest()
				.method(GET)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.uri(routes.AuthorizationController.logout().url()));
		assertEquals(SEE_OTHER, result.status());
		verify(mockSessionsManager, times(1)).unregisterSession(eq("active_token"));
		assertTrue(result.cookies().get("session_token").value().equals(""));
	}
}

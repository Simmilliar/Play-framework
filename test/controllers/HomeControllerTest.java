package controllers;

import models.Session;
import models.Users;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import java.time.Duration;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

public class HomeControllerTest extends WithApplication
{
	private UsersRepository mockUsersRepo;
	private SessionRepository mockSessionRepo;
	private Session mockSession;

	@Override
	protected Application provideApplication()
	{
		mockUsersRepo = mock(UsersRepository.class);
		mockSessionRepo = mock(SessionRepository.class);
		mockSession = mock(Session.class);

		return new GuiceApplicationBuilder()
				.overrides(bind(SessionRepository.class).toInstance(mockSessionRepo))
				.overrides(bind(UsersRepository.class).toInstance(mockUsersRepo))
				.build();
	}

	@Test
	public void testUnauthorized()
	{
		Http.RequestBuilder request = new Http.RequestBuilder()
				.method(GET)
				.uri(routes.HomeController.index().url());

		Result result = route(app, request);
		assertEquals(OK, result.status());
		verify(mockUsersRepo, never()).usersList();
		assertTrue(contentAsString(result).contains("Lorem ipsum dolor sit amet, consectetur adipiscing elit."));
	}

	@Test
	public void testAuthorized()
	{
		when(mockSession.getExpirationDate()).thenReturn(System.currentTimeMillis() + 1000000L);
		when(mockSession.getUser()).thenReturn(new Users());
		when(mockSessionRepo.findByToken("test_token")).thenReturn(mockSession);
		when(mockUsersRepo.usersList()).thenReturn(new ArrayList<>());

		Http.RequestBuilder request = new Http.RequestBuilder()
				.method(GET)
				.cookie(Http.Cookie.builder("session_token", "test_token")
						.withMaxAge(Duration.ofMinutes(1)).build())
				.uri(routes.HomeController.index().url());

		Result result = route(app, request);
		assertEquals(OK, result.status());
		verify(mockUsersRepo).usersList();
		assertTrue(contentAsString(result).contains("Registered users"));
	}
}
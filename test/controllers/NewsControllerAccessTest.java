package controllers;

import controllers.repositories.NewsRepository;
import controllers.repositories.SessionRepository;
import models.Session;
import models.Users;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.SEE_OTHER;
import static play.test.Helpers.*;

public class NewsControllerAccessTest extends WithApplication {

	@Override
	protected Application provideApplication() {
		NewsRepository mockNewsRepository = mock(NewsRepository.class);
		SessionRepository mockSessionRepository = mock(SessionRepository.class);
		Session mockSession = mock(Session.class);

		when(mockSession.getUser()).thenReturn(new Users());
		when(mockSession.getExpirationDate()).thenReturn(System.currentTimeMillis() + 1000000L);
		when(mockSessionRepository.findByToken("active_token")).thenReturn(mockSession);

		return new GuiceApplicationBuilder()
				.overrides(bind(SessionRepository.class).toInstance(mockSessionRepository))
				.overrides(bind(NewsRepository.class).toInstance(mockNewsRepository))
				.build();
	}

	@Test
	public void accessNewsAuthorized() {
		Result result = route(app, fakeRequest()
				.method(GET)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.uri(routes.NewsController.news().url()));
		assertEquals(OK, result.status());
	}

	@Test
	public void accessLoadNewsAuthorized() {
		Result result = route(app, fakeRequest()
				.method(GET)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.uri(routes.NewsController.loadNews(20, 0).url()));
		assertEquals(OK, result.status());
	}

	@Test
	public void accessNewsUnauthorized() {
		Result result = route(app, fakeRequest()
				.method(GET)
				.uri(routes.NewsController.news().url()));
		assertEquals(SEE_OTHER, result.status());
	}

	@Test
	public void accessLoadNewsUnauthorized() {
		Result result = route(app, fakeRequest()
				.method(GET)
				.uri(routes.NewsController.loadNews(20, 0).url()));
		assertEquals(SEE_OTHER, result.status());
	}
}
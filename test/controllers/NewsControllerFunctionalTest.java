package controllers;

import controllers.repositories.NewsRepository;
import controllers.repositories.SessionRepository;
import models.News;
import models.Session;
import models.Users;
import org.junit.Test;
import play.Application;
import play.Logger;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

public class NewsControllerFunctionalTest extends WithApplication {

	private NewsRepository mockNewsRepository;

	@Override
	protected Application provideApplication() {
		mockNewsRepository = mock(NewsRepository.class);
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
	public void test_open_news_page() {
		Result result = route(app, fakeRequest()
				.method(GET)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.uri(routes.NewsController.news().url()));
		assertEquals(OK, result.status());
		assertTrue(contentAsString(result).contains("Xakep news"));
	}

	@Test
	public void test_load_news() {
		when(mockNewsRepository.getNews(0, 2)).thenReturn(Arrays.asList(
				new News().setTitle("A").setDescription("a").setImageUrl("a").setUrl("a")
						.setId(UUID.fromString("6bae2388-da35-4b88-91f4-76d9e836c45a")),
				new News().setTitle("B").setDescription("b").setImageUrl("b").setUrl("b")
						.setId(UUID.fromString("1fe1d280-32b3-4588-b86f-14348df2a5d6"))
		));
		Result result = route(app, fakeRequest()
				.method(GET)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.uri(routes.NewsController.loadNews(2, 0).url()));
		assertEquals(OK, result.status());
		Logger.debug(contentAsString(result));
		assertTrue(contentAsString(result).equals("[{\"id\":\"6bae2388-da35-4b88-91f4-76d9e836c45a\",\"url\":\"a\"," +
				"\"imageUrl\":\"a\",\"title\":\"A\",\"description\":\"a\"},{\"id\":\"1fe1d280-32b3-4588-b86f-14348d" +
				"f2a5d6\",\"url\":\"b\",\"imageUrl\":\"b\",\"title\":\"B\",\"description\":\"b\"}]"));
	}
}
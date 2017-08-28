package controllers;

import controllers.repositories.SessionRepository;
import controllers.repositories.UsersRepository;
import controllers.utils.FileUploader;
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
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.SEE_OTHER;
import static play.test.Helpers.*;

public class ProfileEditorControllerAccessTest extends WithApplication {

	@Override
	protected Application provideApplication() {
		SessionRepository mockSessionRepository = mock(SessionRepository.class);
		Session mockSession = mock(Session.class);

		when(mockSession.getUser()).thenReturn(new Users());
		when(mockSession.getExpirationDate()).thenReturn(System.currentTimeMillis() + 1000000L);
		when(mockSessionRepository.findByToken("active_token")).thenReturn(mockSession);

		return new GuiceApplicationBuilder()
				.overrides(bind(SessionRepository.class).toInstance(mockSessionRepository))
				.overrides(bind(UsersRepository.class).toInstance(mock(UsersRepository.class)))
				.overrides(bind(FileUploader.class).toInstance(mock(FileUploader.class)))
				.build();
	}

	@Test
	public void accessProfileEditorAuthorized() {
		Result result = route(app, fakeRequest()
				.method(GET)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.uri(routes.ProfileEditorController.profileEditor().url()));
		assertEquals(OK, result.status());
	}

	@Test
	public void accessEditAuthorized() {
		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.uri(routes.ProfileEditorController.edit().url()));
		assertEquals(BAD_REQUEST, result.status());
	}

	@Test
	public void accessProfileEditorUnauthorized() {
		Result result = route(app, fakeRequest()
				.method(GET)
				.uri(routes.ProfileEditorController.profileEditor().url()));
		assertEquals(SEE_OTHER, result.status());
	}

	@Test
	public void accessEditUnauthorized() {
		Result result = route(app, fakeRequest()
				.method(POST)
				.uri(routes.ProfileEditorController.edit().url()));
		assertEquals(SEE_OTHER, result.status());
	}
}
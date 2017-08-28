package controllers;

import controllers.repositories.CardRepository;
import controllers.repositories.S3FileRepository;
import controllers.repositories.SessionRepository;
import controllers.utils.ImageMagickUtils;
import models.Session;
import models.Users;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

public class CardsControllerAccessTest extends WithApplication {

	@Override
	protected Application provideApplication() {
		CardRepository mockCardRepository = mock(CardRepository.class);
		ImageMagickUtils mockMagickService = mock(ImageMagickUtils.class);
		S3FileRepository mockS3FileRepository = mock(S3FileRepository.class);
		SessionRepository mockSessionRepository = mock(SessionRepository.class);
		Session mockSession = mock(Session.class);

		when(mockSession.getUser()).thenReturn(new Users());
		when(mockSession.getExpirationDate()).thenReturn(System.currentTimeMillis() + 1000000L);
		when(mockSessionRepository.findByToken("active_token")).thenReturn(mockSession);

		return new GuiceApplicationBuilder()
				.overrides(bind(CardRepository.class).toInstance(mockCardRepository))
				.overrides(bind(ImageMagickUtils.class).toInstance(mockMagickService))
				.overrides(bind(S3FileRepository.class).toInstance(mockS3FileRepository))
				.overrides(bind(SessionRepository.class).toInstance(mockSessionRepository))
				.build();
	}

	@Test
	public void accept_authorized_user_on_cards_page_request() {
		Result result = route(app, fakeRequest()
				.method(GET)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.uri(routes.CardsController.cards().url()));
		assertEquals(OK, result.status());
	}

	@Test
	public void accept_authorized_user_on_cards_list_request() {
		Result result = route(app, fakeRequest()
				.method(GET)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.uri(routes.CardsController.loadCards().url()));
		assertEquals(OK, result.status());
	}

	@Test
	public void accept_authorized_user_on_add_card_request() {
		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.uri(routes.CardsController.addCard().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).equals("Missing fields."));
	}

	@Test
	public void accept_authorized_user_on_delete_card_request() {
		Result result = route(app, fakeRequest()
				.method(DELETE)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.uri(routes.CardsController.deleteCard("").url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).equals("Wrong card UUID"));
	}

	@Test
	public void redirect_unauthorized_user_to_home_page_on_cards_page_request() {
		Result result = route(app, fakeRequest()
				.method(GET)
				.uri(routes.CardsController.cards().url()));
		assertEquals(SEE_OTHER, result.status());
	}

	@Test
	public void redirect_unauthorized_user_to_home_page_on_cards_list_request() {
		Result result = route(app, fakeRequest()
				.method(GET)
				.uri(routes.CardsController.loadCards().url()));
		assertEquals(SEE_OTHER, result.status());
	}

	@Test
	public void redirect_unauthorized_user_to_home_page_on_add_card_request() {
		Result result = route(app, fakeRequest()
				.method(POST)
				.uri(routes.CardsController.addCard().url()));
		assertEquals(SEE_OTHER, result.status());
	}

	@Test
	public void redirect_unauthorized_user_to_home_page_on_delete_card_request() {
		Result result = route(app, fakeRequest()
				.method(DELETE)
				.uri(routes.CardsController.deleteCard("").url()));
		assertEquals(SEE_OTHER, result.status());
	}
}

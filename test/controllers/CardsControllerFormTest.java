package controllers;

import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import controllers.repositories.CardRepository;
import controllers.repositories.SessionRepository;
import controllers.utils.FileUploadUtils;
import models.Card;
import models.Session;
import models.Users;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Files;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;
import static play.test.Helpers.*;

public class CardsControllerFormTest extends WithApplication {

	private CardRepository mockCardRepository;
	private Users mockUser;

	@Override
	protected Application provideApplication() {
		mockCardRepository = mock(CardRepository.class);
		mockUser = mock(Users.class);
		SessionRepository mockSessionRepository = mock(SessionRepository.class);
		Session mockSession = mock(Session.class);
		FileUploadUtils mockFileUploadUtils = mock(FileUploadUtils.class);

		when(mockUser.getUserId()).thenReturn(UUID.randomUUID());
		when(mockSession.getUser()).thenReturn(mockUser);
		when(mockSession.getExpirationDate()).thenReturn(System.currentTimeMillis() + 1000000L);
		when(mockSessionRepository.findByToken("active_token")).thenReturn(mockSession);

		return new GuiceApplicationBuilder()
				.overrides(bind(CardRepository.class).toInstance(mockCardRepository))
				.overrides(bind(FileUploadUtils.class).toInstance(mockFileUploadUtils))
				.overrides(bind(SessionRepository.class).toInstance(mockSessionRepository))
				.build();
	}

	@Test
	public void add_card_success() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> titlePart =
				new Http.MultipartFormData.DataPart("title", "Valid title");
		Http.MultipartFormData.Part<Source<ByteString, ?>> contentPart =
				new Http.MultipartFormData.DataPart("content", "Lorem ipsum dolor sit amet");
		Http.MultipartFormData.Part<Source<ByteString, ?>> filePart = new Http.MultipartFormData.FilePart<>("key",
				"fileName", "application/octet-stream", Source.empty());
		List<Http.MultipartFormData.Part<Source<ByteString, ?>>> data = Arrays.asList(titlePart, contentPart, filePart);

		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.bodyMultipart(data, Files.singletonTemporaryFileCreator(), mat)
				.uri(routes.CardsController.addCard().url()));
		assertEquals(OK, result.status());
	}

	@Test
	public void add_card_failed_missed_title() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> contentPart =
				new Http.MultipartFormData.DataPart("content", "Lorem ipsum dolor sit amet");
		Http.MultipartFormData.Part<Source<ByteString, ?>> filePart = new Http.MultipartFormData.FilePart<>("key",
				"fileName", "application/octet-stream", Source.empty());
		List<Http.MultipartFormData.Part<Source<ByteString, ?>>> data = Arrays.asList(contentPart, filePart);

		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.bodyMultipart(data, Files.singletonTemporaryFileCreator(), mat)
				.uri(routes.CardsController.addCard().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).equals("Missing fields."));
	}

	@Test
	public void add_card_failed_empty_title() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> titlePart =
				new Http.MultipartFormData.DataPart("title", "");
		Http.MultipartFormData.Part<Source<ByteString, ?>> contentPart =
				new Http.MultipartFormData.DataPart("content", "Lorem ipsum dolor sit amet");
		Http.MultipartFormData.Part<Source<ByteString, ?>> filePart = new Http.MultipartFormData.FilePart<>("key",
				"fileName", "application/octet-stream", Source.empty());
		List<Http.MultipartFormData.Part<Source<ByteString, ?>>> data = Arrays.asList(titlePart, contentPart, filePart);

		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.bodyMultipart(data, Files.singletonTemporaryFileCreator(), mat)
				.uri(routes.CardsController.addCard().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).equals("Title and content cannot be empty."));
	}

	@Test
	public void add_card_failed_missed_content() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> titlePart =
				new Http.MultipartFormData.DataPart("title", "Valid title");
		Http.MultipartFormData.Part<Source<ByteString, ?>> filePart = new Http.MultipartFormData.FilePart<>("key",
				"fileName", "application/octet-stream", Source.empty());
		List<Http.MultipartFormData.Part<Source<ByteString, ?>>> data = Arrays.asList(titlePart, filePart);

		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.bodyMultipart(data, Files.singletonTemporaryFileCreator(), mat)
				.uri(routes.CardsController.addCard().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).equals("Missing fields."));
	}

	@Test
	public void add_card_failed_empty_content() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> titlePart =
				new Http.MultipartFormData.DataPart("title", "Valid title");
		Http.MultipartFormData.Part<Source<ByteString, ?>> contentPart =
				new Http.MultipartFormData.DataPart("content", "");
		Http.MultipartFormData.Part<Source<ByteString, ?>> filePart = new Http.MultipartFormData.FilePart<>("key",
				"fileName", "application/octet-stream", Source.empty());
		List<Http.MultipartFormData.Part<Source<ByteString, ?>>> data = Arrays.asList(titlePart, contentPart, filePart);

		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.bodyMultipart(data, Files.singletonTemporaryFileCreator(), mat)
				.uri(routes.CardsController.addCard().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).equals("Title and content cannot be empty."));
	}

	@Test
	public void add_card_failed_more_than_5_files() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> titlePart =
				new Http.MultipartFormData.DataPart("title", "Valid title");
		Http.MultipartFormData.Part<Source<ByteString, ?>> contentPart =
				new Http.MultipartFormData.DataPart("content", "Lorem ipsum dolor sit amet");
		Http.MultipartFormData.Part<Source<ByteString, ?>> filePart1 = new Http.MultipartFormData.FilePart<>("key1",
				"fileName1", "application/octet-stream", Source.empty());
		Http.MultipartFormData.Part<Source<ByteString, ?>> filePart2 = new Http.MultipartFormData.FilePart<>("key2",
				"fileName2", "application/octet-stream", Source.empty());
		Http.MultipartFormData.Part<Source<ByteString, ?>> filePart3 = new Http.MultipartFormData.FilePart<>("key3",
				"fileName3", "application/octet-stream", Source.empty());
		Http.MultipartFormData.Part<Source<ByteString, ?>> filePart4 = new Http.MultipartFormData.FilePart<>("key4",
				"fileName4", "application/octet-stream", Source.empty());
		Http.MultipartFormData.Part<Source<ByteString, ?>> filePart5 = new Http.MultipartFormData.FilePart<>("key5",
				"fileName5", "application/octet-stream", Source.empty());
		Http.MultipartFormData.Part<Source<ByteString, ?>> filePart6 = new Http.MultipartFormData.FilePart<>("key6",
				"fileName6", "application/octet-stream", Source.empty());
		List<Http.MultipartFormData.Part<Source<ByteString, ?>>> data = Arrays.asList(titlePart, contentPart,
				filePart1, filePart2, filePart3, filePart4, filePart5, filePart6);

		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.bodyMultipart(data, Files.singletonTemporaryFileCreator(), mat)
				.uri(routes.CardsController.addCard().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).equals("Can't load more than 5 files in one card."));
	}

	@Test
	public void add_card_failed_too_large_files() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> titlePart =
				new Http.MultipartFormData.DataPart("title", "Valid title");
		Http.MultipartFormData.Part<Source<ByteString, ?>> contentPart =
				new Http.MultipartFormData.DataPart("content", "Lorem ipsum dolor sit amet");
		Http.MultipartFormData.Part<Source<ByteString, ?>> filePart = new Http.MultipartFormData.FilePart<>("key",
				"fileName", "application/octet-stream", FileIO.fromFile(new File("bigimage.bmp")));
		List<Http.MultipartFormData.Part<Source<ByteString, ?>>> data = Arrays.asList(titlePart, contentPart, filePart);

		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.bodyMultipart(data, Files.singletonTemporaryFileCreator(), mat)
				.uri(routes.CardsController.addCard().url()));
		assertEquals(REQUEST_ENTITY_TOO_LARGE, result.status());
	}

	@Test
	public void add_card_failed_file_not_a_image() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> titlePart =
				new Http.MultipartFormData.DataPart("title", "Valid title");
		Http.MultipartFormData.Part<Source<ByteString, ?>> contentPart =
				new Http.MultipartFormData.DataPart("content", "Lorem ipsum dolor sit amet");
		Http.MultipartFormData.Part<Source<ByteString, ?>> filePart = new Http.MultipartFormData.FilePart<>("key",
				"fileName", "application/octet-stream", FileIO.fromFile(new File("notimage.txt")));
		List<Http.MultipartFormData.Part<Source<ByteString, ?>>> data = Arrays.asList(titlePart, contentPart, filePart);

		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.bodyMultipart(data, Files.singletonTemporaryFileCreator(), mat)
				.uri(routes.CardsController.addCard().url()));
		assertEquals(BAD_REQUEST, result.status());
	}

	@Test
	public void delete_card_failed_missed_id() {
		Result result = route(app, fakeRequest()
				.method(DELETE)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.uri(routes.CardsController.deleteCard("").url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).equals("Wrong card UUID"));
	}

	@Test
	public void delete_card_failed_invalid_id() {
		Result result = route(app, fakeRequest()
				.method(DELETE)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.uri(routes.CardsController.deleteCard("550e8400-e29b-41d4-a716").url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).equals("Wrong card UUID"));
	}

	@Test
	public void delete_card_failed_foreign_card() {
		Card mockCard = mock(Card.class);
		Users mockOtherUser = mock(Users.class);
		when(mockOtherUser.getUserId()).thenReturn(UUID.randomUUID());
		when(mockCard.getOwner()).thenReturn(mockOtherUser);
		when(mockCardRepository.findCardById(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))).thenReturn(mockCard);

		Result result = route(app, fakeRequest()
				.method(DELETE)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.uri(routes.CardsController.deleteCard("550e8400-e29b-41d4-a716-446655440000").url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).equals("Wrong card UUID"));
	}

	@Test
	public void delete_card_success() {
		Card mockCard = mock(Card.class);
		when(mockCard.getOwner()).thenReturn(mockUser);
		when(mockCardRepository.findCardById(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))).thenReturn(mockCard);

		Result result = route(app, fakeRequest()
				.method(DELETE)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.uri(routes.CardsController.deleteCard("550e8400-e29b-41d4-a716-446655440000").url()));
		assertEquals(OK, result.status());
	}
}
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
import org.mockito.ArgumentCaptor;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Files;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

public class CardsControllerFunctionalTest extends WithApplication {

	private CardRepository mockCardRepository;
	private FileUploadUtils mockFileUploadUtils;

	private Users mockMyUser;

	@Override
	protected Application provideApplication() {
		mockFileUploadUtils = mock(FileUploadUtils.class);
		when(mockFileUploadUtils.uploadImageAndShrink(any(File.class), anyInt()))
				.thenReturn("a").thenReturn("b").thenReturn("c").thenReturn("d").thenReturn("e");

		mockMyUser = mock(Users.class);
		when(mockMyUser.getUserId()).thenReturn(UUID.randomUUID());

		Session mockMySession = mock(Session.class);
		when(mockMySession.getExpirationDate()).thenReturn(System.currentTimeMillis() + 1000000L);
		when(mockMySession.getUser()).thenReturn(mockMyUser);

		SessionRepository mockSessionRepository = mock(SessionRepository.class);
		when(mockSessionRepository.findByToken("my_token")).thenReturn(mockMySession);

		mockCardRepository = mock(CardRepository.class);

		return new GuiceApplicationBuilder()
				.overrides(bind(CardRepository.class).toInstance(mockCardRepository))
				.overrides(bind(FileUploadUtils.class).toInstance(mockFileUploadUtils))
				.overrides(bind(SessionRepository.class).toInstance(mockSessionRepository))
				.build();
	}

	@Test
	public void cards_loading_test() {
		when(mockCardRepository.findUsersCard(mockMyUser.getUserId())).thenReturn(
				Arrays.asList(
						new Card().setTitle("My card 1").setContent("1").setImages(Collections.emptyList())
								.setOwner(mockMyUser).setId(UUID.fromString("42a651db-77aa-4884-ac74-f2f4b49601da")),
						new Card().setTitle("My card 2").setContent("2").setImages(Arrays.asList("a"))
								.setOwner(mockMyUser).setId(UUID.fromString("656e7592-8c95-48c6-894f-8297af485c2f")),
						new Card().setTitle("My card 3").setContent("3").setImages(Arrays.asList("a", "b", "c"))
								.setOwner(mockMyUser).setId(UUID.fromString("b7273c84-7bc0-4e83-b4d2-1a1954d575b5")),
						new Card().setTitle("My card 4").setContent("4").setImages(Arrays.asList("a", "b", "c", "d", "e"))
								.setOwner(mockMyUser).setId(UUID.fromString("259d4b4f-a750-4a7b-b188-cff9770d90c8"))
				)
		);

		Result result = route(app, fakeRequest()
				.method(GET)
				.cookie(Http.Cookie.builder("session_token", "my_token").build())
				.uri(routes.CardsController.loadCards().url()));
		assertEquals(OK, result.status());
		assertEquals("[{\"id\":\"42a651db-77aa-4884-ac74-f2f4b49601da\",\"title\":\"My card 1\",\"content\":" +
				"\"1\",\"images\":[]},{\"id\":\"656e7592-8c95-48c6-894f-8297af485c2f\",\"title\":\"My card 2\"," +
				"\"content\":\"2\",\"images\":[\"a\"]},{\"id\":\"b7273c84-7bc0-4e83-b4d2-1a1954d575b5\",\"title\":" +
				"\"My card 3\",\"content\":\"3\",\"images\":[\"a\",\"b\",\"c\"]},{\"id\":\"259d4b4f-a750-4a7b-b188-cf" +
				"f9770d90c8\",\"title\":\"My card 4\",\"content\":\"4\",\"images\":[\"a\",\"b\",\"c\",\"d\",\"e\"]}]",
				contentAsString(result));
	}

	@Test
	public void test_adding_card_with_no_images() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> titlePart =
				new Http.MultipartFormData.DataPart("title", "Valid title");
		Http.MultipartFormData.Part<Source<ByteString, ?>> contentPart =
				new Http.MultipartFormData.DataPart("content", "Lorem ipsum dolor sit amet");
		List<Http.MultipartFormData.Part<Source<ByteString, ?>>> data = Arrays.asList(titlePart, contentPart);

		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "my_token").build())
				.bodyMultipart(data, Files.singletonTemporaryFileCreator(), mat)
				.uri(routes.CardsController.addCard().url()));
		assertEquals(OK, result.status());
		assertEquals("{\"id\":null,\"title\":\"Valid title\",\"content\":\"Lorem ipsum dolor sit amet\"," +
				"\"images\":[]}", contentAsString(result));
		ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
		verify(mockCardRepository, times(1)).saveCard(cardCaptor.capture());
		Card savedCard = cardCaptor.getValue();
		assertEquals(savedCard.getOwner().getUserId(), mockMyUser.getUserId());
		assertEquals(savedCard.getTitle(), "Valid title");
		assertEquals(savedCard.getContent(), "Lorem ipsum dolor sit amet");
		assertEquals(savedCard.getImages().size(), 0);
	}

	@Test
	public void test_adding_card_with_one_image() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> titlePart =
				new Http.MultipartFormData.DataPart("title", "Valid title");
		Http.MultipartFormData.Part<Source<ByteString, ?>> contentPart =
				new Http.MultipartFormData.DataPart("content", "Lorem ipsum dolor sit amet");
		Http.MultipartFormData.Part<Source<ByteString, ?>> img1 = new Http.MultipartFormData.FilePart<>("image1",
				"image1", "application/octet-stream", FileIO.fromFile(new File("testimage.jpg")));
		List<Http.MultipartFormData.Part<Source<ByteString, ?>>> data = Arrays.asList(titlePart, contentPart, img1);

		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "my_token").build())
				.bodyMultipart(data, Files.singletonTemporaryFileCreator(), mat)
				.uri(routes.CardsController.addCard().url()));
		assertEquals(OK, result.status());
		assertEquals("{\"id\":null,\"title\":\"Valid title\",\"content\":\"Lorem ipsum dolor sit amet\"," +
				"\"images\":[\"a\"]}", contentAsString(result));
		ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
		verify(mockFileUploadUtils, times(1)).uploadImageAndShrink(any(File.class), anyInt());
		verify(mockCardRepository, times(1)).saveCard(cardCaptor.capture());
		Card savedCard = cardCaptor.getValue();
		assertEquals(savedCard.getOwner().getUserId(), mockMyUser.getUserId());
		assertEquals(savedCard.getTitle(), "Valid title");
		assertEquals(savedCard.getContent(), "Lorem ipsum dolor sit amet");
		assertEquals(savedCard.getImages().size(), 1);
	}

	@Test
	public void test_adding_card_with_three_images() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> titlePart =
				new Http.MultipartFormData.DataPart("title", "Valid title");
		Http.MultipartFormData.Part<Source<ByteString, ?>> contentPart =
				new Http.MultipartFormData.DataPart("content", "Lorem ipsum dolor sit amet");
		Http.MultipartFormData.Part<Source<ByteString, ?>> img1 = new Http.MultipartFormData.FilePart<>("image1",
				"image1", "application/octet-stream", FileIO.fromFile(new File("testimage.jpg")));
		Http.MultipartFormData.Part<Source<ByteString, ?>> img2 = new Http.MultipartFormData.FilePart<>("image2",
				"image2", "application/octet-stream", FileIO.fromFile(new File("testimage.jpg")));
		Http.MultipartFormData.Part<Source<ByteString, ?>> img3 = new Http.MultipartFormData.FilePart<>("image3",
				"image3", "application/octet-stream", FileIO.fromFile(new File("testimage.jpg")));
		List<Http.MultipartFormData.Part<Source<ByteString, ?>>> data = Arrays.asList(titlePart, contentPart, img1, img2, img3);

		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "my_token").build())
				.bodyMultipart(data, Files.singletonTemporaryFileCreator(), mat)
				.uri(routes.CardsController.addCard().url()));
		assertEquals(OK, result.status());
		assertEquals("{\"id\":null,\"title\":\"Valid title\",\"content\":\"Lorem ipsum dolor sit amet\"," +
				"\"images\":[\"a\",\"b\",\"c\"]}", contentAsString(result));
		ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
		verify(mockFileUploadUtils, times(3)).uploadImageAndShrink(any(File.class), anyInt());
		verify(mockCardRepository, times(1)).saveCard(cardCaptor.capture());
		Card savedCard = cardCaptor.getValue();
		assertEquals(savedCard.getOwner().getUserId(), mockMyUser.getUserId());
		assertEquals(savedCard.getTitle(), "Valid title");
		assertEquals(savedCard.getContent(), "Lorem ipsum dolor sit amet");
		assertEquals(savedCard.getImages().size(), 3);
	}

	@Test
	public void test_adding_card_with_five_images() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> titlePart =
				new Http.MultipartFormData.DataPart("title", "Valid title");
		Http.MultipartFormData.Part<Source<ByteString, ?>> contentPart =
				new Http.MultipartFormData.DataPart("content", "Lorem ipsum dolor sit amet");
		Http.MultipartFormData.Part<Source<ByteString, ?>> img1 = new Http.MultipartFormData.FilePart<>("image1",
				"image1", "application/octet-stream", FileIO.fromFile(new File("testimage.jpg")));
		Http.MultipartFormData.Part<Source<ByteString, ?>> img2 = new Http.MultipartFormData.FilePart<>("image2",
				"image2", "application/octet-stream", FileIO.fromFile(new File("testimage.jpg")));
		Http.MultipartFormData.Part<Source<ByteString, ?>> img3 = new Http.MultipartFormData.FilePart<>("image3",
				"image3", "application/octet-stream", FileIO.fromFile(new File("testimage.jpg")));
		Http.MultipartFormData.Part<Source<ByteString, ?>> img4 = new Http.MultipartFormData.FilePart<>("image4",
				"image4", "application/octet-stream", FileIO.fromFile(new File("testimage.jpg")));
		Http.MultipartFormData.Part<Source<ByteString, ?>> img5 = new Http.MultipartFormData.FilePart<>("image5",
				"image5", "application/octet-stream", FileIO.fromFile(new File("testimage.jpg")));
		List<Http.MultipartFormData.Part<Source<ByteString, ?>>> data = Arrays.asList(titlePart, contentPart, img1, img2, img3, img4, img5);

		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "my_token").build())
				.bodyMultipart(data, Files.singletonTemporaryFileCreator(), mat)
				.uri(routes.CardsController.addCard().url()));
		assertEquals(OK, result.status());
		assertEquals("{\"id\":null,\"title\":\"Valid title\",\"content\":\"Lorem ipsum dolor sit amet\"," +
				"\"images\":[\"a\",\"b\",\"c\",\"d\",\"e\"]}", contentAsString(result));
		ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
		verify(mockFileUploadUtils, times(5)).uploadImageAndShrink(any(File.class), anyInt());
		verify(mockCardRepository, times(1)).saveCard(cardCaptor.capture());
		Card savedCard = cardCaptor.getValue();
		assertEquals(savedCard.getOwner().getUserId(), mockMyUser.getUserId());
		assertEquals(savedCard.getTitle(), "Valid title");
		assertEquals(savedCard.getContent(), "Lorem ipsum dolor sit amet");
		assertEquals(savedCard.getImages().size(), 5);
	}

	@Test
	public void test_delete_owners_card() {
		when(mockCardRepository.findCardById(UUID.fromString("42a651db-77aa-4884-ac74-f2f4b49601da")))
				.thenReturn(new Card().setTitle("My card").setContent("1").setImages(Collections.emptyList())
						.setOwner(mockMyUser).setId(UUID.fromString("42a651db-77aa-4884-ac74-f2f4b49601da")));

		Result result = route(app, fakeRequest()
				.method(DELETE)
				.cookie(Http.Cookie.builder("session_token", "my_token").build())
				.uri(routes.CardsController.deleteCard("42a651db-77aa-4884-ac74-f2f4b49601da").url()));
		assertEquals(OK, result.status());
		ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
		verify(mockCardRepository, times(1)).deleteCard(cardCaptor.capture());
		assertEquals(UUID.fromString("42a651db-77aa-4884-ac74-f2f4b49601da"), cardCaptor.getValue().getId());
	}

	@Test
	public void test_delete_foreign_card() {
		when(mockCardRepository.findCardById(UUID.fromString("42a651db-77aa-4884-ac74-f2f4b49601da")))
				.thenReturn(new Card().setTitle("Not my card").setContent("1").setImages(Collections.emptyList())
						.setOwner(new Users().setUserId(UUID.randomUUID()))
						.setId(UUID.fromString("42a651db-77aa-4884-ac74-f2f4b49601da")));

		Result result = route(app, fakeRequest()
				.method(DELETE)
				.cookie(Http.Cookie.builder("session_token", "my_token").build())
				.uri(routes.CardsController.deleteCard("42a651db-77aa-4884-ac74-f2f4b49601da").url()));
		assertEquals(BAD_REQUEST, result.status());
		assertEquals("Wrong card UUID", contentAsString(result));
		verify(mockCardRepository, never()).deleteCard(any(Card.class));
	}

	@Test
	public void test_delete_non_existent_card() {
		when(mockCardRepository.findCardById(UUID.fromString("42a651db-77aa-4884-ac74-f2f4b49601da"))).thenReturn(null);

		Result result = route(app, fakeRequest()
				.method(DELETE)
				.cookie(Http.Cookie.builder("session_token", "my_token").build())
				.uri(routes.CardsController.deleteCard("42a651db-77aa-4884-ac74-f2f4b49601da").url()));
		assertEquals(BAD_REQUEST, result.status());
		assertEquals("Wrong card UUID", contentAsString(result));
		verify(mockCardRepository, never()).deleteCard(any(Card.class));
	}
}
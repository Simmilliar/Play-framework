package controllers;

import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import controllers.utils.FileUploader;
import controllers.utils.Utils;
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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.SEE_OTHER;
import static play.test.Helpers.*;

public class ProfileEditorControllerFunctionalTest extends WithApplication {

	private UsersRepository mockUsersRepository;

	@Override
	protected Application provideApplication() {
		FileUploader mockFileUploader = mock(FileUploader.class);
		mockUsersRepository = mock(UsersRepository.class);
		SessionRepository mockSessionRepository = mock(SessionRepository.class);
		Session mockSession = mock(Session.class);

		when(mockFileUploader.uploadImageAndCropSquared(any(File.class), anyInt())).thenReturn("a");
		when(mockSession.getUser()).thenReturn(new Users().setName("Yaroslav").setEmail("valid@email.com")
				.setAvatarUrl("b").setPasswordSalt("12345").setPasswordHash(new Utils()
						.hashString("longEnoughPassword","12345")).setConfirmed(true));
		when(mockSession.getExpirationDate()).thenReturn(System.currentTimeMillis() + 1000000L);
		when(mockSessionRepository.findByToken("active_token")).thenReturn(mockSession);

		return new GuiceApplicationBuilder()
				.overrides(bind(SessionRepository.class).toInstance(mockSessionRepository))
				.overrides(bind(UsersRepository.class).toInstance(mockUsersRepository))
				.overrides(bind(FileUploader.class).toInstance(mockFileUploader))
				.build();
	}

	@Test
	public void changeAll() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> namePart =
				new Http.MultipartFormData.DataPart("name", "Anatoliy");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordPart =
				new Http.MultipartFormData.DataPart("password", "shortEnoughPassword");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordConfirmPart =
				new Http.MultipartFormData.DataPart("passwordConfirm", "shortEnoughPassword");
		Http.MultipartFormData.Part<Source<ByteString, ?>> filePart = new Http.MultipartFormData.FilePart<>("avatarFile",
				"avatarFile", "application/octet-stream", FileIO.fromFile(
				new File("testimage.jpg")));
		List<Http.MultipartFormData.Part<Source<ByteString, ?>>> data = Arrays.asList(namePart, passwordPart,
				passwordConfirmPart, filePart);

		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.bodyMultipart(data, Files.singletonTemporaryFileCreator(), mat)
				.uri(routes.ProfileEditorController.edit().url()));
		assertEquals(SEE_OTHER, result.status());
		ArgumentCaptor<Users> usersCaptor = ArgumentCaptor.forClass(Users.class);
		verify(mockUsersRepository, times(1)).saveUser(usersCaptor.capture());
		Users savedUser = usersCaptor.getValue();
		assertEquals("a", savedUser.getAvatarUrl());
		assertEquals("Anatoliy", savedUser.getName());
		assertNotEquals("12345", savedUser.getPasswordSalt());
		assertEquals(new Utils().hashString("shortEnoughPassword", savedUser.getPasswordSalt()), savedUser.getPasswordHash());
	}

	@Test
	public void changeName() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> namePart =
				new Http.MultipartFormData.DataPart("name", "Anatoliy");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordPart =
				new Http.MultipartFormData.DataPart("password", "");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordConfirmPart =
				new Http.MultipartFormData.DataPart("passwordConfirm", "");
		Http.MultipartFormData.Part<Source<ByteString, ?>> filePart = new Http.MultipartFormData.FilePart<>("avatarFile",
				"avatarFile", "application/octet-stream", Source.empty());
		List<Http.MultipartFormData.Part<Source<ByteString, ?>>> data = Arrays.asList(namePart, passwordPart,
				passwordConfirmPart, filePart);

		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.bodyMultipart(data, Files.singletonTemporaryFileCreator(), mat)
				.uri(routes.ProfileEditorController.edit().url()));
		assertEquals(SEE_OTHER, result.status());
		ArgumentCaptor<Users> usersCaptor = ArgumentCaptor.forClass(Users.class);
		verify(mockUsersRepository, times(1)).saveUser(usersCaptor.capture());
		Users savedUser = usersCaptor.getValue();
		assertEquals("b", savedUser.getAvatarUrl());
		assertEquals("Anatoliy", savedUser.getName());
		assertEquals("12345", savedUser.getPasswordSalt());
		assertEquals(new Utils().hashString("longEnoughPassword", "12345"), savedUser.getPasswordHash());
	}

	@Test
	public void changePassword() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> namePart =
				new Http.MultipartFormData.DataPart("name", "Yaroslav");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordPart =
				new Http.MultipartFormData.DataPart("password", "shortEnoughPassword");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordConfirmPart =
				new Http.MultipartFormData.DataPart("passwordConfirm", "shortEnoughPassword");
		Http.MultipartFormData.Part<Source<ByteString, ?>> filePart = new Http.MultipartFormData.FilePart<>("avatarFile",
				"avatarFile", "application/octet-stream", Source.empty());
		List<Http.MultipartFormData.Part<Source<ByteString, ?>>> data = Arrays.asList(namePart, passwordPart,
				passwordConfirmPart, filePart);

		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.bodyMultipart(data, Files.singletonTemporaryFileCreator(), mat)
				.uri(routes.ProfileEditorController.edit().url()));
		assertEquals(SEE_OTHER, result.status());
		ArgumentCaptor<Users> usersCaptor = ArgumentCaptor.forClass(Users.class);
		verify(mockUsersRepository, times(1)).saveUser(usersCaptor.capture());
		Users savedUser = usersCaptor.getValue();
		assertEquals("b", savedUser.getAvatarUrl());
		assertEquals("Yaroslav", savedUser.getName());
		assertNotEquals("12345", savedUser.getPasswordSalt());
		assertEquals(new Utils().hashString("shortEnoughPassword", savedUser.getPasswordSalt()), savedUser.getPasswordHash());
	}

	@Test
	public void changeAvatar() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> namePart =
				new Http.MultipartFormData.DataPart("name", "Yaroslav");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordPart =
				new Http.MultipartFormData.DataPart("password", "");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordConfirmPart =
				new Http.MultipartFormData.DataPart("passwordConfirm", "");
		Http.MultipartFormData.Part<Source<ByteString, ?>> filePart = new Http.MultipartFormData.FilePart<>("avatarFile",
				"avatarFile", "application/octet-stream", FileIO.fromFile(
				new File("testimage.jpg")));
		List<Http.MultipartFormData.Part<Source<ByteString, ?>>> data = Arrays.asList(namePart, passwordPart,
				passwordConfirmPart, filePart);

		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.bodyMultipart(data, Files.singletonTemporaryFileCreator(), mat)
				.uri(routes.ProfileEditorController.edit().url()));
		assertEquals(SEE_OTHER, result.status());
		ArgumentCaptor<Users> usersCaptor = ArgumentCaptor.forClass(Users.class);
		verify(mockUsersRepository, times(1)).saveUser(usersCaptor.capture());
		Users savedUser = usersCaptor.getValue();
		assertEquals("a", savedUser.getAvatarUrl());
		assertEquals("Yaroslav", savedUser.getName());
		assertEquals("12345", savedUser.getPasswordSalt());
		assertEquals(new Utils().hashString("longEnoughPassword", "12345"), savedUser.getPasswordHash());
	}
}

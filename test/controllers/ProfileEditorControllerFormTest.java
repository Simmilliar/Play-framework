package controllers;

import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import controllers.repositories.SessionRepository;
import controllers.repositories.UsersRepository;
import controllers.utils.FileUploader;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;
import static play.test.Helpers.*;

public class ProfileEditorControllerFormTest extends WithApplication {

	private FileUploader mockFileUploader;

	@Override
	protected Application provideApplication() {
		mockFileUploader = mock(FileUploader.class);
		UsersRepository mockUsersRepository = mock(UsersRepository.class);
		SessionRepository mockSessionRepository = mock(SessionRepository.class);
		Session mockSession = mock(Session.class);

		when(mockFileUploader.uploadImageAndCropSquared(any(File.class), anyInt())).thenReturn("a");
		when(mockSession.getUser()).thenReturn(new Users().setName("Yaroslav"));
		when(mockSession.getExpirationDate()).thenReturn(System.currentTimeMillis() + 1000000L);
		when(mockSessionRepository.findByToken("active_token")).thenReturn(mockSession);

		return new GuiceApplicationBuilder()
				.overrides(bind(SessionRepository.class).toInstance(mockSessionRepository))
				.overrides(bind(UsersRepository.class).toInstance(mockUsersRepository))
				.overrides(bind(FileUploader.class).toInstance(mockFileUploader))
				.build();
	}

	@Test
	public void edit_profile_form_success() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> namePart =
				new Http.MultipartFormData.DataPart("name", "Anatoliy");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordPart =
				new Http.MultipartFormData.DataPart("password", "longEnoughPassword");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordConfirmPart =
				new Http.MultipartFormData.DataPart("passwordConfirm", "longEnoughPassword");
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
	}

	@Test
	public void edit_profile_form_failed_missed_name() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordPart =
				new Http.MultipartFormData.DataPart("password", "longEnoughPassword");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordConfirmPart =
				new Http.MultipartFormData.DataPart("passwordConfirm", "longEnoughPassword");
		Http.MultipartFormData.Part<Source<ByteString, ?>> filePart = new Http.MultipartFormData.FilePart<>("avatarFile",
				"avatarFile", "application/octet-stream", FileIO.fromFile(
				new File("testimage.jpg")));
		List<Http.MultipartFormData.Part<Source<ByteString, ?>>> data = Arrays.asList(passwordPart,
				passwordConfirmPart, filePart);

		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.bodyMultipart(data, Files.singletonTemporaryFileCreator(), mat)
				.uri(routes.ProfileEditorController.edit().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Missing fields."));
	}

	@Test
	public void edit_profile_form_failed_empty_name() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> namePart =
				new Http.MultipartFormData.DataPart("name", "");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordPart =
				new Http.MultipartFormData.DataPart("password", "longEnoughPassword");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordConfirmPart =
				new Http.MultipartFormData.DataPart("passwordConfirm", "longEnoughPassword");
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
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Invalid name."));
	}

	@Test
	public void edit_profile_form_failed_invalid_name() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> namePart =
				new Http.MultipartFormData.DataPart("name", "Inavlid~name");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordPart =
				new Http.MultipartFormData.DataPart("password", "longEnoughPassword");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordConfirmPart =
				new Http.MultipartFormData.DataPart("passwordConfirm", "longEnoughPassword");
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
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Invalid name."));
	}

	@Test
	public void edit_profile_form_failed_missed_password() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> namePart =
				new Http.MultipartFormData.DataPart("name", "Anatoliy");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordConfirmPart =
				new Http.MultipartFormData.DataPart("passwordConfirm", "longEnoughPassword");
		Http.MultipartFormData.Part<Source<ByteString, ?>> filePart = new Http.MultipartFormData.FilePart<>("avatarFile",
				"avatarFile", "application/octet-stream", FileIO.fromFile(
				new File("testimage.jpg")));
		List<Http.MultipartFormData.Part<Source<ByteString, ?>>> data = Arrays.asList(namePart, passwordConfirmPart, filePart);

		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.bodyMultipart(data, Files.singletonTemporaryFileCreator(), mat)
				.uri(routes.ProfileEditorController.edit().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Missing fields."));
	}

	@Test
	public void edit_profile_form_success_empty_password() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> namePart =
				new Http.MultipartFormData.DataPart("name", "Anatoliy");
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
	}

	@Test
	public void edit_profile_form_failed_short_password() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> namePart =
				new Http.MultipartFormData.DataPart("name", "Anatoliy");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordPart =
				new Http.MultipartFormData.DataPart("password", "short");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordConfirmPart =
				new Http.MultipartFormData.DataPart("passwordConfirm", "short");
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
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Password must be at least 8 symbols long."));
	}

	@Test
	public void edit_profile_form_failed_missed_password_confirmation() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> namePart =
				new Http.MultipartFormData.DataPart("name", "Anatoliy");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordPart =
				new Http.MultipartFormData.DataPart("password", "longEnoughPassword");
		Http.MultipartFormData.Part<Source<ByteString, ?>> filePart = new Http.MultipartFormData.FilePart<>("avatarFile",
				"avatarFile", "application/octet-stream", FileIO.fromFile(
				new File("testimage.jpg")));
		List<Http.MultipartFormData.Part<Source<ByteString, ?>>> data = Arrays.asList(namePart, passwordPart, filePart);

		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.bodyMultipart(data, Files.singletonTemporaryFileCreator(), mat)
				.uri(routes.ProfileEditorController.edit().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Missing fields."));
	}

	@Test
	public void edit_profile_form_failed_password_confirmation_not_match() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> namePart =
				new Http.MultipartFormData.DataPart("name", "Anatoliy");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordPart =
				new Http.MultipartFormData.DataPart("password", "longEnoughPassword");
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
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Passwords does not match."));
	}

	@Test
	public void edit_profile_form_failed_avatar_missing() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> namePart =
				new Http.MultipartFormData.DataPart("name", "Anatoliy");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordPart =
				new Http.MultipartFormData.DataPart("password", "longEnoughPassword");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordConfirmPart =
				new Http.MultipartFormData.DataPart("passwordConfirm", "longEnoughPassword");
		List<Http.MultipartFormData.Part<Source<ByteString, ?>>> data = Arrays.asList(namePart, passwordPart,
				passwordConfirmPart);

		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.bodyMultipart(data, Files.singletonTemporaryFileCreator(), mat)
				.uri(routes.ProfileEditorController.edit().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Missing fields."));
	}

	@Test
	public void edit_profile_form_success_avatar_empty() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> namePart =
				new Http.MultipartFormData.DataPart("name", "Anatoliy");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordPart =
				new Http.MultipartFormData.DataPart("password", "longEnoughPassword");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordConfirmPart =
				new Http.MultipartFormData.DataPart("passwordConfirm", "longEnoughPassword");
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
	}

	@Test
	public void edit_profile_form_failed_avatar_file_too_large() {
		Http.MultipartFormData.Part<Source<ByteString, ?>> namePart =
				new Http.MultipartFormData.DataPart("name", "Anatoliy");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordPart =
				new Http.MultipartFormData.DataPart("password", "longEnoughPassword");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordConfirmPart =
				new Http.MultipartFormData.DataPart("passwordConfirm", "longEnoughPassword");
		Http.MultipartFormData.Part<Source<ByteString, ?>> filePart = new Http.MultipartFormData.FilePart<>("avatarFile",
				"avatarFile", "application/octet-stream", FileIO.fromFile(
				new File("bigimage.bmp")));
		List<Http.MultipartFormData.Part<Source<ByteString, ?>>> data = Arrays.asList(namePart, passwordPart,
				passwordConfirmPart, filePart);

		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.bodyMultipart(data, Files.singletonTemporaryFileCreator(), mat)
				.uri(routes.ProfileEditorController.edit().url()));
		assertEquals(REQUEST_ENTITY_TOO_LARGE, result.status());
	}

	@Test
	public void edit_profile_form_failed_avatar_file_not_a_image() {
		when(mockFileUploader.uploadImageAndCropSquared(any(File.class), anyInt())).thenReturn(null);

		Http.MultipartFormData.Part<Source<ByteString, ?>> namePart =
				new Http.MultipartFormData.DataPart("name", "Anatoliy");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordPart =
				new Http.MultipartFormData.DataPart("password", "longEnoughPassword");
		Http.MultipartFormData.Part<Source<ByteString, ?>> passwordConfirmPart =
				new Http.MultipartFormData.DataPart("passwordConfirm", "longEnoughPassword");
		Http.MultipartFormData.Part<Source<ByteString, ?>> filePart = new Http.MultipartFormData.FilePart<>("avatarFile",
				"avatarFile", "application/octet-stream", FileIO.fromFile(
				new File("notimage.txt")));
		List<Http.MultipartFormData.Part<Source<ByteString, ?>>> data = Arrays.asList(namePart, passwordPart,
				passwordConfirmPart, filePart);

		Result result = route(app, fakeRequest()
				.method(POST)
				.cookie(Http.Cookie.builder("session_token", "active_token").build())
				.bodyMultipart(data, Files.singletonTemporaryFileCreator(), mat)
				.uri(routes.ProfileEditorController.edit().url()));
		assertEquals(BAD_REQUEST, result.status());
		assertTrue(contentAsString(result).contains("Unable to read file as image."));
	}
}
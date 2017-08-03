package controllers;

import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;

public class RegistrationTest extends WithApplication
{
	@Override
	protected Application provideApplication()
	{
		return new GuiceApplicationBuilder().build();
	}
	/*
	@Before
	public void setupRegistration()
	{
		Users confirmedUser = new Users();
		confirmedUser.name = "Confirmed user";
		confirmedUser.email = "confirmed-user@example.com";
		confirmedUser.passwordHash = Utils.hashString("yZybgWmeWl");
		confirmedUser.confirmationKey = "I3cZBsbNFw";
		confirmedUser.confirmed = true;
		confirmedUser.save();

		Users unconfirmedUser = new Users();
		unconfirmedUser.name = "Unconfirmed user";
		unconfirmedUser.email = "unconfirmed-user@example.com";
		unconfirmedUser.passwordHash = Utils.hashString("BGfnAaoLIw");
		unconfirmedUser.confirmationKey = "PPFgwkHX95";
		unconfirmedUser.confirmed = false;
		unconfirmedUser.save();
	}

	@Test
	public void testRegisteredConfirmed()
	{
		Result result;
		Map<String, String> formBody = new HashMap<>();

		formBody.clear();
		formBody.put("name", "Test Registered Confirmed");
		formBody.put("email", "confirmed-user@example.com");
		formBody.put("password", "4lrxrJsYww");
		formBody.put("passwordConfirm", "4lrxrJsYww");
		result = route(app, new Http.RequestBuilder()
				.method(POST)
				.uri("/registration")
				.bodyForm(formBody)
		);
		assertEquals(BAD_REQUEST, result.status());
	}

	@Test
	public void testRegisteredNotConfirmed()
	{
		Result result;
		Map<String, String> formBody = new HashMap<>();

		formBody.clear();
		formBody.put("name", "Test Registered Not Confirmed");
		formBody.put("email", "unconfirmed-user@example.com");
		formBody.put("password", "4lrxrJsYww");
		formBody.put("passwordConfirm", "4lrxrJsYww");
		result = route(app,
				new Http.RequestBuilder()
						.method(POST)
						.uri("/registration")
						.bodyForm(formBody)
		);
		assertEquals(SEE_OTHER, result.status());
	}

	@Test
	public void testNotRegistered()
	{
		Result result;
		Map<String, String> formBody = new HashMap<>();

		formBody.clear();
		formBody.put("name", "Test Not Registered");
		formBody.put("email", "not-registered-user@meta.ua");
		formBody.put("password", "4lrxrJsYww");
		formBody.put("passwordConfirm", "4lrxrJsYww");
		result = route(app,
				new Http.RequestBuilder()
						.method(POST)
						.uri("/registration")
						.bodyForm(formBody)
		);
		assertEquals(SEE_OTHER, result.status());
	}

	@After
	public void clearRegistration()
	{
		Ebean.deleteAll(Ebean.find(Users.class).findList());
	}*/
}
package controllers;

import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.SEE_OTHER;
import static play.test.Helpers.GET;
import static play.test.Helpers.route;

public class RoutesAsUnauthorizedTest extends WithApplication {

	@Override
	protected Application provideApplication() {
		return new GuiceApplicationBuilder().build();
	}

	@Test
	public void testRoutesAsUnauthorized() {
		Http.RequestBuilder request;
		Result result;

		//GET     /                           controllers.HomeController.index
		request = new Http.RequestBuilder().method(GET).uri("/");
		result = route(app, request);
		assertEquals(OK, result.status());

		//GET     /registration               controllers.RegistrationController.registration
		//POST    /registration               controllers.RegistrationController.register
		//GET     /login                      controllers.AuthorizationController.authorization
		//POST    /login                      controllers.AuthorizationController.authorize

		//GET     /logout                     controllers.AuthorizationController.logout
		request = new Http.RequestBuilder().method(GET).uri("/logout");
		result = route(app, request);
		assertEquals(SEE_OTHER, result.status());
	}
}
package controllers;

import controllers.utils.Utils;
import io.ebean.Ebean;
import models.data.User;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.SEE_OTHER;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.route;

public class HomeControllerTest extends WithApplication
{
	@Override
	protected Application provideApplication()
	{
		return new GuiceApplicationBuilder().build();
	}

	@Test
	public void testIndex()
	{
		Http.RequestBuilder request = new Http.RequestBuilder()
				.method(GET)
				.uri("/");

		Result result = route(app, request);
		assertEquals(OK, result.status());
	}
}
package controllers;

import controllers.actions.AuthorizationCheckAction;
import io.ebean.Ebean;
import models.data.News;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;

@With(AuthorizationCheckAction.class)
public class NewsController extends Controller
{
	public Result news()
	{
		return ok(views.html.news.render());
	}

	public Result loadNews(int count, int offset)
	{
		return ok(Ebean.json().toJson(Ebean.find(News.class).setFirstRow(offset).setMaxRows(count).findList()));
	}
}
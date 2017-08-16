package controllers;

import controllers.actions.AuthorizationCheckAction;
import io.ebean.Ebean;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;

import javax.inject.Inject;

@With(AuthorizationCheckAction.class)
public class NewsController extends Controller
{
	private final NewsRepository newsRepository;

	@Inject
	public NewsController(NewsRepository newsRepository)
	{
		this.newsRepository = newsRepository;
	}

	public Result news()
	{
		return ok(views.html.news.render());
	}

	public Result loadNews(int count, int offset)
	{
		return ok(Ebean.json().toJson(newsRepository.getNews(offset, count)));
	}
}
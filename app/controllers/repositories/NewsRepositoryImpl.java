package controllers.repositories;

import io.ebean.Ebean;
import models.News;

import java.util.List;

public class NewsRepositoryImpl implements NewsRepository
{
	@Override
	public List<News> getNews(int offset, int count)
	{
		return Ebean.find(News.class).setFirstRow(offset).setMaxRows(count).findList();
	}
}

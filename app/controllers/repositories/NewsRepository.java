package controllers.repositories;

import com.google.inject.ImplementedBy;
import models.News;

import java.util.List;

@ImplementedBy(NewsRepositoryImpl.class)
public interface NewsRepository
{
	List<News> getNews(int offset, int count);
}
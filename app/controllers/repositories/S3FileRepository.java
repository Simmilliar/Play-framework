package controllers.repositories;

import com.google.inject.ImplementedBy;
import models.S3File;

@ImplementedBy(S3FileRepositoryImpl.class)
public interface S3FileRepository
{
	void saveFile(S3File file);
	boolean deleteFile(S3File file);
}
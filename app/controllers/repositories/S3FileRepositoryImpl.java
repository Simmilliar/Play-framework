package controllers.repositories;

import models.S3File;

public class S3FileRepositoryImpl implements S3FileRepository
{
	@Override
	public void saveFile(S3File file)
	{
		file.save();
	}
}

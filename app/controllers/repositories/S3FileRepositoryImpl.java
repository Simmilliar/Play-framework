package controllers.repositories;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import controllers.utils.AmazonUtils;
import models.S3File;
import play.Logger;

import javax.inject.Inject;

public class S3FileRepositoryImpl implements S3FileRepository
{
	private AmazonUtils amazonUtils;

	@Inject
	public S3FileRepositoryImpl(AmazonUtils amazonUtils) {
		this.amazonUtils = amazonUtils;
	}

	@Override
	public void saveFile(S3File file)
	{
		if (amazonUtils.amazonS3 == null)
		{
			Logger.error("Could not save because amazonS3 was null");
			throw new RuntimeException("Could not save");
		}
		else
		{
			file.setBucket(amazonUtils.s3Bucket);
			file.save();
			PutObjectRequest putObjectRequest =
					new PutObjectRequest(amazonUtils.s3Bucket, file.getId().toString(), file.getFile());
			putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
			amazonUtils.amazonS3.putObject(putObjectRequest);
		}
	}

	@Override
	public boolean deleteFile(S3File file) {
		if (amazonUtils.amazonS3 == null)
		{
			Logger.error("Could not delete because amazonS3 was null");
			return false;
		}
		else
		{
			amazonUtils.amazonS3.deleteObject(amazonUtils.s3Bucket, file.getId().toString());
			file.delete();
			return true;
		}
	}
}

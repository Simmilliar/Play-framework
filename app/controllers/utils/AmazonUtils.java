package controllers.utils;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.typesafe.config.ConfigFactory;

import javax.inject.Singleton;

// looks fantastico
@Singleton
public class AmazonUtils
{
	private final AmazonS3 amazonS3;
	private final String s3Bucket;

	public AmazonUtils() {
		amazonS3 = new AmazonS3Client(
				new BasicAWSCredentials(
						ConfigFactory.load().getString("AWS_ACCESS_KEY"),
						ConfigFactory.load().getString("AWS_SECRET_KEY")
				)
		);
		s3Bucket = ConfigFactory.load().getString("AWS_S3_BUCKET");
	}

	public AmazonS3 getAmazonS3() {
		return amazonS3;
	}

	public String getS3Bucket() {
		return s3Bucket;
	}
}
package controllers.utils;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.typesafe.config.ConfigFactory;
import play.Logger;

import javax.inject.Singleton;

// solved todo move all files that ends with "Service" to services package, or rename them with "Util"
// solved todo fix it, make it singletone at least, with getInstance() method
@Singleton
public class AmazonUtils
{
	public final AmazonS3 amazonS3 = new AmazonS3Client(
			new BasicAWSCredentials(
					ConfigFactory.load().getString("AWS_ACCESS_KEY"),
					ConfigFactory.load().getString("AWS_SECRET_KEY")
			)
	);
	public final String s3Bucket = ConfigFactory.load().getString("AWS_S3_BUCKET");

	public AmazonUtils() {
		Logger.debug("Amazon utils created");
	}
}
package controllers.utils;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.typesafe.config.ConfigFactory;

public class AmazonService
{
	public static AmazonS3 amazonS3 = new AmazonS3Client(
			new BasicAWSCredentials(
					ConfigFactory.load().getString("AWS_ACCESS_KEY"),
					ConfigFactory.load().getString("AWS_SECRET_KEY")
			)
	);
	public static String s3Bucket = ConfigFactory.load().getString("AWS_S3_BUCKET");
}
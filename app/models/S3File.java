package models;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import controllers.utils.AmazonService;
import io.ebean.Model;
import play.Logger;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.File;
import java.util.UUID;

@Entity
public class S3File extends Model
{
	@Id
	private UUID id;
	private String bucket;

	@Transient
	public File file;

	public UUID getId()
	{
		return id;
	}

	public void setId(UUID id)
	{
		this.id = id;
	}

	public String getUrl()
	{
		return "https://s3.eu-central-1.amazonaws.com/" + bucket + "/" + id;
	}

	@Override
	public void save()
	{
		if (AmazonService.amazonS3 == null)
		{
			Logger.error("Could not save because amazonS3 was null");
			throw new RuntimeException("Could not save");
		}
		else
		{
			this.bucket = AmazonService.s3Bucket;

			super.save();

			PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, id.toString(), file);
			putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
			AmazonService.amazonS3.putObject(putObjectRequest);
		}
	}

	@Override
	public boolean delete()
	{
		if (AmazonService.amazonS3 == null)
		{
			Logger.error("Could not delete because amazonS3 was null");
			return false;
		}
		else
		{
			AmazonService.amazonS3.deleteObject(bucket, id.toString());
			super.delete();
			return true;
		}
	}
}
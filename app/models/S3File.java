package models;

import com.typesafe.config.ConfigFactory;
import io.ebean.Model;

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
	private File file;

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
		return "https://" + ConfigFactory.load().getString("s3-server-domain") + "/" + bucket + "/" + id;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
}
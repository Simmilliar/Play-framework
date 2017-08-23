package models;

import io.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class News extends Model
{
	@Id
	private UUID id;
	@Column(nullable = false, unique = true)
	private String url;
	@Column(nullable = false)
	private String imageUrl;
	@Column(nullable = false)
	private String title;
	@Column(nullable = false, length = 1023)
	private String description;

	public UUID getId()
	{
		return id;
	}

	public News setId(UUID id)
	{
		this.id = id;
		return this;
	}

	public String getUrl()
	{
		return url;
	}

	public News setUrl(String url)
	{
		this.url = url;
		return this;
	}

	public String getImageUrl()
	{
		return imageUrl;
	}

	public News setImageUrl(String imageUrl)
	{
		this.imageUrl = imageUrl;
		return this;
	}

	public String getTitle()
	{
		return title;
	}

	public News setTitle(String title)
	{
		this.title = title;
		return this;
	}

	public String getDescription()
	{
		return description;
	}

	public News setDescription(String description)
	{
		this.description = description;
		return this;
	}
}
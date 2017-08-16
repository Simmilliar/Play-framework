package models;

import io.ebean.Model;
import io.ebean.annotation.DbJson;
import io.ebean.annotation.DbJsonType;
import io.ebean.annotation.Index;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
public class Card extends Model
{
	@Id
	private UUID id;
	@Index
	@ManyToOne(optional = false)
	private Users owner;
	@Column(nullable = false)
	private String title;
	@Lob
	@Column(nullable = false)
	private String content;
	@Column(nullable = false)
	@DbJson(storage = DbJsonType.VARCHAR)
	private List<String> images;

	public UUID getId()
	{
		return id;
	}

	public void setId(UUID id)
	{
		this.id = id;
	}

	public Users getOwner()
	{
		return owner;
	}

	public void setOwner(Users owner)
	{
		this.owner = owner;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public List<String> getImages()
	{
		return images;
	}

	public void setImages(List<String> images)
	{
		this.images = images;
	}
}
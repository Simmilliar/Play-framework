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

	public Card setId(UUID id)
	{
		this.id = id;
		return this;
	}

	public Users getOwner()
	{
		return owner;
	}

	public Card setOwner(Users owner)
	{
		this.owner = owner;
		return this;
	}

	public String getTitle()
	{
		return title;
	}

	public Card setTitle(String title)
	{
		this.title = title;
		return this;
	}

	public String getContent()
	{
		return content;
	}

	public Card setContent(String content)
	{
		this.content = content;
		return this;
	}

	public List<String> getImages()
	{
		return images;
	}

	public Card setImages(List<String> images)
	{
		this.images = images;
		return this;
	}
}
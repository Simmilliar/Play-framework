package models;

import io.ebean.Model;
import io.ebean.annotation.DbJson;
import io.ebean.annotation.DbJsonType;
import io.ebean.annotation.Index;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
public class Product extends Model
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
	private String description;

	@Column(nullable = false)
	private int price;

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

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public int getPrice()
	{
		return price;
	}

	public void setPrice(int price)
	{
		this.price = price;
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
package models.data;

import io.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Session extends Model
{
	@Id
	public String token;
	@ManyToOne(optional = false)
	public Users user;
	@Column(nullable = false)
	public long expirationDate;
}
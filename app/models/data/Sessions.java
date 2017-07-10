package models.data;

import io.ebean.Model;
import io.ebean.annotation.NotNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Sessions extends Model {
	@Id
	public String token;
	@ManyToOne(optional = false)
	public User user;
	@Column(nullable = false)
	public long expirationDate;
}
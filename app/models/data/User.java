package models.data;

import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User extends Model {
	@Id
	public String email;
	@Column(nullable = false)
	public String name;
	@Column(nullable = false)
	public String passwordHash;
}
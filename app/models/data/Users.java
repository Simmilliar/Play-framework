package models.data;

import io.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

// todo where are getters and setters?
// todo all fields we use in queries should be indexed
@Entity
public class Users extends Model
{
	@Id
	public String email;
	@Column(nullable = false)
	public boolean confirmed;
	@Column(nullable = false)
	public String confirmationKey;
	@Column(nullable = false)
	public String name;
	@Column(nullable = false)
	public String passwordHash;
	@Column(nullable = false)
	public String passwordSalt;
	@Column(nullable = false)
	public String avatarUrl;
}
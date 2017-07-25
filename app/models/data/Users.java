package models.data;

import io.ebean.Model;
import io.ebean.annotation.Index;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

// todo where are getters and setters?
// solved todo all fields we use in queries should be indexed
@Entity
public class Users extends Model
{
	@Id
	public String email;

	@Column(nullable = false)
	public String name;
	@Column(nullable = false)
	public String avatarUrl;

	@Column(nullable = false)
	public String passwordHash;
	@Column(nullable = false)
	public String passwordSalt;

	@Index
	@Column(nullable = false)
	public boolean confirmed;
	@Index
	@Column(nullable = false)
	public String confirmationKeyHash;
	@Index
	@Column(nullable = false)
	public long confirmationKeyExpirationDate;
}
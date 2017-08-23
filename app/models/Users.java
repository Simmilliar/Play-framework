package models;

import io.ebean.Model;
import io.ebean.annotation.Index;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class Users extends Model
{
	@Id
	private UUID userId;

	@Column(nullable = false)
	private String name;
	@Index
	@Column(nullable = false, unique = true)
	private String email;
	@Column(nullable = false)
	private String avatarUrl;
	@Index
	@Column(nullable = false, unique = true)
	private long facebookId;
	@Index
	@Column(nullable = false, unique = true)
	private long twitterId;

	@Column(nullable = false)
	private String passwordHash;
	@Column(nullable = false)
	private String passwordSalt;

	@Index
	@Column(nullable = false)
	private boolean confirmed;
	@Index
	@Column(nullable = false, unique = true)
	private String confirmationKeyHash;
	@Index
	@Column(nullable = false)
	private long confirmationKeyExpirationDate;

	public String getEmail()
	{
		return email;
	}

	public Users setEmail(String email)
	{
		this.email = email;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public Users setName(String name)
	{
		this.name = name;
		return this;
	}

	public String getAvatarUrl()
	{
		return avatarUrl;
	}

	public Users setAvatarUrl(String avatarUrl)
	{
		this.avatarUrl = avatarUrl;
		return this;
	}

	public String getPasswordHash()
	{
		return passwordHash;
	}

	public Users setPasswordHash(String passwordHash)
	{
		this.passwordHash = passwordHash;
		return this;
	}

	public String getPasswordSalt()
	{
		return passwordSalt;
	}

	public Users setPasswordSalt(String passwordSalt)
	{
		this.passwordSalt = passwordSalt;
		return this;
	}

	public boolean isConfirmed()
	{
		return confirmed;
	}

	public Users setConfirmed(boolean confirmed)
	{
		this.confirmed = confirmed;
		return this;
	}

	public String getConfirmationKeyHash()
	{
		return confirmationKeyHash;
	}

	public Users setConfirmationKeyHash(String confirmationKeyHash)
	{
		this.confirmationKeyHash = confirmationKeyHash;
		return this;
	}

	public long getConfirmationKeyExpirationDate()
	{
		return confirmationKeyExpirationDate;
	}

	public Users setConfirmationKeyExpirationDate(long confirmationKeyExpirationDate)
	{
		this.confirmationKeyExpirationDate = confirmationKeyExpirationDate;
		return this;
	}

	public UUID getUserId()
	{
		return userId;
	}

	public Users setUserId(UUID userId)
	{
		this.userId = userId;
		return this;
	}

	public long getFacebookId()
	{
		return facebookId;
	}

	public Users setFacebookId(long facebookId)
	{
		this.facebookId = facebookId;
		return this;
	}

	public long getTwitterId()
	{
		return twitterId;
	}

	public Users setTwitterId(long twitterId)
	{
		this.twitterId = twitterId;
		return this;
	}
}
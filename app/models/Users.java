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

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getAvatarUrl()
	{
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl)
	{
		this.avatarUrl = avatarUrl;
	}

	public String getPasswordHash()
	{
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash)
	{
		this.passwordHash = passwordHash;
	}

	public String getPasswordSalt()
	{
		return passwordSalt;
	}

	public void setPasswordSalt(String passwordSalt)
	{
		this.passwordSalt = passwordSalt;
	}

	public boolean isConfirmed()
	{
		return confirmed;
	}

	public void setConfirmed(boolean confirmed)
	{
		this.confirmed = confirmed;
	}

	public String getConfirmationKeyHash()
	{
		return confirmationKeyHash;
	}

	public void setConfirmationKeyHash(String confirmationKeyHash)
	{
		this.confirmationKeyHash = confirmationKeyHash;
	}

	public long getConfirmationKeyExpirationDate()
	{
		return confirmationKeyExpirationDate;
	}

	public void setConfirmationKeyExpirationDate(long confirmationKeyExpirationDate)
	{
		this.confirmationKeyExpirationDate = confirmationKeyExpirationDate;
	}

	public UUID getUserId()
	{
		return userId;
	}

	public void setUserId(UUID userId)
	{
		this.userId = userId;
	}

	public long getFacebookId()
	{
		return facebookId;
	}

	public void setFacebookId(long facebookId)
	{
		this.facebookId = facebookId;
	}

	public long getTwitterId()
	{
		return twitterId;
	}

	public void setTwitterId(long twitterId)
	{
		this.twitterId = twitterId;
	}
}
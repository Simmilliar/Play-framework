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
	private String token;
	@ManyToOne(optional = false)
	private Users user;
	@Column(nullable = false)
	private long expirationDate;

	public boolean isExpired()
	{
		return expirationDate <= System.currentTimeMillis();
	}

	public String getToken()
	{
		return token;
	}

	public void setToken(String token)
	{
		this.token = token;
	}

	public Users getUser()
	{
		return user;
	}

	public void setUser(Users user)
	{
		this.user = user;
	}

	public long getExpirationDate()
	{
		return expirationDate;
	}

	public void setExpirationDate(long expirationDate)
	{
		this.expirationDate = expirationDate;
	}
}
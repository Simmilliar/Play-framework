package models.forms;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.List;

@Constraints.Validate
public class FacebookAuthForm implements Constraints.Validatable<List<ValidationError>>
{
	@Constraints.Required
	private String accessToken;
	private long expiresIn;
	private String signedRequest;
	@Constraints.Required
	private String userID;

	@Override
	public List<ValidationError> validate()
	{
		return null;
	}

	public String getAccessToken()
	{
		return accessToken;
	}

	public void setAccessToken(String accessToken)
	{
		this.accessToken = accessToken;
	}

	public long getExpiresIn()
	{
		return expiresIn;
	}

	public void setExpiresIn(long expiresIn)
	{
		this.expiresIn = expiresIn;
	}

	public String getSignedRequest()
	{
		return signedRequest;
	}

	public void setSignedRequest(String signedRequest)
	{
		this.signedRequest = signedRequest;
	}

	public String getUserID()
	{
		return userID;
	}

	public void setUserID(String userID)
	{
		this.userID = userID;
	}
}

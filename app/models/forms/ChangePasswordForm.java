package models.forms;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

@Constraints.Validate
public class ChangePasswordForm implements Constraints.Validatable<List<ValidationError>>
{
	@Constraints.Required
	private String password;

	private String passwordConfirm;

	@Override
	public List<ValidationError> validate()
	{
		List<ValidationError> errors = new ArrayList<ValidationError>();
		if (password.length() < 8)
		{
			errors.add(new ValidationError("password", "Password must be at least 8 symbols long."));
		}
		if (!passwordConfirm.equals(password))
		{
			errors.add(new ValidationError("password", "Passwords does not match."));
		}
		return errors;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getPasswordConfirm()
	{
		return passwordConfirm;
	}

	public void setPasswordConfirm(String passwordConfirm)
	{
		this.passwordConfirm = passwordConfirm;
	}
}
package models.forms;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

@Constraints.Validate
public class ChangePasswordForm implements Constraints.Validatable<List<ValidationError>>
{
	@Constraints.Required
	public String password;

	public String passwordConfirm;

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
		return errors.isEmpty() ? null : errors;
	}
}
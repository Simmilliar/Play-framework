package models.forms;

import com.typesafe.config.ConfigFactory;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

@Constraints.Validate
public class RegistrationForm implements Constraints.Validatable<List<ValidationError>>
{
	@Constraints.Required
	public String name;

	@Constraints.Required
	public String email;

	@Constraints.Required
	public String password;

	public String passwordConfirm;

	public List<ValidationError> errors = new ArrayList<>();

	@Override
	public List<ValidationError> validate()
	{
		errors.clear();
		if (!name.matches(ConfigFactory.load().getString("REGEX_NAME")))
		{
			errors.add(new ValidationError("name", "Invalid name."));
		}
		if (!email.matches(ConfigFactory.load().getString("REGEX_EMAIL")))
		{
			errors.add(new ValidationError("email", "Invalid e-mail address."));
		}
		// solved todo move it out to registration controller
		if (password.length() < 8)
		{
			errors.add(new ValidationError("password", "Password must be at least 8 symbols long."));
		}
		if (!password.equals(passwordConfirm))
		{
			errors.add(new ValidationError("passwordConfirm", "Passwords does not match."));
		}
		return errors;
	}
}
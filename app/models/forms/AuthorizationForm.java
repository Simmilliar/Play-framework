package models.forms;

import com.typesafe.config.ConfigFactory;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

@Constraints.Validate
public class AuthorizationForm implements Constraints.Validatable<List<ValidationError>>
{
	@Constraints.Required
	public String email;
	@Constraints.Required
	public String password;

	public List<ValidationError> errors = new ArrayList<>();

	@Override
	public List<ValidationError> validate()
	{
		errors.clear();

		if (!email.matches(ConfigFactory.load().getString("REGEX_EMAIL")))
		{
			errors.add(new ValidationError("email", "Invalid e-mail address."));
		}

		// solved todo move it out, only validate data format here

		return errors;
	}
}
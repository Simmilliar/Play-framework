package models.forms;

import com.typesafe.config.ConfigFactory;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

@Constraints.Validate
public class ForgotPasswordForm implements Constraints.Validatable<List<ValidationError>>
{

	@Constraints.Required
	public String email;

	public List<ValidationError> errors = new ArrayList<>();

	@Override
	public List<ValidationError> validate()
	{
		errors.clear();
		if (!email.matches(ConfigFactory.load().getString("REGEX_EMAIL")))
		{
			errors.add(new ValidationError("email", "Invalid e-mail address."));
		}
		return errors;
	}
}
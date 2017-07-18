package models.forms;

import controllers.utils.Utils;
import io.ebean.Ebean;
import models.data.Users;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

@Constraints.Validate
public class ForgotPasswordForm implements Constraints.Validatable<List<ValidationError>>
{

	@Constraints.Required
	public String email;

	@Override
	public List<ValidationError> validate()
	{
		List<ValidationError> errors = new ArrayList<ValidationError>();
		if (!email.matches(Utils.REGEX_EMAIL))
		{
			errors.add(new ValidationError("email", "Invalid e-mail address."));
		}
		else if (Ebean.find(Users.class, email) == null)
		{
			errors.add(new ValidationError("email", "No registered user with this e-mail."));
		}

		return errors.isEmpty() ? null : errors;
	}
}
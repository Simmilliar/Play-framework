package models.forms;

import controllers.utils.Utils;
import io.ebean.Ebean;
import models.data.User;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import javax.validation.Constraint;
import java.util.ArrayList;
import java.util.List;

@Constraints.Validate
public class AuthorizationForm implements Constraints.Validatable<List<ValidationError>>
{
	@Constraints.Required
	public String email;
	@Constraints.Required
	public String password;

	@Override
	public List<ValidationError> validate()
	{
		List<ValidationError> errors = new ArrayList<ValidationError>();
		if (!email.matches(Utils.REGEX_EMAIL))
		{
			errors.add(new ValidationError("email", "Invalid e-mail address."));
		}

		if (Ebean.find(User.class).where()
				.and()
				.eq("email", email)
				.eq("password_hash", Utils.hashString(password))
				.eq("confirmed", true)
				.endAnd()
				.findList().isEmpty())
		{
			errors.add(new ValidationError("password", "Invalid e-mail or password."));
		}

		return errors.isEmpty() ? null : errors;
	}
}
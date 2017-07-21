package models.forms;

import controllers.utils.Utils;
import io.ebean.Ebean;
import models.data.Users;
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

	@Override
	public List<ValidationError> validate()
	{
		List<ValidationError> errors = new ArrayList<>();
		if (!name.matches(Utils.REGEX_NAME))
		{
			errors.add(new ValidationError("name", "Invalid name."));
		}
		if (!email.matches(Utils.REGEX_EMAIL))
		{
			errors.add(new ValidationError("email", "Invalid e-mail address."));
		}
		if (Ebean.find(Users.class).where()
				.eq("email", email)
				.eq("confirmed", true)
				.findOne() != null)
		{
			errors.add(new ValidationError("email", "This e-mail is already registered."));
		}
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
package models.forms;

import controllers.utils.Utils;
import io.ebean.Ebean;
import models.data.Users;
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

	@Override
	public List<ValidationError> validate()
	{
		List<ValidationError> errors = new ArrayList<ValidationError>();
		if (!email.matches(Utils.REGEX_EMAIL))
		{
			errors.add(new ValidationError("email", "Invalid e-mail address."));
		}

		Users foundedUser = Ebean.find(Users.class).where()
				.and()
				.eq("email", email)
				.eq("confirmed", true)
				.endAnd()
				.findOne();
		if (foundedUser == null)
		{
			errors.add(new ValidationError("email", "Unregistered user."));
		}
		else
		{
			String hash = Utils.hashString(
					new StringBuilder(password)
					.insert(password.length() / 2, foundedUser.passwordSalt)
					.toString()
			);
			if (!foundedUser.passwordHash.equals(hash))
			{
				errors.add(new ValidationError("password", "Wrong password."));
			}
		}

		return errors;
	}
}
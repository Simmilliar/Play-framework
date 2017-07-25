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
	private String name;

	@Constraints.Required
	private String email;

	@Constraints.Required
	private String password;

	private String passwordConfirm;

	private List<ValidationError> errors = new ArrayList<>();

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

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
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

	public List<ValidationError> getErrors()
	{
		return errors;
	}

	public void setErrors(List<ValidationError> errors)
	{
		this.errors = errors;
	}

	public void addError(ValidationError validationError)
	{
		errors.add(validationError);
	}
}
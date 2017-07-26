package models.forms;

import controllers.utils.Utils;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

@Constraints.Validate
public class ForgotPasswordForm implements Constraints.Validatable<List<ValidationError>>
{

	@Constraints.Required
	private String email;

	private List<ValidationError> errors = new ArrayList<>();

	@Override
	public List<ValidationError> validate()
	{
		errors.clear();
		if (!email.matches(Utils.REGEX_EMAIL))
		{
			errors.add(new ValidationError("email", "Invalid e-mail address."));
		}
		return errors;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
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
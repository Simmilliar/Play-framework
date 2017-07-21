package models.forms;

import controllers.utils.Utils;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Constraints.Validate
public class ProfileEditorForm implements Constraints.Validatable<List<ValidationError>>
{
	@Constraints.Required
	public String name;

	@Constraints.Required
	public String email;

	public String password = "";

	public String passwordConfirm = "";

	public File avatarFile;

	public String avatarUrl;

	public boolean avatarFileIsValid;

	@Override
	public List<ValidationError> validate()
	{
		List<ValidationError> errors = new ArrayList<>();
		if (!name.matches(Utils.REGEX_NAME))
		{
			errors.add(new ValidationError("name", "Invalid name."));
		}
		if (password.length() > 0 && password.length() < 8)
		{
			errors.add(new ValidationError("password", "Password must be at least 8 symbols long."));
		}
		if (!passwordConfirm.equals(password))
		{
			errors.add(new ValidationError("passwordConfirm", "Passwords does not match."));
		}
		if (avatarFile != null && !avatarFileIsValid)
		{
			errors.add(new ValidationError("avatarFile", "Unable to read file as image."));
		}
		return errors;
	}
}

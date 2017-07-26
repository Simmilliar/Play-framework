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
	private String name;

	@Constraints.Required
	private String email;

	private String password = "";

	private String passwordConfirm = "";

	private File avatarFile;

	private String avatarUrl;

	private List<ValidationError> errors = new ArrayList<>();

	@Override
	public List<ValidationError> validate()
	{
		errors.clear();
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

	public File getAvatarFile()
	{
		return avatarFile;
	}

	public void setAvatarFile(File avatarFile)
	{
		this.avatarFile = avatarFile;
	}

	public String getAvatarUrl()
	{
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl)
	{
		this.avatarUrl = avatarUrl;
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

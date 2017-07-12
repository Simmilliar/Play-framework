package models.forms;


import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

@Constraints.Validate
public class ProfileEditorForm implements Constraints.Validatable<List<ValidationError>> {

	@Constraints.Required
	public String name;

	public String email;

	public String password = "";

	public String passwordConfirm = "";

	@Override
	public List<ValidationError> validate() {
		List<ValidationError> errors = new ArrayList<ValidationError>();
		if (!name.matches("[a-zA-Z\\s]+")) {
			errors.add(new ValidationError("name", "Invalid name."));
		}
		if (password.length() > 0 && password.length() < 8) {
			errors.add(new ValidationError("password", "Password must be at least 8 symbols long."));
		}
		if (!passwordConfirm.equals(password)) {
			errors.add(new ValidationError("password", "Passwords does not match."));
		}
		return errors.isEmpty() ? null : errors;
	}
}
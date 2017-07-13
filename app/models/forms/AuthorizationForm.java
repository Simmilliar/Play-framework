package models.forms;

import controllers.Utils;
import io.ebean.Ebean;
import models.data.User;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.ArrayList;
import java.util.List;

@Constraints.Validate
public class AuthorizationForm implements Constraints.Validatable<List<ValidationError>> {

	public String email;
	public String password;

	@Override
	public List<ValidationError> validate() {
		List<ValidationError> errors = new ArrayList<ValidationError>();
		if (!email.matches("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")) {
			errors.add(new ValidationError("email", "Invalid e-mail address."));
		}

		if (Ebean.find(User.class).where()
				.and()
				.eq("email", email)
				.eq("password_hash", Utils.hashString(password))
				.eq("confirmed", true)
				.endAnd()
				.findList().isEmpty()) {
			errors.add(new ValidationError("password", "Invalid e-mail or password."));
		}

		return errors.isEmpty() ? null : errors;
	}
}
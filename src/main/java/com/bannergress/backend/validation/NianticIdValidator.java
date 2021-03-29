package com.bannergress.backend.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/** Validates a Niantic ID. */
public class NianticIdValidator implements ConstraintValidator<NianticId, String> {
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return value == null || value.matches("^[0-9a-f]{32}\\.[0-9a-f]{1,3}$");
	}
}

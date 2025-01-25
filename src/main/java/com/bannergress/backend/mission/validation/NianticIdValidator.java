package com.bannergress.backend.mission.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates a Niantic ID.
 */
public class NianticIdValidator implements ConstraintValidator<NianticId, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.matches("^[0-9a-f]{32}\\.[0-9a-f]{1,3}$");
    }
}

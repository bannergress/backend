package com.bannergress.backend.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates a number to be a multiple of six.
 */
public class MultipleOfSixValidator implements ConstraintValidator<MultipleOfSix, Number> {
    @Override
    public boolean isValid(Number value, ConstraintValidatorContext context) {
        return value == null || value.longValue() % 6 == 0;
    }
}

package com.bannergress.backend.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * The annotated elemented must be a multiple of six. <code>null</code> is
 * considered valid.
 */
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MultipleOfSixValidator.class)
@Documented
public @interface MultipleOfSix {

	String message() default "{com.bannergress.backend.validation.nianticid}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}

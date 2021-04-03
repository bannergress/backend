package com.bannergress.backend.validation;

import javax.validation.Constraint;
import javax.validation.Payload;

import java.lang.annotation.*;

/**
 * The annotated elemented must be a valid Niantic ID. <code>null</code> is
 * considered valid.
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NianticIdValidator.class)
@Documented
public @interface NianticId {

    String message() default "{com.bannergress.backend.validation.nianticid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

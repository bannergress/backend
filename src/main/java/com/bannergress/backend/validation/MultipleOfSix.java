package com.bannergress.backend.validation;

import javax.validation.Constraint;
import javax.validation.Payload;

import java.lang.annotation.*;

/**
 * The annotated elemented must be a multiple of six. <code>null</code> is
 * considered valid.
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MultipleOfSixValidator.class)
@Documented
public @interface MultipleOfSix {

    String message() default "{com.bannergress.backend.validation.nianticid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

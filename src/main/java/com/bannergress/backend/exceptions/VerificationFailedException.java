package com.bannergress.backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception for when a verification fails.
 */
@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
public class VerificationFailedException extends Exception {
    private static final long serialVersionUID = 1L;
}

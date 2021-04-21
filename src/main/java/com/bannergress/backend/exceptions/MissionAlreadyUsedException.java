package com.bannergress.backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception for when a mission is already used by another banner.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class MissionAlreadyUsedException extends Exception {
    private static final long serialVersionUID = 1L;
}

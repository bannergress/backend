package com.bannergress.backend.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles error requests.
 */
@RestController
public class ErrorStatusController implements ErrorController {
    @RequestMapping(value = "/error")
    public ResponseEntity<Void> error(HttpServletRequest request) {
        Integer errorCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        return errorCode == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
            : ResponseEntity.status(errorCode).build();
    }
}

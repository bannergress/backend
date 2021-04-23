package com.bannergress.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Returns 404 NOT FOUND with no content for any request to unknown resources.
 */
@RestController
public class DefaultController  {

    @RequestMapping(value = "/**")
    public ResponseEntity<String> notFound() {
        return ResponseEntity.notFound().build();
    }
}

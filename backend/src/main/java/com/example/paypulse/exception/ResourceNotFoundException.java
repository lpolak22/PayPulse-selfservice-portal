package com.example.paypulse.exception;

/** Thrown when a requested entity does not exist (HTTP 404). */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String resource, Object id) {
        super(ErrorCode.NOT_FOUND, resource + " with id " + id + " was not found.");
    }
}

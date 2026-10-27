package com.example.paypulse.exception;
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String resource, Object id) {
        super(ErrorCode.NOT_FOUND, resource + " with id " + id + " was not found.");
    }
}

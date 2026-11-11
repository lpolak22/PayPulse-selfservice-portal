package com.example.paypulse.exception;

public class UserEmailAlreadyExistsException extends ApiException {

    public UserEmailAlreadyExistsException(String email) {
        super(ErrorCode.USER_EMAIL_EXISTS, "A user with email " + email + " already exists.");
    }
}

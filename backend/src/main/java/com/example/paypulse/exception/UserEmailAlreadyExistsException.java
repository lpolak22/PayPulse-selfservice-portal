package com.example.paypulse.exception;

/** Thrown when another user that is not deleted already has the email, ignoring letter case (HTTP 409). */
public class UserEmailAlreadyExistsException extends ApiException {

    public UserEmailAlreadyExistsException(String email) {
        super(ErrorCode.USER_EMAIL_EXISTS, "A user with email " + email + " already exists.");
    }
}

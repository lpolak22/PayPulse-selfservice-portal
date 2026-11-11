package com.example.paypulse.exception;

public class LastAdminException extends ApiException {

    public LastAdminException() {
        super(ErrorCode.LAST_ADMIN);
    }
}

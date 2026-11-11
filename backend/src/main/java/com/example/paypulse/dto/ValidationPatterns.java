package com.example.paypulse.dto;

public final class ValidationPatterns {

    public static final String E164_PHONE = "^\\+[1-9]\\d{1,14}$";

    public static final String EMAIL = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    public static final String NOT_BLANK = "(?s).*\\S.*";

    public static final String E164_PHONE_MESSAGE = "must be in E.164 format, e.g. +385911234567";
    public static final String NOT_BLANK_MESSAGE = "must not be blank";

    private ValidationPatterns() {
    }
}

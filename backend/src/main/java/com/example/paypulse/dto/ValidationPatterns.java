package com.example.paypulse.dto;

public final class ValidationPatterns {

    /** E.164: a plus sign followed by up to 15 digits, no leading zero. */
    public static final String E164_PHONE = "^\\+[1-9]\\d{1,14}$";

    /** Requires a dot in the domain part, which the default {@code @Email} check does not. */
    public static final String EMAIL = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    /** Lets a PATCH field be omitted (null) but rejects blank values. */
    public static final String NOT_BLANK = "(?s).*\\S.*";

    public static final String E164_PHONE_MESSAGE = "must be in E.164 format, e.g. +385911234567";
    public static final String NOT_BLANK_MESSAGE = "must not be blank";

    private ValidationPatterns() {
    }
}

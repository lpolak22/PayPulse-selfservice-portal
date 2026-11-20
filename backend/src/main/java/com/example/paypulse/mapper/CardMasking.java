package com.example.paypulse.mapper;

import java.util.regex.Pattern;

public final class CardMasking {

    private static final int VISIBLE_PREFIX = 6;
    private static final int VISIBLE_SUFFIX = 4;
    private static final Pattern SEPARATORS = Pattern.compile("[\\s-]");
    private static final Pattern MASKABLE_PAN = Pattern.compile("\\d{6}[\\d*Xx\\u2022]{2,9}\\d{4}");

    private CardMasking() {
    }

    public static String mask(String pan) {
        if (pan == null) {
            return null;
        }
        String compact = SEPARATORS.matcher(pan).replaceAll("");
        if (!MASKABLE_PAN.matcher(compact).matches()) {
            return null;
        }
        int hidden = compact.length() - VISIBLE_PREFIX - VISIBLE_SUFFIX;
        return compact.substring(0, VISIBLE_PREFIX)
                + "*".repeat(hidden)
                + compact.substring(compact.length() - VISIBLE_SUFFIX);
    }
}

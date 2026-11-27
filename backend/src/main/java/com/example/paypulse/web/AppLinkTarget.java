package com.example.paypulse.web;

import java.util.Arrays;
import java.util.Optional;

public enum AppLinkTarget {

    ACTIVATE("activate", "Set your PayPulse password"),
    RESET_PASSWORD("reset-password", "Reset your PayPulse password");

    private final String path;
    private final String title;

    AppLinkTarget(String path, String title) {
        this.path = path;
        this.title = title;
    }

    public String path() {
        return path;
    }

    public String title() {
        return title;
    }

    public static Optional<AppLinkTarget> fromPath(String path) {
        return Arrays.stream(values()).filter(target -> target.path.equals(path)).findFirst();
    }
}

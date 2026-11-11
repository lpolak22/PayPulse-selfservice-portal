package com.example.paypulse.service;

import java.util.List;

public interface PasswordPolicy {

    List<String> findViolations(String password);
}

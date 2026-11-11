package com.example.paypulse.service.impl;

import com.example.paypulse.dto.common.ErrorResponse;
import com.example.paypulse.exception.FieldValidationException;
import com.example.paypulse.service.NewPasswordValidator;
import com.example.paypulse.service.PasswordPolicy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PolicyNewPasswordValidator implements NewPasswordValidator {

    private final PasswordPolicy passwordPolicy;

    public PolicyNewPasswordValidator(PasswordPolicy passwordPolicy) {
        this.passwordPolicy = passwordPolicy;
    }

    @Override
    public void validate(String password, String confirmPassword) {
        List<ErrorResponse.FieldError> errors = new ArrayList<>();
        passwordPolicy.findViolations(password)
                .forEach(message -> errors.add(new ErrorResponse.FieldError("password", message)));
        if (!password.equals(confirmPassword)) {
            errors.add(new ErrorResponse.FieldError("confirmPassword", "Passwords do not match."));
        }
        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }
    }
}

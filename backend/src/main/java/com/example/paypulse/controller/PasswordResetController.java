package com.example.paypulse.controller;

import com.example.paypulse.dto.auth.PasswordResetRequest;
import com.example.paypulse.dto.auth.PasswordResetRequestResponse;
import com.example.paypulse.dto.auth.PasswordResetStatusResponse;
import com.example.paypulse.dto.auth.ResetPasswordRequest;
import com.example.paypulse.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/password-reset")
@Tag(name = "Password reset", description = "Reset a forgotten password with a link sent by email")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/request")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Request a reset link",
            description = "Always returns the same response, an email is sent only to an active account. "
                    + "Returns 429 after 3 requests per email or 10 per IP address in an hour.")
    public PasswordResetRequestResponse requestReset(@Valid @RequestBody PasswordResetRequest request,
                                                     HttpServletRequest httpRequest) {
        passwordResetService.requestReset(request.email(), httpRequest.getRemoteAddr());
        return PasswordResetRequestResponse.sentIfExists();
    }

    @GetMapping("/{token}")
    @Operation(summary = "Check a reset link", description = "Returns VALID, EXPIRED or USED.")
    public PasswordResetStatusResponse checkToken(@PathVariable String token) {
        return new PasswordResetStatusResponse(passwordResetService.checkToken(token));
    }

    @PostMapping("/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Set a new password with the reset link",
            description = "Ends all sessions of the user and unlocks the account.")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
    }
}

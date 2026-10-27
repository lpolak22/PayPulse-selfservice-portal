package com.example.paypulse.security;

/**
 * Gives services the acting user without tying them to a specific authentication mechanism.
 */
public interface CurrentUserProvider {

    CurrentUser getCurrentUser();
}

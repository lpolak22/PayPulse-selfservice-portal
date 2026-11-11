package com.example.paypulse.domain.auth

object EmailRule {
    private val pattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    fun isValid(email: String): Boolean = pattern.matches(email)
}

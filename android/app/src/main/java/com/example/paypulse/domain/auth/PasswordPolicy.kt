package com.example.paypulse.domain.auth

object PasswordPolicy {
    const val MIN_LENGTH = 3

    fun isMet(rule: PasswordRule, password: String): Boolean = when (rule) {
        PasswordRule.MIN_LENGTH -> password.length >= MIN_LENGTH
        PasswordRule.UPPERCASE -> password.any { it.isUpperCase() }
        PasswordRule.LOWERCASE -> password.any { it.isLowerCase() }
        PasswordRule.DIGIT -> password.any { it.isDigit() }
    }

    fun metRules(password: String): Set<PasswordRule> =
        PasswordRule.entries.filter { isMet(it, password) }.toSet()

    fun unmetRules(password: String): Set<PasswordRule> =
        PasswordRule.entries.filterNot { isMet(it, password) }.toSet()

    fun isSatisfiedBy(password: String): Boolean = unmetRules(password).isEmpty()
}

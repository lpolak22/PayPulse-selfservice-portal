package com.example.paypulse.domain.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordPolicyTest {

    private val validPassword = "Ab1".padEnd(PasswordPolicy.MIN_LENGTH, 'x')

    @Test
    fun emptyPassword_meetsNoRule() {
        val allRules = PasswordRule.entries.toSet()
        val unmet = PasswordPolicy.unmetRules("")
        assertEquals(allRules, unmet)
    }

    @Test
    fun validPassword_meetsAllRules() {
        assertEquals(emptySet<PasswordRule>(), PasswordPolicy.unmetRules(validPassword))
        assertTrue(PasswordPolicy.isSatisfiedBy(validPassword))
    }

    @Test
    fun passwordOneShorterThanMinimum_failsMinLength() {
        val password = "a".repeat(PasswordPolicy.MIN_LENGTH - 1)
        assertFalse(PasswordPolicy.isMet(PasswordRule.MIN_LENGTH, password))
    }

    @Test
    fun passwordExactlyAtMinimum_meetsMinLength() {
        val password = "a".repeat(PasswordPolicy.MIN_LENGTH)
        assertTrue(PasswordPolicy.isMet(PasswordRule.MIN_LENGTH, password))
    }

    @Test
    fun passwordWithoutUppercase_failsUppercase() {
        assertFalse(PasswordPolicy.isMet(PasswordRule.UPPERCASE, "abc1"))
    }

    @Test
    fun passwordWithoutLowercase_failsLowercase() {
        assertFalse(PasswordPolicy.isMet(PasswordRule.LOWERCASE, "ABC1"))
    }

    @Test
    fun passwordWithoutDigit_failsDigit() {
        assertFalse(PasswordPolicy.isMet(PasswordRule.DIGIT, "Abcd"))
    }

    @Test
    fun croatianLetters_countAsUpperAndLowercase() {
        assertTrue(PasswordPolicy.isMet(PasswordRule.UPPERCASE, "Čabc"))
        assertTrue(PasswordPolicy.isMet(PasswordRule.LOWERCASE, "ABCž"))
    }

    @Test
    fun unmetRules_listsOnlyTheFailedRules() {
        val unmet = PasswordPolicy.unmetRules("abc".padEnd(PasswordPolicy.MIN_LENGTH, 'x'))
        assertEquals(setOf(PasswordRule.UPPERCASE, PasswordRule.DIGIT), unmet)
    }

    @Test
    fun metRules_listsOnlyThePassedRules() {
        val met = PasswordPolicy.metRules("abc".padEnd(PasswordPolicy.MIN_LENGTH, 'x'))
        assertEquals(setOf(PasswordRule.MIN_LENGTH, PasswordRule.LOWERCASE), met)
    }

    @Test
    fun anyUnmetRule_meansPolicyIsNotSatisfied() {
        assertFalse(PasswordPolicy.isSatisfiedBy(validPassword.lowercase()))
    }
}

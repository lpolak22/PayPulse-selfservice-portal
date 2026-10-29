package com.example.paypulse.data.remote.mapper

import com.example.paypulse.data.remote.ActivationApiContract
import com.example.paypulse.domain.activation.ActivationError
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivationErrorMapperTest {

    @Test
    fun tokenCodes_mapToTokenErrors() {
        assertEquals(ActivationError.TOKEN_EXPIRED, activationErrorFrom(ActivationApiContract.CODE_TOKEN_EXPIRED))
        assertEquals(ActivationError.TOKEN_USED, activationErrorFrom(ActivationApiContract.CODE_TOKEN_USED))
        assertEquals(ActivationError.TOKEN_INVALID, activationErrorFrom(ActivationApiContract.CODE_TOKEN_INVALID))
    }

    @Test
    fun validationErrorOnPassword_mapsToPolicyError() {
        val error = activationErrorFrom(ActivationApiContract.CODE_VALIDATION_ERROR, listOf(ActivationApiContract.FIELD_PASSWORD))
        assertEquals(ActivationError.PASSWORD_POLICY, error)
    }

    @Test
    fun validationErrorOnConfirmPassword_mapsToMismatchError() {
        val fields = listOf(ActivationApiContract.FIELD_CONFIRM_PASSWORD)
        val error = activationErrorFrom(ActivationApiContract.CODE_VALIDATION_ERROR, fields)
        assertEquals(ActivationError.PASSWORD_MISMATCH, error)
    }

    @Test
    fun validationErrorOnBothFields_reportsPolicyFirst() {
        val fields = listOf(ActivationApiContract.FIELD_CONFIRM_PASSWORD, ActivationApiContract.FIELD_PASSWORD)
        val error = activationErrorFrom(ActivationApiContract.CODE_VALIDATION_ERROR, fields)
        assertEquals(ActivationError.PASSWORD_POLICY, error)
    }

    @Test
    fun validationErrorWithoutKnownField_mapsToUnknown() {
        assertEquals(ActivationError.UNKNOWN, activationErrorFrom(ActivationApiContract.CODE_VALIDATION_ERROR))
        assertEquals(ActivationError.UNKNOWN, activationErrorFrom(ActivationApiContract.CODE_VALIDATION_ERROR, listOf("email")))
    }

    @Test
    fun unknownOrMissingCode_mapsToUnknown() {
        assertEquals(ActivationError.UNKNOWN, activationErrorFrom("SOMETHING_NEW"))
        assertEquals(ActivationError.UNKNOWN, activationErrorFrom(null))
    }
}

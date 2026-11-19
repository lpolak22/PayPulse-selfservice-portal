package com.example.paypulse.data.remote.mapper

import com.example.paypulse.data.remote.TransactionApiContract
import com.example.paypulse.domain.transaction.TransactionDetailsError
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionDetailsErrorMapperTest {

    @Test
    fun notFound_mapsToNotFound() {
        val error = transactionDetailsErrorFrom(TransactionApiContract.CODE_NOT_FOUND)
        assertEquals(TransactionDetailsError.NOT_FOUND, error)
    }

    @Test
    fun otherCodes_mapToUnknown() {
        assertEquals(TransactionDetailsError.UNKNOWN, transactionDetailsErrorFrom("VALIDATION_ERROR"))
        assertEquals(TransactionDetailsError.UNKNOWN, transactionDetailsErrorFrom("INTERNAL_ERROR"))
    }

    @Test
    fun missingCode_mapsToUnknown() {
        assertEquals(TransactionDetailsError.UNKNOWN, transactionDetailsErrorFrom(null))
    }
}

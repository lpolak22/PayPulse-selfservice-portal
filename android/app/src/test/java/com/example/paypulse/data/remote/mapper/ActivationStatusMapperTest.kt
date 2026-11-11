package com.example.paypulse.data.remote.mapper

import com.example.paypulse.data.remote.dto.ActivationStatusResponseDto
import com.example.paypulse.domain.activation.ActivationTokenStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ActivationStatusMapperTest {

    @Test
    fun backendStatuses_mapToDomainStatuses() {
        assertEquals(ActivationTokenStatus.VALID, ActivationStatusResponseDto("VALID").toTokenStatus())
        assertEquals(ActivationTokenStatus.EXPIRED, ActivationStatusResponseDto("EXPIRED").toTokenStatus())
        assertEquals(ActivationTokenStatus.USED, ActivationStatusResponseDto("USED").toTokenStatus())
    }

    @Test
    fun unknownStatus_mapsToNull() {
        assertNull(ActivationStatusResponseDto("SOMETHING_NEW").toTokenStatus())
        assertNull(ActivationStatusResponseDto("valid").toTokenStatus())
    }
}

package com.example.paypulse.data.remote.mapper

import com.example.paypulse.data.remote.dto.ActivationStatusResponseDto
import com.example.paypulse.domain.activation.ActivationTokenStatus

fun ActivationStatusResponseDto.toTokenStatus(): ActivationTokenStatus? =
    ActivationTokenStatus.entries.firstOrNull { it.name == status }

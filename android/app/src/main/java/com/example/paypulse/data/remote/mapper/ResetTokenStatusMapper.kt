package com.example.paypulse.data.remote.mapper

import com.example.paypulse.data.remote.dto.ResetTokenStatusResponseDto
import com.example.paypulse.domain.passwordreset.ResetTokenStatus

fun ResetTokenStatusResponseDto.toResetTokenStatus(): ResetTokenStatus? =
    ResetTokenStatus.entries.firstOrNull { it.name == status }

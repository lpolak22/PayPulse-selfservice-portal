package com.example.paypulse.data.remote.dto

import com.example.paypulse.data.remote.serialization.BigDecimalSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class TrendPointDto(
    val start: String,
    @Serializable(with = BigDecimalSerializer::class) val amount: BigDecimal,
    val count: Long
)

package com.example.paypulse.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TrendDto(
    val unit: String,
    val points: List<TrendPointDto>
)

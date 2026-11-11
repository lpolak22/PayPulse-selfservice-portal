package com.example.paypulse.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PageResponseDto<T>(
    val items: List<T> = emptyList(),
    val page: Int,
    val pageSize: Int,
    val total: Long
)

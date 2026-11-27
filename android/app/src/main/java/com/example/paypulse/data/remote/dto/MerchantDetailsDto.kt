package com.example.paypulse.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MerchantDetailsDto(
    val id: Long,
    val merchantId: String? = null,
    val name: String,
    val registrationNumber: String,
    val status: String? = null,
    val outlets: RelatedItemsDto<OutletDto> = RelatedItemsDto(),
    val terminals: RelatedItemsDto<MerchantTerminalDto> = RelatedItemsDto()
)

@Serializable
data class RelatedItemsDto<T>(
    val items: List<T> = emptyList(),
    val total: Long = 0
)

@Serializable
data class MerchantTerminalDto(
    val id: Long,
    val terminalId: String? = null,
    val outlet: OutletRefDto? = null,
    val status: String? = null
)

@Serializable
data class OutletRefDto(
    val id: Long,
    val name: String? = null
)

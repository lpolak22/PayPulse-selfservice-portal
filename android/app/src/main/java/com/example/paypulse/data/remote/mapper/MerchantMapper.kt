package com.example.paypulse.data.remote.mapper

import com.example.paypulse.data.remote.dto.MerchantDto
import com.example.paypulse.data.remote.dto.PageResponseDto
import com.example.paypulse.domain.merchant.Merchant
import com.example.paypulse.domain.merchant.MerchantPage
import com.example.paypulse.domain.merchant.MerchantStatus

fun PageResponseDto<MerchantDto>.toMerchantPage(): MerchantPage = MerchantPage(
    items = items.map { it.toMerchant() },
    page = page,
    pageSize = pageSize,
    total = total
)

fun MerchantDto.toMerchant(): Merchant = Merchant(
    id = id,
    name = name,
    oib = registrationNumber,
    status = MerchantStatus.entries.firstOrNull { it.name.equals(status, ignoreCase = true) },
    outletCount = outletCount
)

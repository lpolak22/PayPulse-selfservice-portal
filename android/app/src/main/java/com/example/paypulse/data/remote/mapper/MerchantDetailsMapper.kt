package com.example.paypulse.data.remote.mapper

import com.example.paypulse.data.remote.dto.MerchantDetailsDto
import com.example.paypulse.data.remote.dto.MerchantTerminalDto
import com.example.paypulse.data.remote.dto.OutletDto
import com.example.paypulse.data.remote.dto.RelatedItemsDto
import com.example.paypulse.domain.merchant.MerchantDetails
import com.example.paypulse.domain.merchant.MerchantStatus
import com.example.paypulse.domain.merchant.MerchantTerminal
import com.example.paypulse.domain.merchant.RelatedItems
import com.example.paypulse.domain.outlet.Outlet
import com.example.paypulse.domain.outlet.OutletStatus
import com.example.paypulse.domain.terminal.TerminalStatus

fun MerchantDetailsDto.toMerchantDetails(): MerchantDetails = MerchantDetails(
    id = id,
    merchantId = merchantId.nullIfBlank(),
    name = name,
    oib = registrationNumber,
    status = enumOrNull<MerchantStatus>(status),
    outlets = outlets.toRelatedItems { it.toRelatedOutlet() },
    terminals = terminals.toRelatedItems { it.toMerchantTerminal() }
)

private fun <T, R> RelatedItemsDto<T>.toRelatedItems(transform: (T) -> R): RelatedItems<R> =
    RelatedItems(items = items.map(transform), total = maxOf(total, items.size.toLong()))

private fun OutletDto.toRelatedOutlet(): Outlet = Outlet(
    id = id,
    outletId = outletId.nullIfBlank(),
    name = name,
    address = address.nullIfBlank(),
    city = city.nullIfBlank(),
    postalCode = postalCode.nullIfBlank(),
    status = enumOrNull<OutletStatus>(status),
    terminalCount = terminalCount
)

private fun MerchantTerminalDto.toMerchantTerminal(): MerchantTerminal = MerchantTerminal(
    id = id,
    terminalId = terminalId.nullIfBlank(),
    outletName = outlet?.name.nullIfBlank(),
    status = enumOrNull<TerminalStatus>(status)
)

private fun String?.nullIfBlank(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

private inline fun <reified T : Enum<T>> enumOrNull(value: String?): T? =
    enumValues<T>().firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) }

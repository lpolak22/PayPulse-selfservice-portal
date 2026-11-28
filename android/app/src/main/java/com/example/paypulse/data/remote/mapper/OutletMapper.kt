package com.example.paypulse.data.remote.mapper

import com.example.paypulse.data.remote.dto.OutletDto
import com.example.paypulse.data.remote.dto.PageResponseDto
import com.example.paypulse.domain.outlet.Outlet
import com.example.paypulse.domain.outlet.OutletPage
import com.example.paypulse.domain.outlet.OutletStatus

fun PageResponseDto<OutletDto>.toOutletPage(): OutletPage = OutletPage(
    items = items.map { it.toOutlet() },
    page = page,
    pageSize = pageSize,
    total = total
)

fun OutletDto.toOutlet(): Outlet = Outlet(
    id = id,
    outletId = outletId.orNullIfBlank(),
    name = name,
    address = address.orNullIfBlank(),
    city = city.orNullIfBlank(),
    postalCode = postalCode.orNullIfBlank(),
    status = OutletStatus.entries.firstOrNull { it.name.equals(status, ignoreCase = true) },
    terminalCount = terminalCount
)

private fun String?.orNullIfBlank(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

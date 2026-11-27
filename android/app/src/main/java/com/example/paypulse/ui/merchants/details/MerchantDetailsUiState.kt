package com.example.paypulse.ui.merchants.details

import com.example.paypulse.domain.merchant.MerchantDetails
import com.example.paypulse.domain.merchant.MerchantDetailsError
import com.example.paypulse.domain.merchant.MerchantTerminal
import com.example.paypulse.domain.outlet.Outlet

data class MerchantDetailsUiState(
    val content: MerchantDetailsContent = MerchantDetailsContent.Loading,
    val selectedTab: MerchantDetailsTab = MerchantDetailsTab.OUTLETS,
    val outletSearch: String = "",
    val terminalSearch: String = ""
) {
    val details: MerchantDetails?
        get() = (content as? MerchantDetailsContent.Loaded)?.details

    val currentSearch: String
        get() = when (selectedTab) {
            MerchantDetailsTab.OUTLETS -> outletSearch
            MerchantDetailsTab.TERMINALS -> terminalSearch
        }

    val visibleOutlets: List<Outlet>
        get() = details?.outlets?.items.orEmpty().filter { it.matches(outletSearch) }

    val visibleTerminals: List<MerchantTerminal>
        get() = details?.terminals?.items.orEmpty().filter { it.matches(terminalSearch) }
}

sealed interface MerchantDetailsContent {
    data object Loading : MerchantDetailsContent
    data class Error(val error: MerchantDetailsError) : MerchantDetailsContent
    data class Loaded(val details: MerchantDetails) : MerchantDetailsContent
}

enum class MerchantDetailsTab { OUTLETS, TERMINALS }

private fun Outlet.matches(search: String): Boolean =
    matchesAny(search, name, outletId, address, city, postalCode)

private fun MerchantTerminal.matches(search: String): Boolean =
    matchesAny(search, terminalId, outletName)

private fun matchesAny(search: String, vararg values: String?): Boolean {
    val text = search.trim()
    return text.isEmpty() || values.any { it?.contains(text, ignoreCase = true) == true }
}

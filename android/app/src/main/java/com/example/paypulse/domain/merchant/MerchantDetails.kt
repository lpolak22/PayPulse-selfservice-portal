package com.example.paypulse.domain.merchant

import com.example.paypulse.domain.outlet.Outlet
import com.example.paypulse.domain.terminal.TerminalStatus

data class MerchantDetails(
    val id: Long,
    val merchantId: String?,
    val name: String,
    val oib: String,
    val status: MerchantStatus?,
    val outlets: RelatedItems<Outlet>,
    val terminals: RelatedItems<MerchantTerminal>
)

data class RelatedItems<T>(
    val items: List<T>,
    val total: Long
)

data class MerchantTerminal(
    val id: Long,
    val terminalId: String?,
    val outletName: String?,
    val status: TerminalStatus?
)

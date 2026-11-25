package com.example.paypulse.ui.merchants.details

import com.example.paypulse.domain.merchant.MerchantDetails
import com.example.paypulse.domain.merchant.MerchantStatus
import com.example.paypulse.domain.merchant.MerchantTerminal
import com.example.paypulse.domain.merchant.RelatedItems
import com.example.paypulse.domain.outlet.Outlet
import com.example.paypulse.domain.outlet.OutletStatus
import com.example.paypulse.domain.terminal.TerminalStatus

val ilica = Outlet(11, "PM-0001", "PM Ilica 12", "Ilica 12", "Zagreb", "10000", OutletStatus.ACTIVE, 3)
val avenueMall = Outlet(12, "PM-0002", "PM Avenue Mall", "Avenija Dubrovnik 16", "Zagreb", null, OutletStatus.ACTIVE, 3)
val velikaGorica = Outlet(13, "PM-0004", "PM Velika Gorica", "Zagrebačka 8", "Velika Gorica", null, OutletStatus.DEACTIVATED, 1)

val ilicaTerminal = MerchantTerminal(21, "TK000101", "PM Ilica 12", TerminalStatus.ACTIVE)
val mallTerminal = MerchantTerminal(22, "TK000201", "PM Avenue Mall", TerminalStatus.BLOCKED)

fun merchantDetails(
    id: Long = 7,
    outlets: List<Outlet> = listOf(ilica, avenueMall, velikaGorica),
    terminals: List<MerchantTerminal> = listOf(ilicaTerminal, mallTerminal)
) = MerchantDetails(
    id = id,
    merchantId = "TR-100245",
    name = "Trgovina Kvadrat d.o.o.",
    oib = "12345678901",
    status = MerchantStatus.ACTIVE,
    outlets = RelatedItems(outlets, outlets.size.toLong()),
    terminals = RelatedItems(terminals, terminals.size.toLong())
)

package com.example.paypulse.ui.merchants.details

import com.example.paypulse.domain.merchant.MerchantDetails
import com.example.paypulse.domain.merchant.MerchantDetailsError
import com.example.paypulse.domain.merchant.MerchantDetailsResult
import com.example.paypulse.domain.merchant.MerchantError
import com.example.paypulse.domain.merchant.MerchantQuery
import com.example.paypulse.domain.merchant.MerchantRepository
import com.example.paypulse.domain.merchant.MerchantResult
import com.example.paypulse.domain.merchant.MerchantStatus
import com.example.paypulse.domain.merchant.MerchantTerminal
import com.example.paypulse.domain.merchant.RelatedItems
import com.example.paypulse.domain.outlet.Outlet
import com.example.paypulse.domain.outlet.OutletStatus
import com.example.paypulse.domain.terminal.TerminalStatus

val ilicaOutlet = Outlet(11, "PM-0001", "PM Ilica 12", "Ilica 12", "Zagreb", "10000", OutletStatus.ACTIVE, 3)
val goricaOutlet = Outlet(14, "PM-0004", "PM Velika Gorica", "Zagrebačka 8", "Velika Gorica", null, OutletStatus.DEACTIVATED, 1)
val ilicaTerminal = MerchantTerminal(21, "TK000101", "PM Ilica 12", TerminalStatus.ACTIVE)

fun kvadratDetails(
    outlets: List<Outlet> = listOf(ilicaOutlet, goricaOutlet),
    terminals: List<MerchantTerminal> = listOf(ilicaTerminal),
    outletTotal: Long = outlets.size.toLong(),
    terminalTotal: Long = terminals.size.toLong()
) = MerchantDetails(
    id = 7,
    merchantId = "TR-100245",
    name = "Trgovina Kvadrat d.o.o.",
    oib = "12345678901",
    status = MerchantStatus.ACTIVE,
    outlets = RelatedItems(outlets, outletTotal),
    terminals = RelatedItems(terminals, terminalTotal)
)

class OrganisationMerchantRepository(private val ownMerchants: Map<Long, MerchantDetails>) : MerchantRepository {

    override suspend fun search(query: MerchantQuery): MerchantResult =
        MerchantResult.Failure(MerchantError.UNKNOWN)

    override suspend fun details(id: Long): MerchantDetailsResult =
        ownMerchants[id]?.let { MerchantDetailsResult.Success(it) }
            ?: MerchantDetailsResult.Failure(MerchantDetailsError.NOT_FOUND)
}

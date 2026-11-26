package com.example.paypulse.data.remote.mapper

import com.example.paypulse.data.remote.createApiJson
import com.example.paypulse.data.remote.dto.TransactionDetailsDto
import com.example.paypulse.domain.dashboard.CardBrand
import com.example.paypulse.domain.dashboard.TransactionStatus
import com.example.paypulse.domain.transaction.SalesChannel
import com.example.paypulse.domain.transaction.TransactionCard
import com.example.paypulse.domain.transaction.TransactionDetails
import com.example.paypulse.domain.transaction.TransactionMerchant
import com.example.paypulse.domain.transaction.TransactionOutlet
import com.example.paypulse.domain.transaction.TransactionTerminal
import com.example.paypulse.domain.transaction.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant

private const val FULL_DETAILS = """
    {"id":42,"transactionAt":"2026-10-24T12:32:07Z","amount":42.90,"currency":"EUR","status":"APPROVED",
    "type":"SALE","salesChannel":"CONTACTLESS","authCode":"0A12B3","rrn":"629714503218","acquirer":"Acquirer A",
    "card":{"brand":"VISA","maskedPan":"411111******4821"},
    "terminal":{"id":451,"terminalId":"T-00451","type":"POS"},
    "outlet":{"id":12,"name":"PM Ilica 12","address":"Ilica 12","city":"Zagreb","postalCode":"10000"},
    "merchant":{"id":3,"name":"Trgovina Kvadrat d.o.o."}}
"""

private const val SPARSE_DETAILS = """
    {"id":43,"transactionAt":"2026-10-24T12:32:07Z","amount":5,"currency":null,"status":"SETTLED",
    "type":"PREAUTH","salesChannel":null,"authCode":"  ","rrn":null,"acquirer":null,
    "card":{"brand":"UNIONPAY","maskedPan":null},
    "terminal":{"id":451,"terminalId":"T-00451","type":null},
    "outlet":{"id":12,"name":"PM Ilica 12","address":null,"city":null,"postalCode":null},
    "merchant":{"id":3,"name":"Trgovina Kvadrat d.o.o."}}
"""

class TransactionDetailsMapperTest {

    private val json = createApiJson()

    private fun mapped(body: String): TransactionDetails =
        json.decodeFromString<TransactionDetailsDto>(body).toTransactionDetails()

    @Test
    fun fullResponse_mapsEveryField() {
        val expected = TransactionDetails(
            id = 42,
            transactionAt = Instant.parse("2026-10-24T12:32:07Z"),
            amount = BigDecimal("42.90"),
            currency = "EUR",
            status = TransactionStatus.APPROVED,
            type = TransactionType.SALE,
            salesChannel = SalesChannel.CONTACTLESS,
            authCode = "0A12B3",
            rrn = "629714503218",
            acquirer = "Acquirer A",
            card = TransactionCard(CardBrand.VISA, "411111******4821"),
            terminal = TransactionTerminal(451, "T-00451", "POS"),
            outlet = TransactionOutlet(12, "PM Ilica 12", "Ilica 12", "Zagreb", "10000"),
            merchant = TransactionMerchant(3, "Trgovina Kvadrat d.o.o.")
        )
        assertEquals(expected, mapped(FULL_DETAILS))
    }

    @Test
    fun missingSalesChannelAndCardNumber_mapToNull() {
        val details = mapped(SPARSE_DETAILS)
        assertNull(details.salesChannel)
        assertNull(details.card.maskedPan)
    }

    @Test
    fun blankOrMissingTexts_mapToNull() {
        val details = mapped(SPARSE_DETAILS)
        assertNull(details.currency)
        assertNull(details.authCode)
        assertNull(details.rrn)
        assertNull(details.acquirer)
        assertNull(details.terminal.type)
        assertNull(details.outlet.address)
    }

    @Test
    fun unknownValues_fallBack() {
        val details = mapped(SPARSE_DETAILS)
        assertNull(details.status)
        assertEquals(TransactionType.OTHER, details.type)
        assertEquals(CardBrand.OTHER, details.card.brand)
    }

    @Test
    fun cardNumber_staysMaskedAsTheBackendSentIt() {
        assertEquals("411111******4821", mapped(FULL_DETAILS).card.maskedPan)
    }
}

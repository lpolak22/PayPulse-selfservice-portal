package com.example.paypulse.ui.transactions.details

import com.example.paypulse.domain.dashboard.CardBrand
import com.example.paypulse.domain.dashboard.TransactionStatus
import com.example.paypulse.domain.transaction.SalesChannel
import com.example.paypulse.domain.transaction.TransactionCard
import com.example.paypulse.domain.transaction.TransactionDetails
import com.example.paypulse.domain.transaction.TransactionDetailsRepository
import com.example.paypulse.domain.transaction.TransactionDetailsResult
import com.example.paypulse.domain.transaction.TransactionMerchant
import com.example.paypulse.domain.transaction.TransactionOutlet
import com.example.paypulse.domain.transaction.TransactionTerminal
import com.example.paypulse.domain.transaction.TransactionType
import java.math.BigDecimal
import java.time.Instant

class StubTransactionDetailsRepository : TransactionDetailsRepository {

    val requestedIds = mutableListOf<Long>()
    private val queued = ArrayDeque<TransactionDetailsResult>()
    var defaultResult: TransactionDetailsResult = TransactionDetailsResult.Success(details())

    fun enqueue(vararg results: TransactionDetailsResult) {
        queued.addAll(results)
    }

    override suspend fun getDetails(id: Long): TransactionDetailsResult {
        requestedIds += id
        return queued.removeFirstOrNull() ?: defaultResult
    }

    companion object {
        fun details(id: Long = 42) = TransactionDetails(
            id = id,
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
    }
}

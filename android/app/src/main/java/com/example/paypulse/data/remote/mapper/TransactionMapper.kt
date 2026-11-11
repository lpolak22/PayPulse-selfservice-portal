package com.example.paypulse.data.remote.mapper

import com.example.paypulse.data.remote.dto.PageResponseDto
import com.example.paypulse.data.remote.dto.TransactionDto
import com.example.paypulse.domain.dashboard.TransactionStatus
import com.example.paypulse.domain.transaction.SalesChannel
import com.example.paypulse.domain.transaction.Transaction
import com.example.paypulse.domain.transaction.TransactionPage
import com.example.paypulse.domain.transaction.TransactionType
import java.time.Instant

fun PageResponseDto<TransactionDto>.toTransactionPage(): TransactionPage = TransactionPage(
    items = items.map { it.toTransaction() },
    page = page,
    pageSize = pageSize,
    total = total
)

fun TransactionDto.toTransaction(): Transaction = Transaction(
    id = id,
    transactionAt = Instant.parse(transactionAt),
    amount = amount,
    currency = currency,
    status = enumOrNull<TransactionStatus>(status),
    type = enumOrNull<TransactionType>(type) ?: TransactionType.OTHER,
    cardBrand = cardBrandOf(cardBrand),
    maskedPan = maskedPan,
    salesChannel = enumOrNull<SalesChannel>(salesChannel),
    outletName = outletName,
    terminalId = terminalId,
    authCode = authCode,
    rrn = rrn
)

private inline fun <reified T : Enum<T>> enumOrNull(value: String?): T? =
    enumValues<T>().firstOrNull { it.name == value }

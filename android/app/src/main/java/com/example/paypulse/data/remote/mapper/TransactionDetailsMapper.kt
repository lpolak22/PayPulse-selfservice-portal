package com.example.paypulse.data.remote.mapper

import com.example.paypulse.data.remote.dto.TransactionCardDto
import com.example.paypulse.data.remote.dto.TransactionDetailsDto
import com.example.paypulse.data.remote.dto.TransactionMerchantDto
import com.example.paypulse.data.remote.dto.TransactionOutletDto
import com.example.paypulse.data.remote.dto.TransactionTerminalDto
import com.example.paypulse.domain.dashboard.TransactionStatus
import com.example.paypulse.domain.transaction.SalesChannel
import com.example.paypulse.domain.transaction.TransactionCard
import com.example.paypulse.domain.transaction.TransactionDetails
import com.example.paypulse.domain.transaction.TransactionMerchant
import com.example.paypulse.domain.transaction.TransactionOutlet
import com.example.paypulse.domain.transaction.TransactionTerminal
import com.example.paypulse.domain.transaction.TransactionType
import java.time.Instant

fun TransactionDetailsDto.toTransactionDetails(): TransactionDetails = TransactionDetails(
    id = id,
    transactionAt = Instant.parse(transactionAt),
    amount = amount,
    currency = currency.orNullIfBlank(),
    status = enumOrNull<TransactionStatus>(status),
    type = enumOrNull<TransactionType>(type) ?: TransactionType.OTHER,
    salesChannel = enumOrNull<SalesChannel>(salesChannel),
    authCode = authCode.orNullIfBlank(),
    rrn = rrn.orNullIfBlank(),
    acquirer = acquirer.orNullIfBlank(),
    card = card.toTransactionCard(),
    terminal = terminal.toTransactionTerminal(),
    outlet = outlet.toTransactionOutlet(),
    merchant = merchant.toTransactionMerchant()
)

private fun TransactionCardDto.toTransactionCard(): TransactionCard =
    TransactionCard(brand = cardBrandOf(brand), maskedPan = maskedPan.orNullIfBlank())

private fun TransactionTerminalDto.toTransactionTerminal(): TransactionTerminal =
    TransactionTerminal(id = id, terminalId = terminalId.orNullIfBlank(), type = type.orNullIfBlank())

private fun TransactionOutletDto.toTransactionOutlet(): TransactionOutlet = TransactionOutlet(
    id = id,
    name = name.orNullIfBlank(),
    address = address.orNullIfBlank(),
    city = city.orNullIfBlank(),
    postalCode = postalCode.orNullIfBlank()
)

private fun TransactionMerchantDto.toTransactionMerchant(): TransactionMerchant =
    TransactionMerchant(id = id, name = name.orNullIfBlank())

private fun String?.orNullIfBlank(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

package com.example.paypulse.ui.components

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

internal val CroatianLocale: Locale = Locale.forLanguageTag("hr-HR")

private val THOUSAND = BigDecimal(1_000)
private val MILLION = BigDecimal(1_000_000)

object CroatianFormat {
    private val euro: Currency = Currency.getInstance("EUR")

    fun money(amount: BigDecimal): String =
        NumberFormat.getCurrencyInstance(CroatianLocale).apply { currency = euro }.format(amount)

    fun count(value: Int): String = NumberFormat.getIntegerInstance(CroatianLocale).format(value)

    fun percent(ratio: BigDecimal): String = percentFormat().format(ratio)

    fun signedPercent(ratio: BigDecimal): String = percentFormat().apply { positivePrefix = "+" }.format(ratio)

    fun wholePercent(percent: Int): String = NumberFormat.getPercentInstance(CroatianLocale).format(percent / 100.0)

    fun compactAmount(amount: BigDecimal): String = when {
        amount >= MILLION -> oneDecimal().format(amount.divide(MILLION, 1, RoundingMode.HALF_UP)) + "M"
        amount >= THOUSAND -> oneDecimal().format(amount.divide(THOUSAND, 1, RoundingMode.HALF_UP)) + "k"
        else -> count(amount.setScale(0, RoundingMode.HALF_UP).toInt())
    }

    private fun oneDecimal(): DecimalFormat = DecimalFormat("#,##0.0", DecimalFormatSymbols(CroatianLocale))

    private fun percentFormat(): DecimalFormat = (NumberFormat.getPercentInstance(CroatianLocale) as DecimalFormat).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }
}

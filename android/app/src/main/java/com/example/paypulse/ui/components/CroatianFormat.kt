package com.example.paypulse.ui.components

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CroatianFormat {
    private val locale: Locale = Locale.forLanguageTag("hr-HR")
    private val euro: Currency = Currency.getInstance("EUR")

    fun money(amount: BigDecimal): String =
        NumberFormat.getCurrencyInstance(locale).apply { currency = euro }.format(amount)

    fun count(value: Int): String = NumberFormat.getIntegerInstance(locale).format(value)

    fun percent(ratio: BigDecimal): String = percentFormat().format(ratio)

    fun signedPercent(ratio: BigDecimal): String = percentFormat().apply { positivePrefix = "+" }.format(ratio)

    private fun percentFormat(): DecimalFormat = (NumberFormat.getPercentInstance(locale) as DecimalFormat).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }
}

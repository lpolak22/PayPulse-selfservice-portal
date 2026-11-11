package com.example.paypulse.ui.transactions

import com.example.paypulse.ui.components.CroatianLocale
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object TransactionDateFormat {
    private val date = DateTimeFormatter.ofPattern("dd.MM.yyyy.", CroatianLocale)
    private val time = DateTimeFormatter.ofPattern("HH:mm", CroatianLocale)
    private val dateTime = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm", CroatianLocale)

    fun date(value: LocalDate): String = date.format(value)

    fun time(value: LocalDateTime): String = time.format(value)

    fun dateTime(value: LocalDateTime): String = dateTime.format(value)
}

package com.example.paypulse.ui.components

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

object CroatianDateFormat {
    private val dayMonth = DateTimeFormatter.ofPattern("d. M.", CroatianLocale)
    private val compactDayMonth = DateTimeFormatter.ofPattern("d.M.", CroatianLocale)
    private val hourMinute = DateTimeFormatter.ofPattern("H:mm", CroatianLocale)
    private val clockTime = DateTimeFormatter.ofPattern("HH:mm", CroatianLocale)
    private val dayMonthTime = DateTimeFormatter.ofPattern("d. M. HH:mm", CroatianLocale)

    fun weekdayShort(date: LocalDate): String = date.dayOfWeek.getDisplayName(TextStyle.SHORT, CroatianLocale).capitalized()

    fun weekdayWithDate(date: LocalDate): String =
        "${date.dayOfWeek.getDisplayName(TextStyle.FULL, CroatianLocale).capitalized()}, ${dayMonth.format(date)}"

    fun compactDate(date: LocalDate): String = compactDayMonth.format(date)

    fun hour(time: LocalDateTime): String = hourMinute.format(time)

    fun hourRange(start: LocalDateTime): String = "${hourMinute.format(start)}–${hourMinute.format(start.plusHours(1))}"

    fun time(dateTime: LocalDateTime): String = clockTime.format(dateTime)

    fun dateTime(dateTime: LocalDateTime): String = dayMonthTime.format(dateTime)

    private fun String.capitalized(): String = replaceFirstChar { it.titlecase(CroatianLocale) }
}

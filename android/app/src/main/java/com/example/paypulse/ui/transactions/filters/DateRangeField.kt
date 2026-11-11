@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.paypulse.ui.transactions.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.dashboard.DateRange
import com.example.paypulse.ui.transactions.TransactionDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private const val SELECTABLE_YEARS_BACK = 2

@Composable
fun DateRangeField(
    range: DateRange,
    today: LocalDate,
    onRangeChange: (DateRange) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        DateBox(
            label = stringResource(R.string.transactions_date_from),
            value = TransactionDateFormat.date(range.from),
            onClick = { showPicker = true },
            modifier = Modifier.weight(1f)
        )
        DateBox(
            label = stringResource(R.string.transactions_date_to),
            value = TransactionDateFormat.date(range.to),
            onClick = { showPicker = true },
            modifier = Modifier.weight(1f)
        )
    }
    if (showPicker) {
        DateRangeDialog(
            range = range,
            today = today,
            onConfirm = { onRangeChange(it); showPicker = false },
            onDismiss = { showPicker = false }
        )
    }
}

@Composable
private fun DateBox(label: String, value: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedCard(onClick = onClick, modifier = modifier.heightIn(min = 56.dp)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun DateRangeDialog(
    range: DateRange,
    today: LocalDate,
    onConfirm: (DateRange) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = range.from.toUtcMillis(),
        initialSelectedEndDateMillis = range.to.toUtcMillis(),
        yearRange = (today.year - SELECTABLE_YEARS_BACK)..today.year,
        selectableDates = UpToToday(today)
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { state.toDateRange()?.let(onConfirm) }, enabled = state.selectedEndDateMillis != null) {
                Text(stringResource(R.string.transactions_date_picker_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.transactions_date_picker_cancel)) }
        }
    ) {
        DateRangePicker(state = state, title = { PickerTitle() }, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PickerTitle() {
    Text(
        text = stringResource(R.string.transactions_date_picker_title),
        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)
    )
}

private class UpToToday(today: LocalDate) : SelectableDates {
    private val lastSelectableMillis = today.toUtcMillis()
    private val lastSelectableYear = today.year

    override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= lastSelectableMillis

    override fun isSelectableYear(year: Int): Boolean = year <= lastSelectableYear
}

private fun DateRangePickerState.toDateRange(): DateRange? {
    val start = selectedStartDateMillis ?: return null
    val end = selectedEndDateMillis ?: return null
    return DateRange(from = start.toUtcDate(), to = end.toUtcDate())
}

private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toUtcDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

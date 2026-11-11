package com.example.paypulse.ui.transactions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.paypulse.R
import com.example.paypulse.domain.transaction.SalesChannel
import com.example.paypulse.domain.transaction.TransactionError
import com.example.paypulse.domain.transaction.TransactionSort
import com.example.paypulse.domain.transaction.TransactionType
import com.example.paypulse.ui.dashboard.label

@StringRes
fun TransactionType.label(): Int = when (this) {
    TransactionType.SALE -> R.string.transactions_type_sale
    TransactionType.SALE_WITH_INSTALLMENTS -> R.string.transactions_type_sale_with_installments
    TransactionType.REFUND -> R.string.transactions_type_refund
    TransactionType.STORNO -> R.string.transactions_type_storno
    TransactionType.TECHNICAL_REVERSAL -> R.string.transactions_type_technical_reversal
    TransactionType.OTHER -> R.string.transactions_type_other
}

@StringRes
fun SalesChannel.label(): Int = when (this) {
    SalesChannel.POS -> R.string.transactions_channel_pos
    SalesChannel.ECOM -> R.string.transactions_channel_ecom
    SalesChannel.MOTO -> R.string.transactions_channel_moto
    SalesChannel.CONTACTLESS -> R.string.transactions_channel_contactless
}

@StringRes
fun TransactionSort.label(): Int = when (this) {
    TransactionSort.NEWEST_FIRST -> R.string.transactions_sort_newest
    TransactionSort.OLDEST_FIRST -> R.string.transactions_sort_oldest
    TransactionSort.HIGHEST_AMOUNT_FIRST -> R.string.transactions_sort_highest_amount
    TransactionSort.LOWEST_AMOUNT_FIRST -> R.string.transactions_sort_lowest_amount
}

@StringRes
fun FilterOption.label(): Int = when (this) {
    is FilterOption.Status -> value.label()
    is FilterOption.Type -> value.label()
    is FilterOption.Brand -> value.label()
    is FilterOption.Channel -> value.label()
}

@DrawableRes
fun TransactionError.icon(): Int = when (this) {
    TransactionError.NETWORK -> R.drawable.ic_cloud_off
    TransactionError.INVALID_FILTER, TransactionError.UNKNOWN -> R.drawable.ic_error_outline
}

@StringRes
fun TransactionError.title(): Int = when (this) {
    TransactionError.NETWORK -> R.string.transactions_error_network_title
    TransactionError.INVALID_FILTER -> R.string.transactions_error_invalid_filter_title
    TransactionError.UNKNOWN -> R.string.transactions_error_unknown_title
}

@StringRes
fun TransactionError.body(): Int = when (this) {
    TransactionError.NETWORK -> R.string.transactions_error_network_body
    TransactionError.INVALID_FILTER -> R.string.transactions_error_invalid_filter_body
    TransactionError.UNKNOWN -> R.string.transactions_error_unknown_body
}

package com.example.paypulse.ui.transactions.details

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.paypulse.R
import com.example.paypulse.ui.components.DetailLinkRow
import com.example.paypulse.ui.components.DetailRow
import com.example.paypulse.ui.components.DetailSection
import com.example.paypulse.ui.dashboard.label
import com.example.paypulse.ui.transactions.label

private const val VISIBLE_CARD_DIGITS = 4

@Composable
fun TransactionInfoSection(details: TransactionDetailsModel) {
    DetailSection(
        title = stringResource(R.string.transaction_details_section_transaction),
        rows = listOfNotNull(
            infoRow(stringResource(R.string.transaction_details_type), stringResource(details.type.label())),
            infoRow(stringResource(R.string.transaction_details_auth_code), details.authCode),
            infoRow(stringResource(R.string.transaction_details_rrn), details.rrn),
            infoRow(
                stringResource(R.string.transaction_details_sales_channel),
                details.salesChannel?.let { stringResource(it.label()) }
            ),
            infoRow(stringResource(R.string.transaction_details_acquirer), details.acquirer)
        )
    )
}

@Composable
fun CardSection(details: TransactionDetailsModel) {
    val cardDescription = details.cardNumber?.let {
        stringResource(R.string.transaction_details_card_number_description, it.takeLast(VISIBLE_CARD_DIGITS))
    }
    DetailSection(
        title = stringResource(R.string.transaction_details_section_card),
        rows = listOfNotNull(
            infoRow(stringResource(R.string.transaction_details_brand), stringResource(details.cardBrand.label())),
            infoRow(stringResource(R.string.transaction_details_card_number), details.cardNumber, cardDescription)
        )
    )
}

@Composable
fun LinkSection(title: String, link: DetailsLink, onClickLabel: String, onClick: (Long) -> Unit) {
    val text = link.labelParts
        .joinToString(stringResource(R.string.transactions_row_separator))
        .ifEmpty { stringResource(R.string.transactions_status_unknown) }
    DetailSection(
        title = title,
        rows = listOf<@Composable () -> Unit>(
            { DetailLinkRow(text = text, onClickLabel = onClickLabel, onClick = { onClick(link.id) }) }
        )
    )
}

private fun infoRow(label: String, value: String?, valueDescription: String? = null): (@Composable () -> Unit)? {
    if (value == null) return null
    return { DetailRow(label = label, value = value, valueDescription = valueDescription) }
}

package com.example.paypulse.ui.transactions

import androidx.annotation.StringRes
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.paypulse.R
import com.example.paypulse.domain.dashboard.CardBrand
import com.example.paypulse.domain.dashboard.TransactionStatus
import com.example.paypulse.domain.transaction.SalesChannel
import com.example.paypulse.domain.transaction.Transaction
import com.example.paypulse.domain.transaction.TransactionSort
import com.example.paypulse.domain.transaction.TransactionType
import com.example.paypulse.ui.components.CroatianFormat
import com.example.paypulse.ui.dashboard.label
import com.example.paypulse.ui.theme.PayPulseTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

private const val FULL_PAN = "4111111111111011"
private const val PAN_FIRST_DIGITS = "411111"
private const val LAST_DIGITS = "1011"
private val sampleToday = LocalDate.of(2026, 11, 11)
private val sampleAmount = BigDecimal("1234.56")
private val sampleLocalTime = LocalDateTime.of(2026, 11, 11, 13, 41)

private val sampleTransaction = Transaction(
    id = 1,
    transactionAt = Instant.parse("2026-11-11T12:41:00Z"),
    amount = sampleAmount,
    currency = "EUR",
    status = TransactionStatus.APPROVED,
    type = TransactionType.SALE,
    cardBrand = CardBrand.VISA,
    maskedPan = FULL_PAN,
    salesChannel = SalesChannel.POS,
    outletName = "Zagreb Centar",
    terminalId = "T000121",
    authCode = "A1B2C1",
    rrn = "615312345671"
)

private fun hasSingleDescriptionStartingWith(prefix: String) =
    SemanticsMatcher("one content description starting with \"$prefix\"") { node ->
        val descriptions = node.config.getOrNull(SemanticsProperties.ContentDescription).orEmpty()
        descriptions.size == 1 && descriptions.single().startsWith(prefix)
    }

private fun hasClickLabel(label: String) = SemanticsMatcher("OnClick label is \"$label\"") { node ->
    node.config.getOrNull(SemanticsActions.OnClick)?.label == label
}

private fun descriptionMatches(test: String, predicate: (String) -> Boolean) = SemanticsMatcher(test) { node ->
    node.config.getOrNull(SemanticsProperties.ContentDescription).orEmpty().all(predicate)
}

@RunWith(AndroidJUnit4::class)
class TransactionRowScreenReaderTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

    @Test
    fun XP6_row_reads_status_amount_card_datetime_in_order() {
        showRow()
        val expectedStart = listOf(
            text(TransactionStatus.APPROVED.label()),
            CroatianFormat.money(sampleAmount),
            maskedCard(),
            TransactionDateFormat.dateTime(sampleLocalTime)
        ).joinToString(text(R.string.transactions_description_separator))

        composeRule.onNode(hasClickAction()).assert(hasSingleDescriptionStartingWith(expectedStart))
    }

    @Test
    fun XP6_row_click_label_is_otvori_detalje() {
        showRow()

        composeRule.onNode(hasClickAction()).assert(hasClickLabel("Otvori detalje"))
    }

    @Test
    fun XP6_row_reads_card_masked() {
        showRow()

        composeRule.onNodeWithContentDescription(maskedCard(), substring = true)
            .assert(descriptionMatches("never contains the full card number") { !it.contains(PAN_FIRST_DIGITS) })
    }

    private fun showRow() {
        val row = sampleTransaction.toRowModel()
        composeRule.setContent {
            PayPulseTheme { TransactionRow(row = row, onClick = {}) }
        }
    }

    private fun Transaction.toRowModel(): TransactionRowModel =
        listOf(this).toListItems(TransactionSort.NEWEST_FIRST, sampleToday)
            .filterIsInstance<TransactionListItem.Row>()
            .single()
            .row

    private fun maskedCard(): String = resources.getString(R.string.transactions_card, text(CardBrand.VISA.label()), LAST_DIGITS)

    private fun text(@StringRes id: Int): String = resources.getString(id)
}

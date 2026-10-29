package com.example.paypulse.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.auth.PasswordPolicy
import com.example.paypulse.domain.auth.PasswordRule

@Composable
fun PasswordRuleChecklist(
    metRules: Set<PasswordRule>,
    highlightUnmetRules: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PasswordRule.entries.forEach { rule ->
            val isMet = rule in metRules
            PasswordRuleRow(label = rule.label(), isMet = isMet, isHighlighted = highlightUnmetRules && !isMet)
        }
    }
}

@Composable
private fun PasswordRuleRow(label: String, isMet: Boolean, isHighlighted: Boolean) {
    val state = stringResource(if (isMet) R.string.password_rule_met else R.string.password_rule_unmet)
    val icon = if (isMet) R.drawable.ic_check_circle else R.drawable.ic_radio_button_unchecked
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { stateDescription = state },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = ruleIconColor(isMet, isHighlighted)
        )
        Spacer(Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = ruleTextColor(isMet, isHighlighted))
    }
}

@Composable
private fun ruleIconColor(isMet: Boolean, isHighlighted: Boolean): Color = when {
    isMet -> MaterialTheme.colorScheme.primary
    isHighlighted -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
private fun ruleTextColor(isMet: Boolean, isHighlighted: Boolean): Color = when {
    isMet -> MaterialTheme.colorScheme.onSurface
    isHighlighted -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
private fun PasswordRule.label(): String = when (this) {
    PasswordRule.MIN_LENGTH -> pluralStringResource(
        R.plurals.password_rule_min_length,
        PasswordPolicy.MIN_LENGTH,
        PasswordPolicy.MIN_LENGTH
    )
    PasswordRule.UPPERCASE -> stringResource(R.string.password_rule_uppercase)
    PasswordRule.LOWERCASE -> stringResource(R.string.password_rule_lowercase)
    PasswordRule.DIGIT -> stringResource(R.string.password_rule_digit)
}

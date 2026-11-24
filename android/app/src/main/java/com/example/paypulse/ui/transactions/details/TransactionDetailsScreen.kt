package com.example.paypulse.ui.transactions.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.transaction.TransactionDetailsError
import com.example.paypulse.ui.components.BackTopBar
import com.example.paypulse.ui.components.LoadingScreen
import com.example.paypulse.ui.components.StatusMessage

@Composable
fun TransactionDetailsScreen(
    state: TransactionDetailsUiState,
    actions: TransactionDetailsActions,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            BackTopBar(
                title = stringResource(R.string.transaction_details_title),
                backDescription = stringResource(R.string.transaction_details_back),
                onBackClick = actions.onBackClick
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                TransactionDetailsUiState.Loading -> LoadingScreen()
                TransactionDetailsUiState.NotFound -> NotFoundMessage(onBackClick = actions.onBackClick)
                is TransactionDetailsUiState.Error -> ErrorMessage(error = state.error, onRetry = actions.onRetry)
                is TransactionDetailsUiState.Loaded -> TransactionDetailsContent(details = state.details, actions = actions)
            }
        }
    }
}

@Composable
private fun TransactionDetailsContent(details: TransactionDetailsModel, actions: TransactionDetailsActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        TransactionDetailsHeader(details = details)
        TransactionInfoSection(details = details)
        CardSection(details = details)
        LinkSection(
            title = stringResource(R.string.transaction_details_section_terminal),
            link = details.terminal,
            onClickLabel = stringResource(R.string.transaction_details_open_terminal),
            onClick = actions.onTerminalClick
        )
        LinkSection(
            title = stringResource(R.string.transaction_details_section_outlet),
            link = details.outlet,
            onClickLabel = stringResource(R.string.transaction_details_open_outlet),
            onClick = actions.onOutletClick
        )
    }
}

@Composable
private fun NotFoundMessage(onBackClick: () -> Unit) {
    val error = TransactionDetailsError.NOT_FOUND
    StatusMessage(
        icon = R.drawable.ic_search,
        title = stringResource(error.title()),
        body = stringResource(error.body()),
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        MessageButton(text = stringResource(R.string.transaction_details_back), onClick = onBackClick)
    }
}

@Composable
private fun ErrorMessage(error: TransactionDetailsError, onRetry: () -> Unit) {
    StatusMessage(
        icon = error.icon(),
        title = stringResource(error.title()),
        body = stringResource(error.body()),
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        MessageButton(text = stringResource(R.string.transactions_retry), onClick = onRetry)
    }
}

@Composable
private fun MessageButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.heightIn(min = 48.dp)) {
        Text(text = text)
    }
}

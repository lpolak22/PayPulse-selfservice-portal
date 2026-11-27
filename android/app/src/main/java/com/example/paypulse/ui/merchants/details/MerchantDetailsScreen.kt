package com.example.paypulse.ui.merchants.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.paypulse.R
import com.example.paypulse.domain.merchant.MerchantDetails
import com.example.paypulse.domain.merchant.MerchantDetailsError
import com.example.paypulse.ui.components.StatusMessage

@Composable
fun MerchantDetailsScreen(
    state: MerchantDetailsUiState,
    onEvent: (MerchantDetailsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { DetailsTopBar(onBackClick = { onEvent(MerchantDetailsEvent.BackClicked) }) },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            MerchantBreadcrumb(
                merchantName = state.details?.name,
                onMerchantsClick = { onEvent(MerchantDetailsEvent.MerchantsClicked) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Box(modifier = Modifier.weight(1f)) {
                when (val content = state.content) {
                    MerchantDetailsContent.Loading -> MerchantDetailsSkeleton()
                    is MerchantDetailsContent.Error -> ErrorMessage(error = content.error, onEvent = onEvent)
                    is MerchantDetailsContent.Loaded -> DetailsList(details = content.details, state = state, onEvent = onEvent)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsTopBar(onBackClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.merchant_details_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.merchant_details_back)
                )
            }
        },
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
    )
}

@Composable
private fun DetailsList(details: MerchantDetails, state: MerchantDetailsUiState, onEvent: (MerchantDetailsEvent) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 24.dp)
    ) {
        item(key = "profile") { MerchantProfileCard(details = details, modifier = Modifier.padding(bottom = 24.dp)) }
        item(key = "tabs") {
            MerchantDetailsTabs(
                details = details,
                selected = state.selectedTab,
                onSelect = { onEvent(MerchantDetailsEvent.TabSelected(it)) }
            )
        }
        relatedItemsSection(state = state, onEvent = onEvent)
    }
}

@Composable
private fun ErrorMessage(error: MerchantDetailsError, onEvent: (MerchantDetailsEvent) -> Unit) {
    val isNotFound = error == MerchantDetailsError.NOT_FOUND
    StatusMessage(
        icon = error.icon(),
        title = stringResource(error.title()),
        body = stringResource(error.body()),
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        Button(
            onClick = { onEvent(if (isNotFound) MerchantDetailsEvent.MerchantsClicked else MerchantDetailsEvent.Retry) },
            modifier = Modifier.heightIn(min = 48.dp)
        ) {
            Text(stringResource(if (isNotFound) R.string.merchant_details_back_to_merchants else R.string.merchants_retry))
        }
    }
}

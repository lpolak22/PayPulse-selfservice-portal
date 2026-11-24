package com.example.paypulse.ui.shell

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.example.paypulse.ui.merchants.MerchantsRoute
import com.example.paypulse.ui.merchants.details.MerchantDetailsNavigation
import com.example.paypulse.ui.merchants.details.MerchantDetailsRoute

const val MERCHANT_DETAILS_LINK_BASE = "paypulse://merchants"

internal fun NavGraphBuilder.merchantsDestinations(navController: NavHostController, onMenuClick: () -> Unit) {
    composable<ShellRoute.Merchants> {
        MerchantsRoute(
            onMenuClick = onMenuClick,
            onMerchantClick = { id -> navController.navigate(ShellRoute.MerchantDetails(id)) }
        )
    }
    composable<ShellRoute.MerchantDetails>(
        deepLinks = listOf(navDeepLink<ShellRoute.MerchantDetails>(basePath = MERCHANT_DETAILS_LINK_BASE))
    ) {
        MerchantDetailsRoute(
            navigation = MerchantDetailsNavigation(
                onBackClick = { navController.navigateUp() },
                onMerchantsClick = { navController.navigateBackToMerchants() },
                onOutletClick = {},
                onTerminalClick = {}
            )
        )
    }
}

private fun NavHostController.navigateBackToMerchants() {
    if (!popBackStack<ShellRoute.Merchants>(inclusive = false)) navigateToTopLevel(ShellRoute.Merchants)
}

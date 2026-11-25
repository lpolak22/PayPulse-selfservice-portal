package com.example.paypulse.ui.shell

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.paypulse.domain.user.User
import com.example.paypulse.ui.accessibility.AccessibilitySettingsRoute
import com.example.paypulse.ui.accessibility.AccessibilitySettingsViewModel
import com.example.paypulse.ui.dashboard.DashboardRoute
import com.example.paypulse.ui.merchants.MerchantsRoute
import com.example.paypulse.ui.transactions.TransactionsRoute
import com.example.paypulse.ui.transactions.details.TransactionDetailsRoute
import com.example.paypulse.ui.users.UserManagementNavHost

@Composable
fun AppShellNavHost(
    navController: NavHostController,
    user: User,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = ShellRoute.Home, modifier = modifier) {
        composable<ShellRoute.Home> {
            DashboardRoute(
                firstName = user.firstName,
                onMenuClick = onMenuClick,
                onShowAllTransactions = { navController.navigateToTopLevel(ShellRoute.Transactions) }
            )
        }
        composable<ShellRoute.Merchants> {
            MerchantsRoute(onMenuClick = onMenuClick, onMerchantClick = {})
        }
        comingSoon<ShellRoute.Terminals>(DrawerItem.TERMINALS, onMenuClick)
        transactionDestinations(navController, onMenuClick)
        comingSoon<ShellRoute.Reports>(DrawerItem.REPORTS, onMenuClick)
        composable<ShellRoute.Users> {
            UserManagementNavHost(currentUserRole = user.role, onMenuClick = onMenuClick)
        }
        comingSoon<ShellRoute.Info>(DrawerItem.INFO, onMenuClick)
        comingSoon<ShellRoute.Support>(DrawerItem.SUPPORT, onMenuClick)
        comingSoon<ShellRoute.Settings>(DrawerItem.SETTINGS, onMenuClick)
        composable<ShellRoute.Accessibility> {
            AccessibilitySettingsRoute(
                onMenuClick = onMenuClick,
                viewModel = viewModel(factory = AccessibilitySettingsViewModel.Factory)
            )
        }
    }
}

private fun NavGraphBuilder.transactionDestinations(navController: NavHostController, onMenuClick: () -> Unit) {
    composable<ShellRoute.Transactions> { entry ->
        TransactionsRoute(
            onMenuClick = onMenuClick,
            onTransactionClick = { id -> navController.navigateFrom(entry, ShellRoute.TransactionDetails(id)) }
        )
    }
    composable<ShellRoute.TransactionDetails> { entry ->
        TransactionDetailsRoute(
            transactionId = entry.toRoute<ShellRoute.TransactionDetails>().id,
            onBackClick = { navController.popFrom(entry) },
            onTerminalClick = {},
            onOutletClick = {}
        )
    }
}

private inline fun <reified T : Any> NavGraphBuilder.comingSoon(item: DrawerItem, noinline onMenuClick: () -> Unit) {
    composable<T> { ComingSoonScreen(title = stringResource(item.label), onMenuClick = onMenuClick) }
}

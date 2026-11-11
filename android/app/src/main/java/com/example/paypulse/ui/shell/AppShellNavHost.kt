package com.example.paypulse.ui.shell

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.paypulse.domain.user.User
import com.example.paypulse.ui.dashboard.DashboardRoute
import com.example.paypulse.ui.transactions.TransactionsRoute
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
        comingSoon<ShellRoute.Merchants>(DrawerItem.MERCHANTS, onMenuClick)
        comingSoon<ShellRoute.Terminals>(DrawerItem.TERMINALS, onMenuClick)
        composable<ShellRoute.Transactions> {
            TransactionsRoute(onMenuClick = onMenuClick, onTransactionClick = {})
        }
        comingSoon<ShellRoute.Reports>(DrawerItem.REPORTS, onMenuClick)
        composable<ShellRoute.Users> {
            UserManagementNavHost(currentUserRole = user.role, onMenuClick = onMenuClick)
        }
        comingSoon<ShellRoute.Info>(DrawerItem.INFO, onMenuClick)
        comingSoon<ShellRoute.Support>(DrawerItem.SUPPORT, onMenuClick)
        comingSoon<ShellRoute.Settings>(DrawerItem.SETTINGS, onMenuClick)
    }
}

private inline fun <reified T : Any> NavGraphBuilder.comingSoon(item: DrawerItem, noinline onMenuClick: () -> Unit) {
    composable<T> { ComingSoonScreen(title = stringResource(item.label), onMenuClick = onMenuClick) }
}

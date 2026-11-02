package com.example.paypulse.ui.shell

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.paypulse.domain.user.User
import kotlinx.coroutines.launch

@Composable
fun AppShell(user: User, onLogoutClick: () -> Unit, modifier: Modifier = Modifier) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    ModalNavigationDrawer(
        drawerState = drawerState,
        modifier = modifier,
        drawerContent = {
            ModalDrawerSheet(
                drawerState = drawerState,
                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical + WindowInsetsSides.Start)
            ) {
                AppDrawerContent(
                    user = user,
                    selectedItem = backStackEntry?.destination?.toDrawerItem(),
                    onItemClick = { item ->
                        scope.launch { drawerState.close() }
                        navController.navigateToTopLevel(item.route)
                    },
                    onLogoutClick = onLogoutClick
                )
            }
        }
    ) {
        AppShellNavHost(
            navController = navController,
            userRole = user.role,
            onMenuClick = { scope.launch { drawerState.open() } }
        )
    }
}

private fun NavDestination.toDrawerItem(): DrawerItem? =
    DrawerItem.entries.firstOrNull { item -> hierarchy.any { it.hasRoute(item.route::class) } }

private fun NavHostController.navigateToTopLevel(route: ShellRoute) {
    navigate(route) {
        popUpTo<ShellRoute.Home> { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

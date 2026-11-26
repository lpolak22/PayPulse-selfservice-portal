package com.example.paypulse.ui.shell

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.util.Consumer
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
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
    AppLinkNavigationEffect(navController)
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
            user = user,
            onMenuClick = { scope.launch { drawerState.open() } }
        )
    }
}

private fun NavDestination.toDrawerItem(): DrawerItem? =
    if (hasRoute(ShellRoute.MerchantDetails::class)) {
        DrawerItem.MERCHANTS
    } else {
        DrawerItem.entries.firstOrNull { item -> hierarchy.any { it.hasRoute(item.route::class) } }
    }

@Composable
private fun AppLinkNavigationEffect(navController: NavHostController) {
    val activity = LocalContext.current.findActivity() ?: return
    DisposableEffect(activity, navController) {
        val listener = Consumer<Intent> { intent -> navController.handleDeepLink(intent) }
        activity.addOnNewIntentListener(listener)
        onDispose { activity.removeOnNewIntentListener(listener) }
    }
}

private tailrec fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

package com.example.paypulse.ui.shell

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController

internal fun NavHostController.navigateToTopLevel(route: ShellRoute) {
    navigate(route) {
        popUpTo<ShellRoute.Home> { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

internal fun NavHostController.navigateFrom(entry: NavBackStackEntry, route: ShellRoute) {
    if (entry.isResumed()) navigate(route) { launchSingleTop = true }
}

internal fun NavHostController.popFrom(entry: NavBackStackEntry) {
    if (entry.isResumed()) popBackStack()
}

private fun NavBackStackEntry.isResumed(): Boolean = lifecycle.currentState == Lifecycle.State.RESUMED

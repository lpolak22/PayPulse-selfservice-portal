package com.example.paypulse.ui.shell

import androidx.navigation.NavHostController

internal fun NavHostController.navigateToTopLevel(route: ShellRoute) {
    navigate(route) {
        popUpTo<ShellRoute.Home> { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

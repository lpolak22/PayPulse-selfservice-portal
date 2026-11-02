package com.example.paypulse.ui.shell

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.paypulse.R
import com.example.paypulse.domain.user.UserRole

enum class DrawerGroup { MAIN, SECONDARY }

enum class DrawerItem(
    @get:StringRes val label: Int,
    @get:DrawableRes val icon: Int,
    val route: ShellRoute,
    val group: DrawerGroup,
    val requiredRole: UserRole? = null
) {
    HOME(R.string.navigation_home, R.drawable.ic_home, ShellRoute.Home, DrawerGroup.MAIN),
    MERCHANTS(R.string.navigation_merchants, R.drawable.ic_store, ShellRoute.Merchants, DrawerGroup.MAIN),
    TERMINALS(R.string.navigation_terminals, R.drawable.ic_point_of_sale, ShellRoute.Terminals, DrawerGroup.MAIN),
    TRANSACTIONS(R.string.navigation_transactions, R.drawable.ic_swap_horiz, ShellRoute.Transactions, DrawerGroup.MAIN),
    REPORTS(R.string.navigation_reports, R.drawable.ic_description, ShellRoute.Reports, DrawerGroup.MAIN),
    USERS(R.string.navigation_users, R.drawable.ic_group, ShellRoute.Users, DrawerGroup.MAIN, UserRole.ADMIN),
    INFO(R.string.navigation_info, R.drawable.ic_info, ShellRoute.Info, DrawerGroup.SECONDARY),
    SUPPORT(R.string.navigation_support, R.drawable.ic_help, ShellRoute.Support, DrawerGroup.SECONDARY),
    SETTINGS(R.string.navigation_settings, R.drawable.ic_settings, ShellRoute.Settings, DrawerGroup.SECONDARY);

    fun isVisibleFor(role: UserRole): Boolean = requiredRole == null || requiredRole == role

    companion object {
        fun visibleFor(role: UserRole, group: DrawerGroup): List<DrawerItem> =
            entries.filter { it.group == group && it.isVisibleFor(role) }
    }
}

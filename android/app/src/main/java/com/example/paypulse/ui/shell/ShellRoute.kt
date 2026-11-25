package com.example.paypulse.ui.shell

import kotlinx.serialization.Serializable

sealed interface ShellRoute {
    @Serializable data object Home : ShellRoute
    @Serializable data object Merchants : ShellRoute
    @Serializable data object Terminals : ShellRoute
    @Serializable data object Transactions : ShellRoute
    @Serializable data class TransactionDetails(val id: Long) : ShellRoute
    @Serializable data object Reports : ShellRoute
    @Serializable data object Users : ShellRoute
    @Serializable data object Info : ShellRoute
    @Serializable data object Support : ShellRoute
    @Serializable data object Settings : ShellRoute
    @Serializable data object Accessibility : ShellRoute
}

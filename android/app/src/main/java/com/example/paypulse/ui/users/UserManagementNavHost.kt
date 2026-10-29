package com.example.paypulse.ui.users

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.paypulse.R
import com.example.paypulse.domain.user.UserRole
import com.example.paypulse.ui.users.form.EditUserRoute
import com.example.paypulse.ui.users.form.NewUserRoute
import com.example.paypulse.ui.users.list.UsersListRoute
import kotlinx.coroutines.launch

private object UserRoutes {
    const val LIST = "users"
    const val NEW = "users/new"
    const val USER_ID = "userId"
    const val EDIT = "users/edit/{$USER_ID}"
    fun edit(userId: Long) = "users/edit/$userId"
}

@Composable
fun UserManagementNavHost(
    currentUserRole: UserRole,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    if (currentUserRole != UserRole.ADMIN) {
        AccessDeniedMessage(modifier)
        return
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val messages = UserMessages(
        created = stringResource(R.string.users_created_message),
        saved = stringResource(R.string.users_saved_message),
        deleted = stringResource(R.string.users_deleted_message)
    )
    val showMessage: (String) -> Unit = { text -> scope.launch { snackbarHostState.showSnackbar(text) } }
    NavHost(navController = navController, startDestination = UserRoutes.LIST, modifier = modifier) {
        usersListDestination(navController, snackbarHostState)
        newUserDestination(navController) { showMessage(messages.created) }
        editUserDestination(
            navController = navController,
            onSaved = { showMessage(messages.saved) },
            onDeleted = { showMessage(messages.deleted) }
        )
    }
}

private class UserMessages(val created: String, val saved: String, val deleted: String)

private fun NavGraphBuilder.usersListDestination(navController: NavHostController, snackbarHostState: SnackbarHostState) {
    composable(UserRoutes.LIST) {
        UsersListRoute(
            onAddUser = { navController.navigate(UserRoutes.NEW) { launchSingleTop = true } },
            onUserClick = { id -> navController.navigate(UserRoutes.edit(id)) { launchSingleTop = true } },
            snackbarHostState = snackbarHostState
        )
    }
}

private fun NavGraphBuilder.newUserDestination(navController: NavHostController, onCreated: () -> Unit) {
    composable(UserRoutes.NEW) { entry ->
        NewUserRoute(
            onBackClick = { navController.popFrom(entry) },
            onUserCreated = {
                navController.popFrom(entry)
                onCreated()
            }
        )
    }
}

private fun NavGraphBuilder.editUserDestination(
    navController: NavHostController,
    onSaved: () -> Unit,
    onDeleted: () -> Unit
) {
    composable(
        route = UserRoutes.EDIT,
        arguments = listOf(navArgument(UserRoutes.USER_ID) { type = NavType.LongType })
    ) { entry ->
        EditUserRoute(
            userId = entry.arguments?.getLong(UserRoutes.USER_ID) ?: 0L,
            onBackClick = { navController.popFrom(entry) },
            onUserSaved = {
                navController.popFrom(entry)
                onSaved()
            },
            onUserDeleted = {
                navController.popFrom(entry)
                onDeleted()
            }
        )
    }
}

private fun NavHostController.popFrom(entry: NavBackStackEntry) {
    if (entry.lifecycle.currentState == Lifecycle.State.RESUMED) popBackStack()
}

@Composable
private fun AccessDeniedMessage(modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.users_access_denied),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
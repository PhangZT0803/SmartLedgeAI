package com.user.smartledgerai.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Rule
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Login : Screen("login", "Login", Icons.Default.Dashboard)
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Verify : Screen("verify", "Verify", Icons.Default.Rule)
    object History : Screen("history", "History", Icons.Default.History)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)

    object AppSelection : Screen("appSelection", "AppSelection", Icons.Default.Apps)
}

sealed class ProfileScreenNavigationAction{
    object GoToAppSelection: ProfileScreenNavigationAction()
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Verify,
    Screen.History,
    Screen.Profile
)

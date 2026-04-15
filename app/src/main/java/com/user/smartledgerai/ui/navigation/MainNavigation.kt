package com.user.smartledgerai.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.user.smartledgerai.ui.screens.AppSelectionScreen
import com.user.smartledgerai.ui.screens.DashboardScreen
import com.user.smartledgerai.ui.screens.HistoryScreen
import com.user.smartledgerai.ui.screens.OnBoardingScreen
import com.user.smartledgerai.ui.screens.ProfileScreen
import com.user.smartledgerai.ui.screens.VerifyScreen
import com.user.smartledgerai.viewmodel.AuthViewModel
import com.user.smartledgerai.viewmodel.ProfileViewModel
import timber.log.Timber

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val user by authViewModel.user.collectAsState()
    if (user == null) {
        Timber.d("Nav:User is null")
    }
    Scaffold(
        bottomBar = {
            if(user != null){
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable (Screen.Login.route){ OnBoardingScreen(authViewModel)}
            composable(Screen.Dashboard.route) { DashboardScreen() }
            composable(Screen.Verify.route) { VerifyScreen() }
            composable(Screen.History.route) { HistoryScreen() }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    profileViewModel,
                    onAction= { action ->
                        when (action) {
                            ProfileScreenNavigationAction.GoToAppSelection -> navController.navigate(Screen.AppSelection.route)
                        }
                    })
            }
            composable(Screen.AppSelection.route) { AppSelectionScreen() }
        }
    }
    //总结先进来一定是Login->(检查)->清除Login->进入Dashboard
    LaunchedEffect(user) {
        if (user != null) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Login.route) { inclusive = true }  // 清掉登录页，按返回不会回到登录
            }
        }
    }
}
@Preview
@Composable
fun MainNavigationPreview() {
    MainNavigation()
}

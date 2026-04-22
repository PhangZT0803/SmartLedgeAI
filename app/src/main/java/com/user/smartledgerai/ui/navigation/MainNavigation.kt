package com.user.smartledgerai.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.user.smartledgerai.ui.viewmodel.VerifyViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.user.smartledgerai.ui.screens.DashboardScreen
import com.user.smartledgerai.ui.screens.HistoryScreen
import com.user.smartledgerai.ui.screens.ProfileScreen
import com.user.smartledgerai.ui.screens.VerifyScreen
import com.user.smartledgerai.ui.screens.InsightsScreen

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding()
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            if (screen.route == Screen.Verify.route) {
                                val verifyViewModel: VerifyViewModel = hiltViewModel()
                                val pendingCount by verifyViewModel.pendingTransactions.collectAsState()
                                BadgedBox(
                                    badge = {
                                        if (pendingCount.isNotEmpty()) {
                                            Badge { Text(pendingCount.size.toString()) }
                                        }
                                    }
                                ) {
                                    Icon(screen.icon, contentDescription = null)
                                }
                            } else {
                                Icon(screen.icon, contentDescription = null)
                            }
                        },
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) { 
                DashboardScreen(
                    onNavigateToHistory = {
                        navController.navigate(Screen.History.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToInsights = {
                        navController.navigate(Screen.Insights.route) {
                            launchSingleTop = true
                        }
                    }
                ) 
            }
            composable(Screen.Verify.route) { 
                VerifyScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToHistory = {
                        navController.navigate(Screen.History.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                ) 
            }
            composable(Screen.History.route) { HistoryScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
            composable(Screen.Insights.route) { 
                InsightsScreen(onNavigateBack = { navController.popBackStack() }) 
            }
        }
    }
}

package com.user.smartledgerai.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.user.smartledgerai.data.Transaction
import com.user.smartledgerai.ui.screens.AppSelectionScreen
import com.user.smartledgerai.ui.screens.CategoriesScreen
import com.user.smartledgerai.ui.screens.DashboardScreen
import com.user.smartledgerai.ui.screens.HistoryScreen
import com.user.smartledgerai.ui.screens.InsightsScreen
import com.user.smartledgerai.ui.screens.newtransaction.NewTransactionScreen
import com.user.smartledgerai.ui.screens.OnBoardingScreen
import com.user.smartledgerai.ui.screens.ProfileScreen
import com.user.smartledgerai.ui.screens.verify.VerifyScreen
import com.user.smartledgerai.viewmodel.AuthViewModel
import com.user.smartledgerai.viewmodel.ProfileViewModel
import com.user.smartledgerai.viewmodel.TransactionViewModel

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val transactionViewModel: TransactionViewModel = hiltViewModel()

    val user by authViewModel.user.collectAsState()
    val transactions = transactionViewModel.transactions.collectAsState()
    // filter returns a list
    val transactionsToEdit: List<Transaction> = transactions.value.filter {
        !it.isVerified
    }

    val hasUnverified = transactionsToEdit.isNotEmpty()
    val unverifiedCount = transactionsToEdit.size

    var selectedTransactionForEdit by remember { mutableStateOf<Transaction?>(null) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showGlobalFab = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.History.route,
        Screen.Verify.route
    )
    val showBottomBar = currentRoute in listOf(
    Screen.Dashboard.route,
    Screen.Verify.route,
    Screen.History.route,
    Screen.Profile.route
    )
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            if (showGlobalFab) {
                FloatingActionButton(
                    onClick = {
                        selectedTransactionForEdit = null // Reset for new transaction
                        navController.navigate(Screen.NewTransaction.route)
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New")
                }
            }
        },
        bottomBar = {
            if(showBottomBar){
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                tonalElevation = 8.dp,
                shadowElevation = 16.dp
            ) {
                NavigationBar(
                    containerColor = Color.Transparent
                ) {
                    val currentDestination = navBackStackEntry?.destination
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                if (screen.route == Screen.Verify.route && hasUnverified) {
                                    // Numbered badge on Verify icon
                                    BadgedBox(badge = {
                                        Badge(
                                            containerColor = Color(0xFFEF4444)
                                        ) {
                                            Text(
                                                "$unverifiedCount",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                                ),
                                                color = Color.White
                                            )
                                        }
                                    }) {
                                        Icon(
                                            screen.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        screen.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    screen.label,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth / 3 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth / 3 },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            composable(Screen.Login.route) { OnBoardingScreen(authViewModel) }
            composable(
                Screen.Dashboard.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) },
                popEnterTransition = { fadeIn(animationSpec = tween(300)) },
                popExitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                DashboardScreen(
                    transactionViewModel,
                    onAction = { navController.navigate(Screen.History.route) },
                    onInsights = { navController.navigate(Screen.Insights.route) }
                )
            }
            composable(Screen.Verify.route) {
                VerifyScreen(
                    transactionsToEdit,
                    transactionViewModel,
                    onBack = { navController.popBackStack() },
                    onEditManually = { transaction ->
                        selectedTransactionForEdit = transaction
                        navController.navigate(Screen.NewTransaction.route)
                    }
                )
            }
            composable(Screen.Insights.route) {
                InsightsScreen(
                    transactionViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                Screen.NewTransaction.route,
                enterTransition = { slideInVertically(initialOffsetY = { it }) + fadeIn() },
                popExitTransition = { slideOutVertically(targetOffsetY = { it }) + fadeOut() }
            ) {
                NewTransactionScreen(
                    transactionViewModel,
                    isEditMode = selectedTransactionForEdit != null,
                    transactionToEdit = selectedTransactionForEdit,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    transactionViewModel,
                    onEditTransaction = { transaction ->
                        selectedTransactionForEdit = transaction
                        navController.navigate(Screen.NewTransaction.route)
                    }
                )
            }
            composable(
                Screen.Profile.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) },
            ) {
                ProfileScreen(
                    profileViewModel,
                    authViewModel,
                    onAction = { action ->
                        when (action) {
                            ProfileScreenNavigationAction.GoToAppSelection -> navController.navigate(
                                Screen.AppSelection.route
                            )

                            ProfileScreenNavigationAction.GoToCategories -> navController.navigate(
                                Screen.Categories.route
                            )
                        }
                    }
                )
            }
            composable(Screen.Categories.route) { CategoriesScreen(transactionViewModel) }
            composable(Screen.AppSelection.route) { AppSelectionScreen() }
        }
    }

    LaunchedEffect(user) {
        if (user != null) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        } else {
            // 👈 user 变成 null（即触发了 signOut）：自动回登录页，并清空栈
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }
}

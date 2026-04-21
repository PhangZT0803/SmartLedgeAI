package com.user.smartledgerai.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.user.smartledgerai.data.Transaction
import com.user.smartledgerai.ui.screens.AppSelectionScreen
import com.user.smartledgerai.ui.screens.CategoriesScreen
import com.user.smartledgerai.ui.screens.DashboardScreen
import com.user.smartledgerai.ui.screens.HistoryScreen
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
    //filter 返回的是一个list
    val transactionsToEdit: List<Transaction> = transactions.value.filter{
        !it.isVerified
        }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(Screen.NewTransaction.route)},
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "New")
            }
        },
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
            modifier = Modifier.padding(innerPadding),

                    // 1. 进入新页面时：从右向左滑入 + 淡入
                    enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            // 2. 离开当前页面时：向左轻微滑动推出 + 淡出
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth / 3 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            // 3. 按返回键回到上个页面时：从左向右滑入 + 淡入
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth / 3 },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            // 4. 按返回键退出当前页面时：向右滑出 + 淡出
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            composable (Screen.Login.route){ OnBoardingScreen(authViewModel)}
            composable(
                Screen.Dashboard.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) },
                popEnterTransition = { fadeIn(animationSpec = tween(300)) },
                popExitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                DashboardScreen(
                    transactionViewModel,
                    onAction={
                        navController.navigate(Screen.History.route)
                    }
                )
            }
            composable(Screen.Verify.route) {
                VerifyScreen(
                    transactionsToEdit,
                    transactionViewModel,
                    onBack = { navController.popBackStack() }
                    )
            }
            composable(
                Screen.NewTransaction.route,
                // 从底部弹出
                enterTransition = { slideInVertically(initialOffsetY = { it }) + fadeIn() },
                // 向底部收起
                popExitTransition = { slideOutVertically(targetOffsetY = { it }) + fadeOut() }
            ) {
                NewTransactionScreen(
                    transactionViewModel,
                    false,
                    null)
            }
            composable(Screen.History.route) { HistoryScreen() }
            composable(
                Screen.Profile.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) },
            ) {
                ProfileScreen(
                    profileViewModel,
                    authViewModel,
                    onAction= { action ->
                        when (action) {
                            ProfileScreenNavigationAction.GoToAppSelection -> navController.navigate(Screen.AppSelection.route)
                            ProfileScreenNavigationAction.GoToCategories -> navController.navigate(Screen.Categories.route)
                        }
                    }
                )
            }
            composable(Screen.Categories.route) { CategoriesScreen(transactionViewModel)}
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

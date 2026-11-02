package com.example.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.StreakViewModel
import com.example.ui.screens.*

sealed class Screen(val route: String, val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    object Onboarding : Screen("onboarding", "Onboarding", Icons.Filled.Home, Icons.Outlined.Home)
    object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Insights : Screen("insights", "Insights", Icons.Filled.BarChart, Icons.Outlined.BarChart)
    object Social : Screen("social", "Circles", Icons.Filled.Group, Icons.Outlined.Group)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)

    object TaskDetail : Screen("task_detail/{habitId}", "Task Detail", Icons.Filled.Home, Icons.Outlined.Home) {
        fun createRoute(habitId: String) = "task_detail/$habitId"
    }
    object CreateHabit : Screen("create_habit", "Create Circle", Icons.Filled.Home, Icons.Outlined.Home)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    viewModel: StreakViewModel,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
    val startDest = if (isOnboardingCompleted) Screen.Home.route else Screen.Onboarding.route

    val toastMessage by viewModel.toastMessage.collectAsState()

    val bottomBarScreens = listOf(
        Screen.Home,
        Screen.Insights,
        Screen.Social,
        Screen.Profile
    )

    val showBottomBar = bottomBarScreens.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("bottom_nav_bar")
                ) {
                    bottomBarScreens.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        },
        snackbarHost = {
            toastMessage?.let { msg ->
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(2800)
                    viewModel.clearToast()
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .padding(16.dp)
                        .testTag("snackbar_toast")
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDest,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> (fullWidth * 0.25f).toInt() },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> (-fullWidth * 0.2f).toInt() },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                ) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> (-fullWidth * 0.25f).toInt() },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> (fullWidth * 0.2f).toInt() },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                ) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
            },
            modifier = Modifier.padding(
                bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp
            )
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    viewModel = viewModel,
                    onFinished = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onHabitClick = { habitId ->
                        navController.navigate(Screen.TaskDetail.createRoute(habitId))
                    },
                    onCreateHabitClick = {
                        navController.navigate(Screen.CreateHabit.route)
                    }
                )
            }

            composable(Screen.Insights.route) {
                InsightsScreen(viewModel = viewModel)
            }

            composable(Screen.Social.route) {
                SocialScreen(viewModel = viewModel)
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = onToggleDarkTheme
                )
            }

            composable(
                route = Screen.TaskDetail.route,
                arguments = listOf(navArgument("habitId") { type = NavType.StringType })
            ) { backStackEntry ->
                val habitId = backStackEntry.arguments?.getString("habitId") ?: ""
                TaskDetailScreen(
                    habitId = habitId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.CreateHabit.route) {
                CreateHabitScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

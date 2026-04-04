package com.calorieai.app.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.calorieai.app.ui.components.BottomNavBar
import com.calorieai.app.ui.components.NavItem

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Overview : Screen("overview")
    object Functions : Screen("functions")
    object My : Screen("my")
    object BodyProfile : Screen("body_profile")
    object WeightHistory : Screen("weight_history")
    object HealthGoals : Screen("health_goals")
    object AddMethodSelector : Screen("add_method_selector?date={date}") {
        fun createRoute(date: String? = null): String {
            return optionalQueryRoute("add_method_selector", "date", date)
        }
    }
    object FavoriteRecipes : Screen("favorite_recipes")
    object FavoriteRecipesManager : Screen("favorite_recipes_manager")
    object PantryIngredientsManager : Screen("pantry_ingredients_manager")
    object RecipePlanManager : Screen("recipe_plan_manager")
    object ManualAdd : Screen("manual_add")
    object AddFood : Screen("add_food?date={date}") {
        fun createRoute(date: String? = null): String {
            return optionalQueryRoute("add_food", "date", date)
        }
    }
    object Camera : Screen("camera")
    object PhotoAnalysis : Screen("photo_analysis/{photoUri}") {
        fun createRoute(photoUri: String): String {
            val encoded = android.net.Uri.encode(photoUri)
            return "photo_analysis/$encoded"
        }
    }
    object Result : Screen("result/{recordId}") {
        fun createRoute(recordId: String) = "result/$recordId"
    }
    object Stats : Screen("stats")
    object Settings : Screen("settings")
    object AppearanceSettings : Screen("appearance_settings")
    object InteractionSettings : Screen("interaction_settings")
    object NotificationSettings : Screen("notification_settings")
    object BackupSettings : Screen("backup_settings")
    object AISettings : Screen("ai_settings")
    object AIModelCallStats : Screen("ai_model_call_stats")
    object AIConfigDetail : Screen("ai_config_detail?configId={configId}") {
        fun createRoute(configId: String? = null): String {
            return optionalQueryRoute("ai_config_detail", "configId", configId)
        }
    }
    object About : Screen("about")
    object Profile : Screen("profile")
    object AIChat : Screen("ai_chat?sessionId={sessionId}") {
        fun createRoute(sessionId: String? = null): String {
            return optionalQueryRoute("ai_chat", "sessionId", sessionId)
        }
    }
    object WeightRecord : Screen("weight_record?date={date}") {
        fun createRoute(date: String? = null): String {
            return optionalQueryRoute("weight_record", "date", date)
        }
    }
    object ExerciseRecord : Screen("exercise_record?date={date}") {
        fun createRoute(date: String? = null): String {
            return optionalQueryRoute("exercise_record", "date", date)
        }
    }
    object WaterTracker : Screen("water_tracker")
    object WaterHistory : Screen("water_history?date={date}") {
        fun createRoute(date: String? = null): String {
            return optionalQueryRoute("water_history", "date", date)
        }
    }
}

private fun optionalQueryRoute(base: String, key: String, value: String?): String {
    return if (!value.isNullOrBlank()) "$base?$key=$value" else base
}

val bottomNavItems = listOf(
    NavItem(
        route = Screen.Home.route,
        title = "首页",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    NavItem(
        route = Screen.FavoriteRecipes.route,
        title = "菜谱",
        selectedIcon = Icons.Filled.RestaurantMenu,
        unselectedIcon = Icons.Outlined.RestaurantMenu
    ),
    NavItem(
        route = Screen.Overview.route,
        title = "概览",
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart
    ),
    NavItem(
        route = Screen.My.route,
        title = "我的",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
)

val bottomNavScreens = listOf(
    Screen.Home.route,
    Screen.FavoriteRecipes.route,
    Screen.Overview.route,
    Screen.My.route
)

@Composable
fun NavGraph(navController: NavHostController) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bottomNavBehaviorViewModel: BottomNavBehaviorViewModel = hiltViewModel()
    val bottomNavBehavior by bottomNavBehaviorViewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavScreens

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    items = bottomNavItems,
                    selectedRoute = currentRoute ?: Screen.Home.route,
                    onItemSelected = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onItemLongPressed = { route ->
                        when (route) {
                            Screen.Home.route -> {
                                if (bottomNavBehavior.enableLongPressHomeToAdd) {
                                    navController.navigate(Screen.AddMethodSelector.createRoute()) {
                                        launchSingleTop = true
                                    }
                                }
                            }
                            Screen.Overview.route -> {
                                if (bottomNavBehavior.enableLongPressOverviewToStats) {
                                    navController.navigate(Screen.Stats.route) {
                                        launchSingleTop = true
                                    }
                                }
                            }
                            Screen.My.route -> {
                                if (bottomNavBehavior.enableLongPressMyToProfileEdit) {
                                    navController.navigate(Screen.Profile.route) {
                                        launchSingleTop = true
                                    }
                                }
                            }
                            else -> Unit // 菜谱长按功能暂未定义
                        }
                    },
                    isDark = isDark,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            registerAppRoutes(
                navController = navController,
                bottomNavScreens = bottomNavScreens
            )
        }
    }
}

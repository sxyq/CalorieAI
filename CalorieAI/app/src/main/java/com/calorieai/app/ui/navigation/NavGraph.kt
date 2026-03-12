package com.calorieai.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.calorieai.app.ui.screens.add.AddFoodScreen
import com.calorieai.app.ui.screens.camera.CameraScreen
import com.calorieai.app.ui.screens.home.HomeScreen
import com.calorieai.app.ui.screens.result.ResultScreen
import com.calorieai.app.ui.screens.settings.SettingsScreen
import com.calorieai.app.ui.screens.stats.StatsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddFood : Screen("add_food")
    object Camera : Screen("camera")
    object Result : Screen("result/{recordId}") {
        fun createRoute(recordId: String) = "result/$recordId"
    }
    object Stats : Screen("stats")
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAdd = {
                    navController.navigate(Screen.AddFood.route)
                },
                onNavigateToStats = {
                    navController.navigate(Screen.Stats.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToResult = { recordId ->
                    navController.navigate(Screen.Result.createRoute(recordId))
                }
            )
        }
        
        composable(Screen.AddFood.route) {
            AddFoodScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToResult = { recordId ->
                    navController.navigate(Screen.Result.createRoute(recordId)) {
                        popUpTo(Screen.AddFood.route) { inclusive = true }
                    }
                },
                onNavigateToCamera = {
                    navController.navigate(Screen.Camera.route)
                }
            )
        }
        
        composable(Screen.Camera.route) {
            CameraScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPhotoTaken = { uri ->
                    // 拍照后返回添加页面，可以传递识别结果
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Result.route) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getString("recordId") ?: ""
            ResultScreen(
                recordId = recordId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Stats.route) {
            StatsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

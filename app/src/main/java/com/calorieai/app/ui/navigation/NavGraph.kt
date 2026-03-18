package com.calorieai.app.ui.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.calorieai.app.ui.components.BottomNavBar
import com.calorieai.app.ui.components.NavItem
import com.calorieai.app.ui.screens.add.AddFoodScreen
import com.calorieai.app.ui.screens.add.AddMethodSelectorScreen
import com.calorieai.app.ui.screens.add.ManualAddScreen
import com.calorieai.app.ui.screens.ai.AIChatScreen
import com.calorieai.app.ui.screens.camera.CameraScreen
import com.calorieai.app.ui.screens.camera.PhotoAnalysisScreen
import com.calorieai.app.ui.screens.home.HomeScreen
import com.calorieai.app.ui.screens.overview.OverviewScreen
import com.calorieai.app.ui.screens.functions.FunctionsScreen
import com.calorieai.app.ui.screens.profile.MyScreen
import com.calorieai.app.ui.screens.profile.BodyProfileScreen
import com.calorieai.app.ui.screens.profile.WeightHistoryScreen
import com.calorieai.app.ui.screens.profile.WaterHistoryScreen
import com.calorieai.app.ui.screens.profile.HealthGoalsScreen
import com.calorieai.app.ui.screens.result.ResultScreen
import com.calorieai.app.ui.screens.settings.AboutScreen
import com.calorieai.app.ui.screens.settings.AIConfigDetailScreen
import com.calorieai.app.ui.screens.settings.AISettingsScreen
import com.calorieai.app.ui.screens.settings.AppearanceSettingsScreen
import com.calorieai.app.ui.screens.settings.BackupSettingsScreen
import com.calorieai.app.ui.screens.settings.InteractionSettingsScreen
import com.calorieai.app.ui.screens.settings.NotificationSettingsScreen
import com.calorieai.app.ui.screens.settings.ProfileScreen
import com.calorieai.app.ui.screens.settings.SettingsScreen
import com.calorieai.app.ui.screens.stats.StatsScreen
import com.calorieai.app.ui.screens.weight.WeightRecordScreen
import com.calorieai.app.ui.screens.exercise.ExerciseRecordScreen
import com.calorieai.app.ui.screens.favorites.FavoriteRecipeScreen
import com.calorieai.app.ui.screens.water.WaterTrackerScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import com.calorieai.app.data.repository.FoodRecordRepository

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Overview : Screen("overview")
    object Functions : Screen("functions")
    object My : Screen("my")
    object BodyProfile : Screen("body_profile")
    object WeightHistory : Screen("weight_history")
    object HealthGoals : Screen("health_goals")
    object AddMethodSelector : Screen("add_method_selector")
    object ManualAdd : Screen("manual_add")
    object AddFood : Screen("add_food?date={date}") {
        fun createRoute(date: String? = null): String {
            return if (date != null) {
                "add_food?date=$date"
            } else {
                "add_food"
            }
        }
    }
    object Camera : Screen("camera")
    object PhotoAnalysis : Screen("photo_analysis/{photoUri}") {
        fun createRoute(photoUri: String) = "photo_analysis/$photoUri"
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
    object AIConfigDetail : Screen("ai_config_detail?configId={configId}") {
        fun createRoute(configId: String? = null): String {
            return if (configId != null) {
                "ai_config_detail?configId=$configId"
            } else {
                "ai_config_detail"
            }
        }
    }
    object About : Screen("about")
    object Profile : Screen("profile")
    object AIChat : Screen("ai_chat")
    object WeightRecord : Screen("weight_record")
    object ExerciseRecord : Screen("exercise_record")
    object WaterTracker : Screen("water_tracker")
    object FavoriteRecipes : Screen("favorite_recipes")
    
    object WaterHistory : Screen("water_history")
}

val bottomNavItems = listOf(
    NavItem(
        route = Screen.Home.route,
        title = "首页",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
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
    Screen.Overview.route,
    Screen.My.route
)

@Composable
fun NavGraph(navController: NavHostController) {
    val isDark = isSystemInDarkTheme()
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
            // 首页 - 展示今天吃了什么
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToAdd = {
                        navController.navigate(Screen.AddMethodSelector.route)
                    },
                    onNavigateToStats = {
                        navController.navigate(Screen.Stats.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onNavigateToResult = { recordId ->
                        navController.navigate(Screen.Result.createRoute(recordId))
                    },
                    onNavigateToAIChat = {
                        navController.navigate(Screen.AIChat.route)
                    }
                )
            }

            // 概览页 - 热力图 + 本月总结
            composable(Screen.Overview.route) {
                OverviewScreen(
                    onNavigateToStats = {
                        navController.navigate(Screen.Stats.route)
                    },
                    onNavigateToWeightHistory = {
                        navController.navigate(Screen.WeightHistory.route)
                    },
                    onNavigateToGoals = {
                        navController.navigate(Screen.HealthGoals.route)
                    }
                )
            }

            // 功能页 - 快捷功能入口
            composable(Screen.Functions.route) {
                FunctionsScreen(
                    onNavigateToFoodDiary = {
                        navController.navigate(Screen.AddMethodSelector.route)
                    },
                    onNavigateToWeightRecord = {
                        navController.navigate(Screen.WeightRecord.route)
                    },
                    onNavigateToWaterTracker = {
                        navController.navigate(Screen.WaterTracker.route)
                    },
                    onNavigateToExercise = {
                        navController.navigate(Screen.ExerciseRecord.route)
                    },
                    onNavigateToStatistics = {
                        navController.navigate(Screen.Stats.route)
                    },
                    onNavigateToAIAssistant = {
                        navController.navigate(Screen.AIChat.route)
                    },
                    onNavigateToRecipes = {},
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            // 我的页面
            composable(Screen.My.route) {
                MyScreen(
                    onNavigateToBodyProfile = {
                        navController.navigate(Screen.BodyProfile.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            // 体重历史页面
            composable(Screen.WeightHistory.route) {
                WeightHistoryScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 健康目标页面
            composable(Screen.HealthGoals.route) {
                HealthGoalsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 身体档案页面
            composable(Screen.BodyProfile.route) {
                BodyProfileScreen(
                    onEditClick = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onNavigateToWeightHistory = {
                        navController.navigate(Screen.WeightRecord.route)
                    },
                    onNavigateToGoals = {
                        navController.navigate(Screen.HealthGoals.route)
                    }
                )
            }

            // 添加方式选择
            composable(Screen.AddMethodSelector.route) {
                AddMethodSelectorScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToManual = {
                        navController.navigate(Screen.ManualAdd.route)
                    },
                    onNavigateToAI = {
                        navController.navigate(Screen.AddFood.route)
                    },
                    onNavigateToWeight = {
                        navController.navigate(Screen.WeightRecord.route)
                    },
                    onNavigateToExercise = {
                        navController.navigate(Screen.ExerciseRecord.route)
                    },
                    onNavigateToWaterHistory = {
                        navController.navigate(Screen.WaterHistory.route)
                    },
                    onNavigateToFavoriteRecipes = {
                        navController.navigate(Screen.FavoriteRecipes.route)
                    }
                )
            }

            // 手动添加
            composable(Screen.ManualAdd.route) {
                ManualAddScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onSaveComplete = {
                        navController.popBackStack(Screen.Home.route, false)
                    }
                )
            }

            // AI添加食物
            composable(
                route = Screen.AddFood.route,
                arguments = listOf(
                    androidx.navigation.navArgument("date") {
                        type = androidx.navigation.NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val date = backStackEntry.arguments?.getString("date")
                AddFoodScreen(
                    selectedDate = date,
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
            
            // 相机
            composable(Screen.Camera.route) {
                CameraScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onPhotoTaken = { uri ->
                        navController.navigate(Screen.PhotoAnalysis.createRoute(uri.toString()))
                    }
                )
            }

            // 照片分析
            composable(Screen.PhotoAnalysis.route) { backStackEntry ->
                val photoUriString = backStackEntry.arguments?.getString("photoUri") ?: ""
                val photoUri = android.net.Uri.parse(photoUriString)
                PhotoAnalysisScreen(
                    photoUri = photoUri,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onSaveComplete = {
                        navController.popBackStack(Screen.Home.route, false)
                    }
                )
            }
            
            // 结果页面
            composable(Screen.Result.route) { backStackEntry ->
                val recordId = backStackEntry.arguments?.getString("recordId") ?: ""
                ResultScreen(
                    recordId = recordId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            // 收藏菜谱页面
            composable(Screen.FavoriteRecipes.route) {
                FavoriteRecipeScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onAddToRecord = { _, _ ->
                        // 导航回首页
                        navController.popBackStack(Screen.Home.route, false)
                    }
                )
            }
            
            // 统计页面
            composable(Screen.Stats.route) {
                StatsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            // 设置页面
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToAppearance = {
                        navController.navigate(Screen.AppearanceSettings.route)
                    },
                    onNavigateToInteraction = {
                        navController.navigate(Screen.InteractionSettings.route)
                    },
                    onNavigateToNotification = {
                        navController.navigate(Screen.NotificationSettings.route)
                    },
                    onNavigateToBackup = {
                        navController.navigate(Screen.BackupSettings.route)
                    },
                    onNavigateToAISettings = {
                        navController.navigate(Screen.AISettings.route)
                    },
                    onNavigateToAbout = {
                        navController.navigate(Screen.About.route)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    }
                )
            }

            // 外观设置
            composable(Screen.AppearanceSettings.route) {
                AppearanceSettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 交互设置
            composable(Screen.InteractionSettings.route) {
                InteractionSettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 通知设置
            composable(Screen.NotificationSettings.route) {
                NotificationSettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 备份设置
            composable(Screen.BackupSettings.route) {
                BackupSettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // AI设置
            composable(Screen.AISettings.route) {
                AISettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToDetail = { configId ->
                        navController.navigate(Screen.AIConfigDetail.createRoute(configId))
                    }
                )
            }

            // AI配置详情
            composable(Screen.AIConfigDetail.route) { backStackEntry ->
                val configId = backStackEntry.arguments?.getString("configId")
                AIConfigDetailScreen(
                    configId = configId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 关于页面
            composable(Screen.About.route) {
                AboutScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // AI聊天
            composable(Screen.AIChat.route) {
                AIChatScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 个人资料
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 体重记录
            composable(Screen.WeightRecord.route) {
                WeightRecordScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 运动记录
            composable(Screen.ExerciseRecord.route) {
                ExerciseRecordScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 饮水记录
            composable(Screen.WaterTracker.route) {
                WaterTrackerScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 饮水历史
            composable(Screen.WaterHistory.route) {
                WaterHistoryScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

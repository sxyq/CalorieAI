package com.calorieai.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.calorieai.app.ui.screens.add.AddFoodScreen
import com.calorieai.app.ui.screens.add.AddMethodSelectorScreen
import com.calorieai.app.ui.screens.add.ManualAddScreen
import com.calorieai.app.ui.screens.ai.AIChatScreen
import com.calorieai.app.ui.screens.camera.CameraScreen
import com.calorieai.app.ui.screens.camera.PhotoAnalysisScreen
import com.calorieai.app.ui.screens.home.HomeScreen
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

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddMethodSelector : Screen("add_method_selector")
    object ManualAdd : Screen("manual_add")
    object AddFood : Screen("add_food")
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
                }
            )
        }

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
                    // 拍照后跳转到分析页面
                    navController.navigate(Screen.PhotoAnalysis.createRoute(uri.toString()))
                }
            )
        }

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

        composable(Screen.AppearanceSettings.route) {
            AppearanceSettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.InteractionSettings.route) {
            InteractionSettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.NotificationSettings.route) {
            NotificationSettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.BackupSettings.route) {
            BackupSettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

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

        composable(Screen.AIConfigDetail.route) { backStackEntry ->
            val configId = backStackEntry.arguments?.getString("configId")
            AIConfigDetailScreen(
                configId = configId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.AIChat.route) {
            AIChatScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.WeightRecord.route) {
            WeightRecordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ExerciseRecord.route) {
            ExerciseRecordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

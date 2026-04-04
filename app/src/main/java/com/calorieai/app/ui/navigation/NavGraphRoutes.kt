package com.calorieai.app.ui.navigation

import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.composable
import com.calorieai.app.ui.screens.add.AddFoodScreen
import com.calorieai.app.ui.screens.add.AddMethodSelectorScreen
import com.calorieai.app.ui.screens.add.FavoriteRecipesManagerScreen
import com.calorieai.app.ui.screens.add.FavoriteRecipesScreen
import com.calorieai.app.ui.screens.add.ManualAddScreen
import com.calorieai.app.ui.screens.add.MealPlanManagerScreen
import com.calorieai.app.ui.screens.add.PantryIngredientsManagerScreen
import com.calorieai.app.ui.screens.ai.AIChatScreen
import com.calorieai.app.ui.screens.camera.CameraScreen
import com.calorieai.app.ui.screens.camera.PhotoAnalysisScreen
import com.calorieai.app.ui.screens.exercise.ExerciseRecordScreen
import com.calorieai.app.ui.screens.functions.FunctionsScreen
import com.calorieai.app.ui.screens.home.HomeScreen
import com.calorieai.app.ui.screens.overview.OverviewScreen
import com.calorieai.app.ui.screens.profile.BodyProfileScreen
import com.calorieai.app.ui.screens.profile.HealthGoalsScreen
import com.calorieai.app.ui.screens.profile.MyScreen
import com.calorieai.app.ui.screens.profile.WaterHistoryScreen
import com.calorieai.app.ui.screens.profile.WeightHistoryScreen
import com.calorieai.app.ui.screens.result.ResultScreen
import com.calorieai.app.ui.screens.settings.AboutScreen
import com.calorieai.app.ui.screens.settings.AIConfigDetailScreen
import com.calorieai.app.ui.screens.settings.AIModelCallStatsScreen
import com.calorieai.app.ui.screens.settings.AISettingsScreen
import com.calorieai.app.ui.screens.settings.AppearanceSettingsScreen
import com.calorieai.app.ui.screens.settings.BackupSettingsScreen
import com.calorieai.app.ui.screens.settings.InteractionSettingsScreen
import com.calorieai.app.ui.screens.settings.NotificationSettingsScreen
import com.calorieai.app.ui.screens.settings.ProfileScreen
import com.calorieai.app.ui.screens.settings.SettingsScreen
import com.calorieai.app.ui.screens.stats.StatsScreen
import com.calorieai.app.ui.screens.water.WaterTrackerScreen
import com.calorieai.app.ui.screens.weight.WeightRecordScreen

internal fun androidx.navigation.NavGraphBuilder.registerAppRoutes(
    navController: NavHostController,
    bottomNavScreens: List<String>
) {
    composable(Screen.Home.route) {
        HomeScreen(
            onNavigateToAdd = {
                navController.navigate(Screen.AddMethodSelector.createRoute(it))
            },
            onNavigateToAIAdd = {
                navController.navigate(Screen.AddFood.createRoute(it))
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
            onNavigateToAIChat = { sessionId ->
                navController.navigate(Screen.AIChat.createRoute(sessionId))
            }
        )
    }

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

    composable(Screen.Functions.route) {
        FunctionsScreen(
            onNavigateToFoodDiary = {
                navController.navigate(Screen.AddMethodSelector.createRoute())
            },
            onNavigateToWeightRecord = {
                navController.navigate(Screen.WeightRecord.createRoute())
            },
            onNavigateToWaterTracker = {
                navController.navigate(Screen.WaterTracker.route)
            },
            onNavigateToExercise = {
                navController.navigate(Screen.ExerciseRecord.createRoute())
            },
            onNavigateToStatistics = {
                navController.navigate(Screen.Stats.route)
            },
            onNavigateToAIAssistant = {
                navController.navigate(Screen.AIChat.createRoute())
            },
            onNavigateToRecipes = {
                navController.navigate(Screen.FavoriteRecipes.route)
            },
            onNavigateToSettings = {
                navController.navigate(Screen.Settings.route)
            }
        )
    }

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

    composable(Screen.WeightHistory.route) {
        WeightHistoryScreen(onNavigateBack = { navController.popBackStack() })
    }

    composable(Screen.HealthGoals.route) {
        HealthGoalsScreen(onNavigateBack = { navController.popBackStack() })
    }

    composable(Screen.BodyProfile.route) {
        BodyProfileScreen(
            onEditClick = {
                navController.navigate(Screen.Profile.route)
            },
            onNavigateToWeightHistory = {
                navController.navigate(Screen.WeightRecord.createRoute())
            },
            onNavigateToGoals = {
                navController.navigate(Screen.HealthGoals.route)
            }
        )
    }

    composable(
        route = Screen.AddMethodSelector.route,
        arguments = listOf(
            navArgument("date") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val date = backStackEntry.arguments?.getString("date")
        AddMethodSelectorScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToManual = {
                navController.navigate(Screen.ManualAdd.route)
            },
            onNavigateToAI = {
                navController.navigate(Screen.AddFood.createRoute(date))
            },
            onNavigateToFavoriteRecipes = {
                navController.navigate(Screen.FavoriteRecipes.route)
            },
            onNavigateToWeight = {
                navController.navigate(Screen.WeightRecord.createRoute(date))
            },
            onNavigateToExercise = {
                navController.navigate(Screen.ExerciseRecord.createRoute(date))
            },
            onNavigateToWaterHistory = {
                navController.navigate(Screen.WaterHistory.createRoute(date))
            }
        )
    }

    composable(Screen.ManualAdd.route) {
        ManualAddScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(Screen.FavoriteRecipes.route) {
        val previousRoute = navController.previousBackStackEntry?.destination?.route
        val showBackButton = previousRoute != null && previousRoute !in bottomNavScreens
        FavoriteRecipesScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToPantryManager = {
                navController.navigate(Screen.PantryIngredientsManager.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToFavoritesManager = {
                navController.navigate(Screen.FavoriteRecipesManager.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToMealPlanManager = {
                navController.navigate(Screen.RecipePlanManager.route) {
                    launchSingleTop = true
                }
            },
            showBackButton = showBackButton
        )
    }

    composable(Screen.FavoriteRecipesManager.route) {
        FavoriteRecipesManagerScreen(onNavigateBack = { navController.popBackStack() })
    }

    composable(Screen.PantryIngredientsManager.route) {
        PantryIngredientsManagerScreen(onNavigateBack = { navController.popBackStack() })
    }

    composable(Screen.RecipePlanManager.route) {
        MealPlanManagerScreen(onNavigateBack = { navController.popBackStack() })
    }

    composable(
        route = Screen.AddFood.route,
        arguments = listOf(
            navArgument("date") {
                type = NavType.StringType
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

    composable(Screen.PhotoAnalysis.route) { backStackEntry ->
        val encodedPhotoUri = backStackEntry.arguments?.getString("photoUri") ?: ""
        val photoUriString = android.net.Uri.decode(encodedPhotoUri)
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
        StatsScreen(onNavigateBack = { navController.popBackStack() })
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
        AppearanceSettingsScreen(onNavigateBack = { navController.popBackStack() })
    }

    composable(Screen.InteractionSettings.route) {
        InteractionSettingsScreen(onNavigateBack = { navController.popBackStack() })
    }

    composable(Screen.NotificationSettings.route) {
        NotificationSettingsScreen(onNavigateBack = { navController.popBackStack() })
    }

    composable(Screen.BackupSettings.route) {
        BackupSettingsScreen(onNavigateBack = { navController.popBackStack() })
    }

    composable(Screen.AISettings.route) {
        AISettingsScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToDetail = { configId ->
                navController.navigate(Screen.AIConfigDetail.createRoute(configId))
            },
            onNavigateToCallStats = {
                navController.navigate(Screen.AIModelCallStats.route)
            }
        )
    }

    composable(Screen.AIModelCallStats.route) {
        AIModelCallStatsScreen(onNavigateBack = { navController.popBackStack() })
    }

    composable(
        route = Screen.AIConfigDetail.route,
        arguments = listOf(
            navArgument("configId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val configId = backStackEntry.arguments?.getString("configId")
        AIConfigDetailScreen(
            configId = configId,
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    composable(Screen.About.route) {
        AboutScreen(onNavigateBack = { navController.popBackStack() })
    }

    composable(
        route = Screen.AIChat.route,
        arguments = listOf(
            navArgument("sessionId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val sessionId = backStackEntry.arguments?.getString("sessionId")
        AIChatScreen(
            initialSessionId = sessionId,
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    composable(Screen.Profile.route) {
        ProfileScreen(onNavigateBack = { navController.popBackStack() })
    }

    composable(
        route = Screen.WeightRecord.route,
        arguments = listOf(
            navArgument("date") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val date = backStackEntry.arguments?.getString("date")
        WeightRecordScreen(
            selectedDate = date,
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    composable(
        route = Screen.ExerciseRecord.route,
        arguments = listOf(
            navArgument("date") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val date = backStackEntry.arguments?.getString("date")
        ExerciseRecordScreen(
            selectedDate = date,
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    composable(Screen.WaterTracker.route) {
        WaterTrackerScreen(onNavigateBack = { navController.popBackStack() })
    }

    composable(
        route = Screen.WaterHistory.route,
        arguments = listOf(
            navArgument("date") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val date = backStackEntry.arguments?.getString("date")
        WaterHistoryScreen(
            selectedDate = date,
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }
}

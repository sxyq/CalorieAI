package com.calorieai.app.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.calorieai.app.ui.components.BottomNavBar
import com.calorieai.app.ui.screens.home.HomeScreen
import com.calorieai.app.ui.screens.overview.OverviewScreen
import com.calorieai.app.ui.screens.profile.MyScreen
import com.calorieai.app.ui.screens.add.FavoriteRecipesScreen
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainTabsScreen(navController: NavHostController) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bottomNavBehaviorViewModel: BottomNavBehaviorViewModel = hiltViewModel()
    val bottomNavBehavior by bottomNavBehaviorViewModel.uiState.collectAsState()
    
    val mainHazeState = remember { HazeState() }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .haze(mainHazeState)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true,
                beyondBoundsPageCount = 1
            ) { page ->
            when (page) {
                0 -> HomeScreen(
                    onNavigateToAdd = { navController.navigate(Screen.AddMethodSelector.createRoute(it)) },
                    onNavigateToAIAdd = { navController.navigate(Screen.AddFood.createRoute(it)) },
                    onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                    onNavigateToResult = { navController.navigate(Screen.Result.createRoute(it)) },
                    onNavigateToAIChat = { navController.navigate(Screen.AIChat.createRoute(it)) }
                )
                1 -> FavoriteRecipesScreen(
                    onNavigateBack = { },
                    onNavigateToPantryManager = { navController.navigate(Screen.PantryIngredientsManager.route) { launchSingleTop = true } },
                    onNavigateToFavoritesManager = { navController.navigate(Screen.FavoriteRecipesManager.route) { launchSingleTop = true } },
                    onNavigateToMealPlanManager = { navController.navigate(Screen.RecipePlanManager.route) { launchSingleTop = true } },
                    showBackButton = false
                )
                2 -> OverviewScreen(
                    onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                    onNavigateToWeightHistory = { navController.navigate(Screen.WeightHistory.route) },
                    onNavigateToGoals = { navController.navigate(Screen.HealthGoals.route) }
                )
                3 -> MyScreen(
                    onNavigateToBodyProfile = { navController.navigate(Screen.BodyProfile.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
        }
        }

        BottomNavBar(
            items = bottomNavItems,
            pagerState = pagerState,
            onItemSelected = { index ->
                coroutineScope.launch {
                    pagerState.animateScrollToPage(index)
                }
            },
            onItemLongPressed = { index ->
                when (index) {
                    0 -> if (bottomNavBehavior.enableLongPressHomeToAdd) {
                        navController.navigate(Screen.AddMethodSelector.createRoute()) { launchSingleTop = true }
                    }
                    2 -> if (bottomNavBehavior.enableLongPressOverviewToStats) {
                        navController.navigate(Screen.Stats.route) { launchSingleTop = true }
                    }
                    3 -> if (bottomNavBehavior.enableLongPressMyToProfileEdit) {
                        navController.navigate(Screen.Profile.route) { launchSingleTop = true }
                    }
                }
            },
            isDark = isDark,
            hazeState = mainHazeState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

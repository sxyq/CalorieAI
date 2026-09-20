package com.calorieai.app.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainTabsScreen(navController: NavHostController) {
    val uiProfileViewModel: UiProfileViewModel = hiltViewModel()
    val uiProfile by uiProfileViewModel.uiState.collectAsStateWithLifecycle()
    val visibleTabs = uiProfile.visibleTabs
    val pagerState = rememberPagerState(pageCount = { visibleTabs.size })
    val coroutineScope = rememberCoroutineScope()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bottomNavBehaviorViewModel: BottomNavBehaviorViewModel = hiltViewModel()
    val bottomNavBehavior by bottomNavBehaviorViewModel.uiState.collectAsState()
    
    val mainHazeState = remember { HazeState() }
    val bottomNavContentPadding = 72.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LaunchedEffect(uiProfile.isSimplified) {
        if (uiProfile.isSimplified && pagerState.currentPage != 0) {
            pagerState.scrollToPage(0)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            modifier = Modifier
                .fillMaxSize()
                .haze(mainHazeState),
            state = pagerState,
            userScrollEnabled = true,
            beyondBoundsPageCount = 0
        ) { page ->
            when (visibleTabs.getOrNull(page)) {
                MainTab.HOME -> HomeScreen(
                    bottomContentPadding = bottomNavContentPadding,
                    uiProfile = uiProfile,
                    onNavigateToAdd = { navController.navigate(Screen.AddMethodSelector.createRoute(it)) },
                    onNavigateToAIAdd = { navController.navigate(Screen.AddFood.createRoute(it)) },
                    onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                    onNavigateToResult = { navController.navigate(Screen.Result.createRoute(it)) }
                )
                MainTab.RECIPES -> FavoriteRecipesScreen(
                    bottomContentPadding = bottomNavContentPadding,
                    onNavigateBack = { },
                    onNavigateToFavoritesManager = { navController.navigate(Screen.FavoriteRecipesManager.route) { launchSingleTop = true } },
                    onNavigateToMealPlanManager = { navController.navigate(Screen.RecipePlanManager.route) { launchSingleTop = true } },
                    showBackButton = false
                )
                MainTab.OVERVIEW -> OverviewScreen(
                    bottomContentPadding = bottomNavContentPadding,
                    onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                    onNavigateToWeightHistory = { navController.navigate(Screen.WeightHistory.route) },
                    onNavigateToGoals = { navController.navigate(Screen.HealthGoals.route) }
                )
                MainTab.MY -> MyScreen(
                    bottomContentPadding = bottomNavContentPadding,
                    onNavigateToBodyProfile = { navController.navigate(Screen.BodyProfile.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
                null -> Unit
            }
        }

        BottomNavBar(
            items = visibleTabs.map { it.toNavItem() },
            pagerState = pagerState,
            onItemSelected = { index ->
                coroutineScope.launch {
                    pagerState.animateScrollToPage(index)
                }
            },
            onItemLongPressed = { index ->
                when (visibleTabs.getOrNull(index)) {
                    MainTab.HOME -> if (bottomNavBehavior.enableLongPressHomeToAdd) {
                        navController.navigate(Screen.AddMethodSelector.createRoute()) { launchSingleTop = true }
                    }
                    MainTab.OVERVIEW -> if (bottomNavBehavior.enableLongPressOverviewToStats) {
                        navController.navigate(Screen.Stats.route) { launchSingleTop = true }
                    }
                    MainTab.MY -> if (bottomNavBehavior.enableLongPressMyToProfileEdit) {
                        navController.navigate(Screen.Profile.route) { launchSingleTop = true }
                    }
                    MainTab.RECIPES -> Unit
                    null -> Unit
                }
            },
            isDark = isDark,
            hazeState = mainHazeState,
            isSimplifiedMode = uiProfile.isSimplified,
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
        )
    }
}

private fun MainTab.toNavItem() = when (this) {
    MainTab.HOME -> bottomNavItems[0]
    MainTab.RECIPES -> bottomNavItems[1]
    MainTab.OVERVIEW -> bottomNavItems[2]
    MainTab.MY -> bottomNavItems[3]
}

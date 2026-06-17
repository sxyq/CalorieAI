package com.calorieai.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import dev.chrisbanes.haze.hazeChild
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calorieai.app.ui.feedback.AppHapticController
import com.calorieai.app.ui.feedback.rememberAppHapticController
import com.calorieai.app.ui.theme.AppColors
import com.calorieai.app.ui.theme.GlassDarkColors
import com.calorieai.app.ui.theme.GlassLightColors
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs

private const val BOTTOM_NAV_LONG_PRESS_MS = 220L

data class NavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasBadge: Boolean = false
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomNavBar(
    items: List<NavItem>,
    pagerState: PagerState,
    onItemSelected: (Int) -> Unit,
    onItemLongPressed: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
    hazeState: dev.chrisbanes.haze.HazeState? = null
) {
    val haptics = rememberAppHapticController()
    val navigationBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val containerHeight = remember(navigationBarInset) { 72.dp + navigationBarInset }
    
    val backgroundColor = if (isDark) Color(0xFF1E1E22).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.90f)
    val hazeTint = if (isDark) Color(0xFF1E1E22).copy(alpha = 0.35f) else Color.White.copy(alpha = 0.40f)
    val borderColor = if (isDark) Color(0x33FFFFFF) else Color(0xFFE5E7EB)

    val density = LocalDensity.current
    val highlightHeight = with(density) { 1.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(containerHeight)
            .then(
                if (hazeState != null) {
                    Modifier.hazeChild(
                        state = hazeState,
                        style = dev.chrisbanes.haze.HazeStyle(
                            tint = hazeTint,
                            blurRadius = 24.dp
                        )
                    )
                } else {
                    Modifier.background(backgroundColor)
                }
            )
            .drawBehind {
                // Top border line
                drawRect(
                    color = borderColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, highlightHeight)
                )
            }
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .align(Alignment.TopCenter)
        ) {
            val totalWidthPx = constraints.maxWidth.toFloat()
            val itemCount = items.size
            val itemWidthPx = totalWidthPx / itemCount

            // Moving Indicator Layer
            // It calculates translationX purely based on the Pager's current page + offset.
            val indicatorWidthPx = with(density) { 64.dp.toPx() }
            val centerOffsetPx = (itemWidthPx - indicatorWidthPx) / 2f
            val position = pagerState.currentPage + pagerState.currentPageOffsetFraction
            
            // Deformation effect based on velocity/distance
            val velocity = abs(pagerState.currentPageOffsetFraction)
            val scaleX = 1f + (velocity * 0.4f)
            
            val indicatorColor = if (isDark) GlassDarkColors.IndicatorBackground else GlassLightColors.IndicatorBackground

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .graphicsLayer {
                        translationX = (position * itemWidthPx) + centerOffsetPx
                        this.scaleX = scaleX
                    }
            ) {
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .height(32.dp)
                        .align(Alignment.CenterStart) // aligned correctly with translationX
                        .offset(y = (-4).dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
            }

            // Icons and Texts Layer
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                items.forEachIndexed { index, item ->
                    // Calculate visual selection state for colors and scale
                    val distance = abs(position - index)
                    val isSelectedFraction = (1f - distance).coerceIn(0f, 1f)
                    val isSelected = index == pagerState.currentPage

                    NavBarItemContent(
                        item = item,
                        isSelected = isSelected,
                        selectionFraction = isSelectedFraction,
                        onClick = { onItemSelected(index) },
                        onLongClick = onItemLongPressed?.let { handler -> { handler(index) } },
                        haptics = haptics,
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NavBarItemContent(
    item: NavItem,
    isSelected: Boolean,
    selectionFraction: Float,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    haptics: AppHapticController,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val selectedIconColor = if (isDark) GlassDarkColors.SelectedIcon else GlassLightColors.SelectedIcon
    val unselectedIconColor = if (isDark) GlassDarkColors.UnselectedIcon else GlassLightColors.UnselectedIcon
    val selectedTextColor = if (isDark) GlassDarkColors.SelectedText else GlassLightColors.SelectedText
    val unselectedTextColor = if (isDark) GlassDarkColors.UnselectedText else GlassLightColors.UnselectedText

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "iconScale"
    )

    // Blend color based on selectionFraction driven by swipe
    val iconColor = androidx.compose.ui.graphics.lerp(unselectedIconColor, selectedIconColor, selectionFraction)
    val textColor = androidx.compose.ui.graphics.lerp(unselectedTextColor, selectedTextColor, selectionFraction)

    val ripple = rememberRipple(bounded = false, radius = 32.dp, color = Color.Black.copy(alpha = 0.08f))

    Box(
        modifier = modifier
            .fillMaxHeight()
            .then(
                if (onLongClick != null) {
                    Modifier
                        .indication(interactionSource = interactionSource, indication = ripple)
                        .pointerInput(onLongClick, onClick) {
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)
                                val releasedInTime = withTimeoutOrNull(BOTTOM_NAV_LONG_PRESS_MS) {
                                    waitForUpOrCancellation() != null
                                }

                                when (releasedInTime) {
                                    true -> {
                                        haptics.click()
                                        onClick()
                                    }
                                    null -> {
                                        haptics.longPress()
                                        onLongClick()
                                        waitForUpOrCancellation()
                                    }
                                    else -> Unit
                                }
                            }
                        }
                } else {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple,
                        onClick = {
                            haptics.click()
                            onClick()
                        }
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(28.dp).offset(y = (-2).dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.title,
                    modifier = Modifier.scale(iconScale),
                    tint = iconColor
                )
                if (item.hasBadge) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                    )
                }
            }

            Text(
                text = item.title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}

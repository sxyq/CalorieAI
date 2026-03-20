package com.calorieai.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calorieai.app.ui.animation.AnimationEasing
import com.calorieai.app.ui.animation.AnimationSpecs
import com.calorieai.app.ui.theme.*
import kotlinx.coroutines.withTimeoutOrNull

private const val BOTTOM_NAV_LONG_PRESS_MS = 220L

/**
 * 底部导航栏数据类
 */
data class NavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasBadge: Boolean = false
)

/**
 * 底部导航栏组件 - Glass 毛玻璃风格
 * - 高度 80dp，宽度 100%
 * - 圆角 0dp（直角）
 * - 水平等分布局
 */
@Composable
fun BottomNavBar(
    items: List<NavItem>,
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    onItemLongPressed: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    isDark: Boolean = false
) {
    val context = LocalContext.current
    val isLowEnd = remember { GlassDeviceUtils.isLowEndDevice(context) }
    val supportsBlur = GlassDeviceUtils.supportsBlur()

    // 背景颜色 #F3EDF7（浅色）/#211F26（深色），透明度 95%
    val backgroundColor = AppColors.navigationBarBackground(isDark).copy(alpha = GlassAlpha.NAVIGATION_BAR)

    // 边框颜色
    val borderColor = Color.White.copy(alpha = if (isDark) 0.1f else 0.1f)

    val density = LocalDensity.current
    val highlightHeight = with(density) { 1.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .drawBehind {
                // 背景
                drawRect(backgroundColor)

                // 顶部高光 1px 渐变（rgba(255,255,255,0.2) → transparent）
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = highlightHeight
                    ),
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, highlightHeight)
                )

                // 底部内阴影 1px rgba(0,0,0,0.1)
                drawRect(
                    color = Color.Black.copy(alpha = 0.1f),
                    topLeft = Offset(0f, size.height - highlightHeight),
                    size = Size(size.width, highlightHeight)
                )
            }
            .then(
                if (supportsBlur && !isLowEnd) {
                    Modifier.glassBlur(GlassUtils.BlurRadius.MEDIUM)
                } else {
                    Modifier
                }
            )
            .border(width = 1.dp, color = borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                NavBarItem(
                    item = item,
                    isSelected = item.route == selectedRoute,
                    onClick = { onItemSelected(item.route) },
                    onLongClick = onItemLongPressed?.let { handler ->
                        { handler(item.route) }
                    },
                    isDark = isDark
                )
            }
        }
    }
}

/**
 * 导航项组件 - 胶囊指示器 + 动效
 */
@Composable
private fun NavBarItem(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    isDark: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }

    val context = LocalContext.current
    val isLowEnd = remember { GlassDeviceUtils.isLowEndDevice(context) }
    val supportsBlur = GlassDeviceUtils.supportsBlur()

    val colors = AppColors.getColors(isDark)

    // 图标颜色
    val selectedIconColor = if (isDark) GlassDarkColors.SelectedIcon else GlassLightColors.SelectedIcon
    val unselectedIconColor = if (isDark) GlassDarkColors.UnselectedIcon else GlassLightColors.UnselectedIcon
    val selectedTextColor = if (isDark) GlassDarkColors.SelectedText else GlassLightColors.SelectedText
    val unselectedTextColor = if (isDark) GlassDarkColors.UnselectedText else GlassLightColors.UnselectedText

    // 指示器背景色
    val indicatorColor = if (isDark) GlassDarkColors.IndicatorBackground else GlassLightColors.IndicatorBackground

    val density = LocalDensity.current
    val shadowHeight = with(density) { 1.dp.toPx() }

    // 指示器动画 - 缩放 0.8→1 + 淡入，时长 300ms
    val indicatorScale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.8f,
        animationSpec = AnimationSpecs.Normal,
        label = "indicatorScale"
    )

    val indicatorAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = AnimationSpecs.Normal,
        label = "indicatorAlpha"
    )

    // 指示器位置动画 - 时长 250ms，弹跳缓动
    val indicatorOffset by animateFloatAsState(
        targetValue = if (isSelected) -2f else 0f, // 选中时上浮 2dp
        animationSpec = tween(250, easing = AnimationEasing.EaseOutBack),
        label = "indicatorOffset"
    )

    // 图标微动效 - 选中时放大 1.1 倍，弹性回弹
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = AnimationSpecs.SpringBouncy,
        label = "iconScale"
    )

    // 颜色动画 - 时长 200ms
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) selectedIconColor else unselectedIconColor,
        animationSpec = tween(durationMillis = 200),
        label = "iconColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) selectedTextColor else unselectedTextColor,
        animationSpec = tween(durationMillis = 200),
        label = "textColor"
    )

    val ripple = rememberRipple(
        bounded = true,
        radius = 24.dp,
        color = Color.Black.copy(alpha = 0.12f)
    )

    Box(
        modifier = Modifier
            .widthIn(min = 48.dp)
            .heightIn(min = 48.dp)
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
                                    true -> onClick()
                                    null -> {
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
                        onClick = onClick
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 12.dp)
                .padding(bottom = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 图标容器
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                // 胶囊指示器 - 尺寸 64dp × 32dp，圆角 50%
                if (indicatorAlpha > 0.01f) {
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(32.dp)
                            .offset(y = indicatorOffset.dp)
                            .scale(indicatorScale)
                            .alpha(indicatorAlpha)
                            .clip(CircleShape)
                            .drawBehind {
                                // 指示器背景
                                drawRect(color = indicatorColor)

                                // 底部内阴影 1px rgba(0,0,0,0.1)
                                drawRect(
                                    color = Color.Black.copy(alpha = 0.1f),
                                    topLeft = Offset(0f, size.height - shadowHeight),
                                    size = Size(size.width, shadowHeight)
                                )
                            }
                            .then(
                                if (supportsBlur && !isLowEnd) {
                                    Modifier.glassBlur(GlassUtils.BlurRadius.SMALL)
                                } else {
                                    Modifier
                                }
                            )
                    )
                }

                // 图标 - 24dp × 24dp
                Icon(
                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.title,
                    modifier = Modifier.scale(iconScale),
                    tint = iconColor
                )

                // 徽章
                if (item.hasBadge) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                    )
                }
            }

            // 标签 - 12sp，Medium 字重
            Text(
                text = item.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                lineHeight = 16.sp
            )
        }
    }
}



/**
 * 标准底部导航项配置
 */
object StandardNavItems {
    val Overview = NavItem(
        route = "overview",
        title = "概览",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    )

    val Functions = NavItem(
        route = "functions",
        title = "功能",
        selectedIcon = Icons.Filled.Apps,
        unselectedIcon = Icons.Outlined.Apps
    )

    val Add = NavItem(
        route = "add",
        title = "记录",
        selectedIcon = Icons.Filled.AddCircle,
        unselectedIcon = Icons.Outlined.AddCircleOutline
    )

    val Profile = NavItem(
        route = "profile",
        title = "我的",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    val BodyProfile = NavItem(
        route = "body_profile",
        title = "身体",
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder
    )

    val defaultItems = listOf(Overview, Functions, Add, BodyProfile, Profile)
}

/**
 * 底部导航栏 - 标准配置
 */
@Composable
fun StandardBottomNavBar(
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = false
) {
    BottomNavBar(
        items = StandardNavItems.defaultItems,
        selectedRoute = selectedRoute,
        onItemSelected = onItemSelected,
        modifier = modifier,
        isDark = isDark
    )
}

/**
 * 带FAB的底部导航栏 - Glass 风格
 */
@Composable
fun BottomNavBarWithFab(
    items: List<NavItem>,
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    onFabClick: () -> Unit,
    modifier: Modifier = Modifier,
    fabIcon: ImageVector = Icons.Default.Add,
    isDark: Boolean = false
) {
    val context = LocalContext.current
    val isLowEnd = remember { GlassDeviceUtils.isLowEndDevice(context) }
    val supportsBlur = GlassDeviceUtils.supportsBlur()

    val backgroundColor = AppColors.navigationBarBackground(isDark).copy(alpha = GlassAlpha.NAVIGATION_BAR)
    val borderColor = Color.White.copy(alpha = if (isDark) 0.1f else 0.1f)

    val density = LocalDensity.current
    val highlightHeight = with(density) { 1.dp.toPx() }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        // 底部导航栏（为FAB留出空间）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .height(72.dp)
                .drawBehind {
                    drawRect(backgroundColor)
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = highlightHeight
                        ),
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, highlightHeight)
                    )
                    drawRect(
                        color = Color.Black.copy(alpha = 0.1f),
                        topLeft = Offset(0f, size.height - highlightHeight),
                        size = Size(size.width, highlightHeight)
                    )
                }
                .then(
                    if (supportsBlur && !isLowEnd) {
                        Modifier.glassBlur(GlassUtils.BlurRadius.MEDIUM)
                    } else {
                        Modifier
                    }
                )
                .border(width = 1.dp, color = borderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.take(2).forEach { item ->
                    NavBarItem(
                        item = item,
                        isSelected = item.route == selectedRoute,
                        onClick = { onItemSelected(item.route) },
                        isDark = isDark
                    )
                }

                // FAB占位
                Spacer(modifier = Modifier.width(64.dp))

                items.takeLast(2).forEach { item ->
                    NavBarItem(
                        item = item,
                        isSelected = item.route == selectedRoute,
                        onClick = { onItemSelected(item.route) },
                        isDark = isDark
                    )
                }
            }
        }

        // 中央FAB - Glass 风格
        val fabBackgroundColor = AppColors.primary(isDark)
        val fabContentColor = AppColors.onPrimary(isDark)

        FloatingActionButton(
            onClick = onFabClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(64.dp),
            shape = CircleShape,
            containerColor = fabBackgroundColor,
            contentColor = fabContentColor,
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(
                imageVector = fabIcon,
                contentDescription = "添加",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

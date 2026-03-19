package com.calorieai.app.ui.screens.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import com.calorieai.app.ui.components.liquidGlass
import com.calorieai.app.ui.components.interactiveScale
import com.calorieai.app.ui.theme.GlassLightColors
import com.calorieai.app.ui.theme.GlassDarkColors
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun AddMethodSelectorScreen(
    onNavigateBack: () -> Unit,
    onNavigateToManual: () -> Unit,
    onNavigateToAI: () -> Unit,
    onNavigateToFavoriteRecipes: () -> Unit = {},
    onNavigateToWeight: () -> Unit = {},
    onNavigateToExercise: () -> Unit = {},
    onNavigateToWaterHistory: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    var visible by remember { mutableStateOf(false) }
    val primaryColor = if (isDark) GlassDarkColors.Primary else GlassLightColors.Primary
    val surfaceColor = if (isDark) GlassDarkColors.Surface else GlassLightColors.Surface
    val onSurfaceColor = if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    BackHandler(enabled = visible) {
        visible = false
        onNavigateBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor.copy(alpha = 0.98f))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    visible = false
                    onNavigateBack()
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(300)) + slideInVertically(
                    animationSpec = tween(400, easing = EaseOutCubic),
                    initialOffsetY = { it / 2 }
                ),
                exit = fadeOut(tween(200)) + slideOutVertically(
                    animationSpec = tween(200),
                    targetOffsetY = { it / 2 }
                )
            ) {
                Text(
                    text = "添加记录",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(350, delayMillis = 100)),
                exit = fadeOut(tween(150))
            ) {
                Text(
                    text = "选择记录方式",
                    style = MaterialTheme.typography.bodyLarge,
                    color = onSurfaceColor.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 150)) + slideInVertically(
                    animationSpec = tween(400, delayMillis = 150, easing = EaseOutCubic),
                    initialOffsetY = { it }
                ),
                exit = fadeOut(tween(150))
            ) {
                PrimaryMethodCard(
                    icon = Icons.Default.AutoAwesome,
                    title = "AI 智能识别",
                    subtitle = "拍照或描述，AI自动分析热量",
                    backgroundColor = primaryColor,
                    iconBackgroundColor = Color.White.copy(alpha = 0.25f),
                    onClick = onNavigateToAI
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 200)) + slideInVertically(
                    animationSpec = tween(400, delayMillis = 200, easing = EaseOutCubic),
                    initialOffsetY = { it }
                ),
                exit = fadeOut(tween(150))
            ) {
                PrimaryMethodCard(
                    icon = Icons.Default.Edit,
                    title = "手动录入",
                    subtitle = "手动输入食物名称和营养信息",
                    backgroundColor = MaterialTheme.colorScheme.secondary,
                    iconBackgroundColor = Color.White.copy(alpha = 0.25f),
                    onClick = onNavigateToManual
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 250)),
                exit = fadeOut(tween(150))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SecondaryMethodCard(
                            icon = Icons.Default.MenuBook,
                            title = "收藏菜谱",
                            subtitle = "快速添加",
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToFavoriteRecipes
                        )
                        SecondaryMethodCard(
                            icon = Icons.Default.Scale,
                            title = "体重",
                            subtitle = "记录体重",
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToWeight
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SecondaryMethodCard(
                            icon = Icons.Default.FitnessCenter,
                            title = "运动",
                            subtitle = "添加运动",
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToExercise
                        )
                        SecondaryMethodCard(
                            icon = Icons.Default.WaterDrop,
                            title = "饮水",
                            subtitle = "记录饮水",
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToWaterHistory
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(300, delayMillis = 300)),
                exit = fadeOut(tween(150))
            ) {
                FilledTonalButton(
                    onClick = {
                        visible = false
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = surfaceColor.copy(alpha = 0.8f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "取消",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PrimaryMethodCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    backgroundColor: Color,
    iconBackgroundColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SecondaryMethodCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val surfaceVariant = if (isDark)
        Color(0xFF2A2A2A)
    else
        Color(0xFFF5F5F5)
    val onSurfaceVariant = if (isDark)
        Color(0xFFB0B0B0)
    else
        Color(0xFF666666)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )

    Column(
        modifier = modifier
            .height(110.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceVariant)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = onSurfaceVariant.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

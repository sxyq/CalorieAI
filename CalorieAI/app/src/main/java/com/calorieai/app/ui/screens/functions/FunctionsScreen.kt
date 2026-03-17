package com.calorieai.app.ui.screens.functions

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calorieai.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FunctionsScreen(
    onNavigateToFoodDiary: () -> Unit = {},
    onNavigateToWeightRecord: () -> Unit = {},
    onNavigateToWaterTracker: () -> Unit = {},
    onNavigateToExercise: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToAIAssistant: () -> Unit = {},
    onNavigateToRecipes: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "功能",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "主要功能",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassFunctionCard(
                    title = "饮食记录",
                    subtitle = "记录每日饮食",
                    icon = Icons.Default.Restaurant,
                    gradientColors = GlassGradientColors.ORANGE,
                    onClick = onNavigateToFoodDiary,
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )

                GlassFunctionCard(
                    title = "体重记录",
                    subtitle = "追踪体重变化",
                    icon = Icons.Default.MonitorWeight,
                    gradientColors = GlassGradientColors.BLUE,
                    onClick = onNavigateToWeightRecord,
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassFunctionCard(
                    title = "饮水记录",
                    subtitle = "保持水分充足",
                    icon = Icons.Default.WaterDrop,
                    gradientColors = GlassGradientColors.CYAN,
                    onClick = onNavigateToWaterTracker,
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )

                GlassFunctionCard(
                    title = "运动记录",
                    subtitle = "记录运动消耗",
                    icon = Icons.Default.DirectionsRun,
                    gradientColors = GlassGradientColors.GREEN,
                    onClick = onNavigateToExercise,
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "数据分析",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            GlassWideFunctionCard(
                title = "统计图表",
                subtitle = "查看详细数据分析和趋势",
                icon = Icons.Default.BarChart,
                gradientColors = GlassGradientColors.PURPLE,
                onClick = onNavigateToStatistics,
                isDark = isDark
            )

            GlassWideFunctionCard(
                title = "AI 健康助手",
                subtitle = "智能分析和个性化建议",
                icon = Icons.Default.SmartToy,
                gradientColors = GlassGradientColors.INDIGO,
                onClick = onNavigateToAIAssistant,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "更多功能",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            GlassCompactFunctionCard(
                title = "健康食谱",
                subtitle = "发现营养美食",
                icon = Icons.Default.MenuBook,
                color = Color(0xFFEC407A),
                onClick = onNavigateToRecipes,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

object GlassGradientColors {
    val ORANGE = listOf(Color(0xFFFFA726), Color(0xFFFF7043))
    val BLUE = listOf(Color(0xFF42A5F5), Color(0xFF5C6BC0))
    val CYAN = listOf(Color(0xFF26C6DA), Color(0xFF00ACC1))
    val GREEN = listOf(Color(0xFF66BB6A), Color(0xFF43A047))
    val PURPLE = listOf(Color(0xFFAB47BC), Color(0xFF8E24AA))
    val INDIGO = listOf(Color(0xFF5C6BC0), Color(0xFF3949AB))
}

@Composable
private fun GlassFunctionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "functionCardScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .aspectRatio(1f)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .glassCardThemed(
                isDark = isDark,
                cornerRadius = 20.dp
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors.map { it.copy(alpha = 0.15f) },
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = Brush.linearGradient(colors = gradientColors),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassWideFunctionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    isDark: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "wideCardScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .glassCardThemed(
                isDark = isDark,
                cornerRadius = 20.dp
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = gradientColors.map { it.copy(alpha = 0.12f) }
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                brush = Brush.linearGradient(colors = gradientColors),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun GlassCompactFunctionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    isDark: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "compactCardScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .glassCardThemed(
                isDark = isDark,
                cornerRadius = 16.dp
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

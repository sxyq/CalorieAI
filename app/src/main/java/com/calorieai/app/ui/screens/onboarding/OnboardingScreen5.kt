package com.calorieai.app.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.calorieai.app.data.model.GoalType
import com.calorieai.app.data.model.WeightLossStrategy
import com.calorieai.app.ui.components.OnboardingNavigationButtons
import com.calorieai.app.ui.theme.*
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen5(
    goalType: String,
    currentWeight: Float,
    initialTargetWeight: Float? = null,
    initialStrategy: String? = null,
    onNext: (targetWeight: Float, strategy: String, estimatedWeeks: Int) -> Unit,
    onBack: () -> Unit
) {
    val goal = GoalType.fromString(goalType) ?: GoalType.MAINTAIN

    var targetWeightText by remember { mutableStateOf(initialTargetWeight?.toString() ?: "") }
    val targetWeight = targetWeightText.toFloatOrNull()

    var selectedStrategy by remember {
        mutableStateOf<WeightLossStrategy?>(
            initialStrategy?.let { WeightLossStrategy.fromString(it) }
        )
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val estimatedWeeks = remember(targetWeight, selectedStrategy, goal) {
        calculateEstimatedWeeks(currentWeight, targetWeight, selectedStrategy, goal)
    }

    val isFormValid = targetWeight != null && targetWeight > 0 && selectedStrategy != null &&
            isValidTargetWeight(currentWeight, targetWeight, goal)

    val weightRangeHint = when (goal) {
        GoalType.LOSE_WEIGHT -> "建议：${(currentWeight * 0.85).roundToInt()}-${(currentWeight * 0.95).roundToInt()} kg"
        GoalType.GAIN_WEIGHT -> "建议：${(currentWeight * 1.05).roundToInt()}-${(currentWeight * 1.15).roundToInt()} kg"
        GoalType.GAIN_MUSCLE -> "建议：维持或略增"
        GoalType.MAINTAIN -> "建议：${(currentWeight * 0.98).roundToInt()}-${(currentWeight * 1.02).roundToInt()} kg"
    }

    val strategies = listOf(
        StrategyData(
            strategy = WeightLossStrategy.AGGRESSIVE,
            title = "激进",
            subtitle = "快速见效",
            description = "每周约0.5-1kg变化",
            icon = Icons.Default.Bolt,
            color = Color(0xFFF44336),
            gradientColors = listOf(Color(0xFFEF5350), Color(0xFFF44336)),
            tip = "需要较强意志力",
            difficulty = "高难度"
        ),
        StrategyData(
            strategy = WeightLossStrategy.MODERATE,
            title = "平和",
            subtitle = "稳定健康",
            description = "每周约0.3-0.5kg变化",
            icon = Icons.Default.TrendingDown,
            color = Color(0xFF4CAF50),
            gradientColors = listOf(Color(0xFF66BB6A), Color(0xFF4CAF50)),
            tip = "推荐选择",
            difficulty = "中等难度"
        ),
        StrategyData(
            strategy = WeightLossStrategy.GENTLE,
            title = "温和",
            subtitle = "循序渐进",
            description = "每周约0.1-0.3kg变化",
            icon = Icons.Default.Spa,
            color = Color(0xFF2196F3),
            gradientColors = listOf(Color(0xFF42A5F5), Color(0xFF2196F3)),
            tip = "容易坚持",
            difficulty = "低难度"
        )
    )

    val goalColor = when (goal) {
        GoalType.LOSE_WEIGHT -> Color(0xFF4CAF50)
        GoalType.GAIN_MUSCLE -> Color(0xFF2196F3)
        GoalType.GAIN_WEIGHT -> Color(0xFFFF9800)
        GoalType.MAINTAIN -> Color(0xFF9C27B0)
    }

    OnboardingStepRenderer(
        config = OnboardingStepLayoutConfig(
            appBarTitle = "细化目标",
            headline = "设定具体目标",
            subtitle = "根据您的选择，让我们制定更精确的计划",
            currentStep = 4,
            totalSteps = 4,
            horizontalPadding = 20.dp
        ),
        onBack = onBack
    ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CurrentWeightCard(
                    weight = currentWeight,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )

                GoalTypeCard(
                    goal = goal,
                    color = goalColor,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = targetWeightText,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        targetWeightText = it
                    }
                },
                label = { Text("目标体重 (kg)") },
                placeholder = { Text("请输入目标体重") },
                leadingIcon = {
                    Icon(Icons.Default.Flag, contentDescription = null, tint = goalColor)
                },
                trailingIcon = {
                    if (targetWeight != null) {
                        val diff = targetWeight - currentWeight
                        val diffText = if (diff > 0) "+${diff.roundToInt()}" else "${diff.roundToInt()}"
                        Text(
                            text = "$diffText kg",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (diff > 0) Color(0xFF4CAF50) else Color(0xFFFF9800),
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                supportingText = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = weightRangeHint,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                isError = targetWeight != null && !isValidTargetWeight(currentWeight, targetWeight, goal)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "选择执行策略",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            strategies.forEach { strategyData ->
                StrategyCard(
                    data = strategyData,
                    isSelected = selectedStrategy == strategyData.strategy,
                    onClick = { selectedStrategy = strategyData.strategy },
                    isDark = isDark
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            AnimatedVisibility(
                visible = estimatedWeeks != null && estimatedWeeks!! > 0,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                estimatedWeeks?.let { weeks ->
                    Spacer(modifier = Modifier.height(8.dp))
                    EstimatedTimeCard(
                        weeks = weeks,
                        color = goalColor,
                        isDark = isDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OnboardingNavigationButtons(
                onBack = onBack,
                onNext = {
                    if (isFormValid && targetWeight != null) {
                        val strategy = selectedStrategy
                        if (strategy != null) {
                            onNext(targetWeight, strategy.name, estimatedWeeks ?: 0)
                        }
                    }
                },
                isNextEnabled = isFormValid,
                nextButtonText = "完成设置",
                showCheckIcon = true,
                buttonHeight = 52,
                cornerRadius = 14
            )

            Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CurrentWeightCard(
    weight: Float,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MonitorWeight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "当前体重",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${weight.roundToInt()} kg",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GoalTypeCard(
    goal: GoalType,
    color: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val icon = when (goal) {
        GoalType.LOSE_WEIGHT -> Icons.Default.TrendingDown
        GoalType.GAIN_MUSCLE -> Icons.Default.FitnessCenter
        GoalType.GAIN_WEIGHT -> Icons.Default.TrendingUp
        GoalType.MAINTAIN -> Icons.Default.Balance
    }

    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "目标类型",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = goal.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

private data class StrategyData(
    val strategy: WeightLossStrategy,
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val gradientColors: List<Color>,
    val tip: String,
    val difficulty: String
)

@Composable
private fun StrategyCard(
    data: StrategyData,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDark: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.98f
            isSelected -> 1.02f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "strategyCardScale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 6.dp else 1.dp,
        animationSpec = tween(200),
        label = "strategyCardElevation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(elevation, RoundedCornerShape(14.dp))
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        brush = Brush.linearGradient(data.gradientColors),
                        shape = RoundedCornerShape(14.dp)
                    )
                } else {
                    Modifier
                }
            )
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        brush = Brush.linearGradient(data.gradientColors),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSelected) data.color else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = data.color.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = data.difficulty,
                            style = MaterialTheme.typography.labelSmall,
                            color = data.color,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = data.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AnimatedVisibility(
                    visible = isSelected,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = data.color,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = data.tip,
                            style = MaterialTheme.typography.labelSmall,
                            color = data.color
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isSelected,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(
                            brush = Brush.linearGradient(data.gradientColors),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EstimatedTimeCard(
    weeks: Int,
    color: Color,
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(14.dp)
            )
            .background(
                color = color.copy(alpha = 0.08f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(color.copy(alpha = 0.8f), color)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "预计达成时间",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "约 $weeks ",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = "周",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun calculateEstimatedWeeks(
    currentWeight: Float,
    targetWeight: Float?,
    strategy: WeightLossStrategy?,
    goal: GoalType
): Int? {
    if (targetWeight == null || strategy == null) return null

    val weightDiff = abs(targetWeight - currentWeight)
    if (weightDiff < 0.1f) return 0

    return when (goal) {
        GoalType.LOSE_WEIGHT, GoalType.GAIN_WEIGHT -> {
            (weightDiff / strategy.weeklyChange).roundToInt().coerceAtLeast(1)
        }
        GoalType.GAIN_MUSCLE, GoalType.MAINTAIN -> {
            (weightDiff / (strategy.weeklyChange * 0.5f)).roundToInt().coerceAtLeast(1)
        }
    }
}

private fun isValidTargetWeight(currentWeight: Float, targetWeight: Float, goal: GoalType): Boolean {
    return when (goal) {
        GoalType.LOSE_WEIGHT -> targetWeight < currentWeight && targetWeight >= currentWeight * 0.7f
        GoalType.GAIN_WEIGHT -> targetWeight > currentWeight && targetWeight <= currentWeight * 1.3f
        GoalType.GAIN_MUSCLE -> targetWeight >= currentWeight * 0.95f && targetWeight <= currentWeight * 1.1f
        GoalType.MAINTAIN -> targetWeight >= currentWeight * 0.9f && targetWeight <= currentWeight * 1.1f
    }
}

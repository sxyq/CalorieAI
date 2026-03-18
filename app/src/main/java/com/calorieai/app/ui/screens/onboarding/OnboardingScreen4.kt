package com.calorieai.app.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.calorieai.app.data.model.GoalType
import com.calorieai.app.ui.components.OnboardingNavigationButtons
import com.calorieai.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen4(
    initialGoalType: String? = null,
    onNext: (goalType: String) -> Unit,
    onBack: () -> Unit
) {
    var selectedGoalType by remember { mutableStateOf<GoalType?>(initialGoalType?.let { GoalType.fromString(it) }) }

    val isDark = isSystemInDarkTheme()

    val isFormValid = selectedGoalType != null

    val goalOptions = listOf(
        GoalOptionData(
            type = GoalType.LOSE_WEIGHT,
            icon = Icons.Default.TrendingDown,
            color = Color(0xFF4CAF50),
            gradientColors = listOf(Color(0xFF81C784), Color(0xFF4CAF50)),
            description = "减少体脂，塑造身材",
            tip = "建议每周减重0.5-1kg",
            benefits = listOf("改善体型", "提升健康", "增强自信")
        ),
        GoalOptionData(
            type = GoalType.GAIN_MUSCLE,
            icon = Icons.Default.FitnessCenter,
            color = Color(0xFF2196F3),
            gradientColors = listOf(Color(0xFF64B5F6), Color(0xFF2196F3)),
            description = "增加肌肉量，增强力量",
            tip = "需要配合力量训练",
            benefits = listOf("增强力量", "改善体态", "提高代谢")
        ),
        GoalOptionData(
            type = GoalType.GAIN_WEIGHT,
            icon = Icons.Default.TrendingUp,
            color = Color(0xFFFF9800),
            gradientColors = listOf(Color(0xFFFFB74D), Color(0xFFFF9800)),
            description = "健康增重，改善体质",
            tip = "建议每周增重0.25-0.5kg",
            benefits = listOf("增强体质", "改善营养", "提升活力")
        ),
        GoalOptionData(
            type = GoalType.MAINTAIN,
            icon = Icons.Default.Balance,
            color = Color(0xFF9C27B0),
            gradientColors = listOf(Color(0xFFBA68C8), Color(0xFF9C27B0)),
            description = "维持当前体重和健康状态",
            tip = "保持均衡饮食和运动",
            benefits = listOf("维持健康", "稳定体重", "长期坚持")
        )
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("健康目标") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OnboardingProgressIndicator(currentStep = 3, totalSteps = 4, isDark = isDark)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "您的健康目标",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "选择您想要达成的目标，我们将为您制定个性化方案",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            goalOptions.forEach { option ->
                GoalTypeCard(
                    option = option,
                    isSelected = selectedGoalType == option.type,
                    onClick = { selectedGoalType = option.type },
                    isDark = isDark
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            OnboardingNavigationButtons(
                onBack = onBack,
                onNext = {
                    if (isFormValid) {
                        onNext(selectedGoalType!!.name)
                    }
                },
                isNextEnabled = isFormValid,
                buttonHeight = 52,
                cornerRadius = 14
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private data class GoalOptionData(
    val type: GoalType,
    val icon: ImageVector,
    val color: Color,
    val gradientColors: List<Color>,
    val description: String,
    val tip: String,
    val benefits: List<String>
)

@Composable
private fun GoalTypeCard(
    option: GoalOptionData,
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
        label = "goalCardScale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 2.dp,
        animationSpec = tween(200),
        label = "goalCardElevation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(elevation, RoundedCornerShape(16.dp))
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        brush = Brush.linearGradient(option.gradientColors),
                        shape = RoundedCornerShape(16.dp)
                    )
                } else {
                    Modifier
                }
            )
            .glassCardThemed(isDark = isDark, cornerRadius = 16.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = Brush.linearGradient(option.gradientColors),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.type.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) option.color else MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                AnimatedVisibility(
                    visible = isSelected,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        option.benefits.forEach { benefit ->
                            Surface(
                                color = option.color.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = benefit,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = option.color,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
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
                        .size(28.dp)
                        .background(
                            brush = Brush.linearGradient(option.gradientColors),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

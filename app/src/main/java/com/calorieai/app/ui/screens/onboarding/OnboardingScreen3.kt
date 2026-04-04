package com.calorieai.app.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calorieai.app.data.model.ActivityLevel
import com.calorieai.app.ui.components.OnboardingNavigationButtons
import com.calorieai.app.ui.theme.*
import kotlin.math.roundToInt

/**
 * 引导界面二：生活习惯设置
 * 活动水平选择、运动习惯选择和每日热量目标 - Glass 毛玻璃风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen3(
    initialActivityLevel: String = "SEDENTARY",
    initialCalorieGoal: Int? = null,
    onNext: (exerciseHabits: List<String>, calorieGoal: Int, activityLevel: String) -> Unit,
    onBack: () -> Unit
) {
    var selectedExerciseHabits by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedActivityLevel by remember { mutableStateOf(initialActivityLevel) }
    var showMoreExercises by remember { mutableStateOf(false) }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // 常用运动（默认显示）
    val commonExercises = listOf(
        ExerciseOption("跑步", Icons.Default.DirectionsRun, Color(0xFF4CAF50)),
        ExerciseOption("游泳", Icons.Default.Pool, Color(0xFF2196F3)),
        ExerciseOption("骑行", Icons.Default.PedalBike, Color(0xFFFF9800)),
        ExerciseOption("健身", Icons.Default.FitnessCenter, Color(0xFF9C27B0)),
        ExerciseOption("瑜伽", Icons.Default.SelfImprovement, Color(0xFFE91E63)),
        ExerciseOption("球类", Icons.Default.SportsBasketball, Color(0xFFFF5722))
    )

    // 更多运动（可折叠）
    val moreExercises = listOf(
        ExerciseOption("徒步", Icons.Default.Hiking, Color(0xFF795548)),
        ExerciseOption("舞蹈", Icons.Default.MusicNote, Color(0xFF00BCD4)),
        ExerciseOption("拳击", Icons.Default.SportsMma, Color(0xFFD32F2F)),
        ExerciseOption("登山", Icons.Default.Terrain, Color(0xFF388E3C)),
        ExerciseOption("滑雪", Icons.Default.AcUnit, Color(0xFF03A9F4)),
        ExerciseOption("滑冰", Icons.Default.Skateboarding, Color(0xFF7B1FA2)),
        ExerciseOption("冲浪", Icons.Default.Surfing, Color(0xFF00ACC1)),
        ExerciseOption("划船", Icons.Default.Rowing, Color(0xFF5D4037)),
        ExerciseOption("跳绳", Icons.Default.FitnessCenter, Color(0xFFFF7043)),
        ExerciseOption("太极", Icons.Default.SelfImprovement, Color(0xFF8BC34A)),
        ExerciseOption("攀岩", Icons.Default.Terrain, Color(0xFF607D8B)),
        ExerciseOption("滑雪橇", Icons.Default.Sledding, Color(0xFF3F51B5))
    )

    val allExercises = if (showMoreExercises) commonExercises + moreExercises else commonExercises

    // 活动水平详情
    val activityLevels = listOf(
        ActivityLevelDetail(
            level = ActivityLevel.SEDENTARY,
            title = "久坐不动",
            examples = "办公室工作、几乎不运动",
            dailySteps = "< 5,000步",
            icon = Icons.Default.Chair
        ),
        ActivityLevelDetail(
            level = ActivityLevel.LIGHT,
            title = "轻度活动",
            examples = "每周1-3次轻度运动",
            dailySteps = "5,000-7,500步",
            icon = Icons.Default.DirectionsWalk
        ),
        ActivityLevelDetail(
            level = ActivityLevel.MODERATE,
            title = "中度活动",
            examples = "每周3-5次中等强度运动",
            dailySteps = "7,500-10,000步",
            icon = Icons.Default.DirectionsRun
        ),
        ActivityLevelDetail(
            level = ActivityLevel.ACTIVE,
            title = "活跃",
            examples = "每周6-7次运动或体力劳动",
            dailySteps = "10,000-12,500步",
            icon = Icons.Default.FitnessCenter
        ),
        ActivityLevelDetail(
            level = ActivityLevel.VERY_ACTIVE,
            title = "非常活跃",
            examples = "每天高强度运动或运动员",
            dailySteps = "> 12,500步",
            icon = Icons.Default.TrendingUp
        )
    )

    OnboardingStepRenderer(
        config = OnboardingStepLayoutConfig(
            appBarTitle = "生活习惯",
            headline = "您的生活习惯",
            subtitle = "选择您的日常活动水平，我们将为您计算推荐热量",
            currentStep = 2,
            totalSteps = 4
        ),
        onBack = onBack
    ) {
            // 活动水平选择
            Text(
                text = "日常活动水平",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activityLevels.forEach { detail ->
                    ActivityLevelCard(
                        detail = detail,
                        isSelected = selectedActivityLevel == detail.level.name,
                        onClick = { selectedActivityLevel = detail.level.name },
                        isDark = isDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 运动习惯选择
            Text(
                text = "运动习惯（可多选）",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 运动选择网格
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allExercises.chunked(3).forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowOptions.forEach { option ->
                            val isSelected = selectedExerciseHabits.contains(option.name)
                            ExerciseChip(
                                option = option,
                                isSelected = isSelected,
                                onClick = {
                                    selectedExerciseHabits = if (isSelected) {
                                        selectedExerciseHabits - option.name
                                    } else {
                                        selectedExerciseHabits + option.name
                                    }
                                },
                                isDark = isDark,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - rowOptions.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // 展开/收起更多运动
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMoreExercises = !showMoreExercises }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (showMoreExercises) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (showMoreExercises) "收起更多运动" else "展开更多运动（共${commonExercises.size + moreExercises.size}种）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 按钮行
            OnboardingNavigationButtons(
                onBack = onBack,
                onNext = {
                    val activityLevel = ActivityLevel.fromString(selectedActivityLevel)
                    val recommendedCalories = calculateRecommendedCalories(activityLevel)
                    onNext(selectedExerciseHabits, recommendedCalories, selectedActivityLevel)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
    }
}

private data class ActivityLevelDetail(
    val level: ActivityLevel,
    val title: String,
    val examples: String,
    val dailySteps: String,
    val icon: ImageVector
)

@Composable
private fun ActivityLevelCard(
    detail: ActivityLevelDetail,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDark: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "activityCardScale"
    )

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(16.dp)
            )
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
                    .size(48.dp)
                    .background(
                        color = if (isSelected) primaryColor.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = detail.icon,
                    contentDescription = null,
                    tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = detail.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "×${detail.level.multiplier}",
                        style = MaterialTheme.typography.labelMedium,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = detail.examples,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "日均 ${detail.dailySteps}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

data class ExerciseOption(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
private fun ExerciseChip(
    option: ExerciseOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "exerciseChipScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                tint = if (isSelected) option.color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = option.name,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) option.color else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

private fun calculateRecommendedCalories(activityLevel: ActivityLevel?): Int {
    val baseCalories = 2000
    val multiplier = activityLevel?.multiplier ?: 1.2f
    return (baseCalories * multiplier).roundToInt()
}

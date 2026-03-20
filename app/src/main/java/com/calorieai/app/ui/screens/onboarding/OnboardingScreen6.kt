package com.calorieai.app.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calorieai.app.data.model.ActivityLevel
import com.calorieai.app.data.model.Gender
import com.calorieai.app.data.model.GoalType
import com.calorieai.app.data.model.WeightLossStrategy
import com.calorieai.app.ui.components.OnboardingNavigationButtons
import com.calorieai.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * 引导界面六：完成确认
 * 展示用户设置摘要，确认完成引导 - Glass 毛玻璃风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen6(
    onboardingData: OnboardingSummaryData,
    onComplete: () -> Unit,
    onBack: () -> Unit,
    onEdit: (step: Int) -> Unit
) {
    var isCompleting by remember { mutableStateOf(false) }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // 完成动画
    val scale by animateFloatAsState(
        targetValue = if (isCompleting) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "completeScale"
    )

    val surfaceColor = if (isDark) GlassDarkColors.CardBackground else GlassLightColors.CardBackground
    val primaryColor = if (isDark) GlassDarkColors.Primary else GlassLightColors.Primary
    val primaryContainerColor = if (isDark) GlassDarkColors.PrimaryContainer else GlassLightColors.PrimaryContainer
    val onSurfaceColor = if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface
    val onSurfaceVariantColor = if (isDark) GlassDarkColors.OnSurfaceVariant else GlassLightColors.OnSurfaceVariant
    val outlineColor = if (isDark) GlassDarkColors.Outline else GlassLightColors.Outline

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("确认信息") },
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OnboardingProgressIndicator(currentStep = 6, totalSteps = 6, isDark = isDark)

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "确认您的信息",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = onSurfaceColor
            )

            Text(
                text = "请确认以下信息是否正确，这将用于制定您的个性化方案",
                style = MaterialTheme.typography.bodyMedium,
                color = onSurfaceVariantColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 信息摘要卡片 - Glass 风格
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(GlassUtils.CornerRadius.XLARGE))
                    .background(surfaceColor.copy(alpha = GlassAlpha.CARD_BACKGROUND))
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 基本信息
                    SummarySection(
                        title = "基本信息",
                        icon = Icons.Default.Person,
                        isDark = isDark,
                        onEdit = { onEdit(1) }
                    ) {
                        InfoRow(label = "性别", value = onboardingData.gender?.displayName ?: "未设置", isDark = isDark)
                        InfoRow(
                            label = "出生日期",
                            value = onboardingData.birthDate?.let { formatDate(it) } ?: "未设置",
                            isDark = isDark
                        )
                    }

                    Divider(color = outlineColor.copy(alpha = 0.2f))

                    // 身体数据
                    SummarySection(
                        title = "身体数据",
                        icon = Icons.Default.MonitorWeight,
                        isDark = isDark,
                        onEdit = { onEdit(2) }
                    ) {
                        InfoRow(
                            label = "身高",
                            value = "${onboardingData.height.roundToInt()} cm",
                            isDark = isDark
                        )
                        InfoRow(
                            label = "当前体重",
                            value = "${onboardingData.currentWeight.roundToInt()} kg",
                            isDark = isDark
                        )
                        InfoRow(
                            label = "目标体重",
                            value = "${onboardingData.targetWeight?.roundToInt() ?: "--"} kg",
                            isDark = isDark
                        )
                        if (onboardingData.bmi != null) {
                            InfoRow(
                                label = "BMI",
                                value = String.format("%.1f", onboardingData.bmi),
                                valueColor = getBmiColor(onboardingData.bmi),
                                isDark = isDark
                            )
                        }
                    }

                    Divider(color = outlineColor.copy(alpha = 0.2f))

                    // 生活习惯
                    SummarySection(
                        title = "生活习惯",
                        icon = Icons.Default.DirectionsRun,
                        isDark = isDark,
                        onEdit = { onEdit(3) }
                    ) {
                        InfoRow(
                            label = "活动水平",
                            value = onboardingData.activityLevel?.displayName ?: "未设置",
                            isDark = isDark
                        )
                        InfoRow(
                            label = "每日目标",
                            value = "${onboardingData.dailyCalorieGoal} 千卡",
                            isDark = isDark
                        )
                    }

                    Divider(color = outlineColor.copy(alpha = 0.2f))

                    // 目标设定
                    SummarySection(
                        title = "目标设定",
                        icon = Icons.Default.Flag,
                        isDark = isDark,
                        onEdit = { onEdit(4) }
                    ) {
                        InfoRow(
                            label = "主要目标",
                            value = onboardingData.goalType?.displayName ?: "未设置",
                            valueColor = getGoalColor(onboardingData.goalType),
                            isDark = isDark
                        )
                        InfoRow(
                            label = "执行策略",
                            value = onboardingData.weightLossStrategy?.displayName ?: "未设置",
                            isDark = isDark
                        )
                        if (onboardingData.estimatedWeeks != null && onboardingData.estimatedWeeks > 0) {
                            InfoRow(
                                label = "预计周期",
                                value = "约 ${onboardingData.estimatedWeeks} 周",
                                isDark = isDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 提示信息 - Glass 卡片
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(GlassUtils.CornerRadius.LARGE))
                    .background(primaryContainerColor.copy(alpha = 0.7f))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "您可以随时在\"我的\"页面修改这些信息",
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurfaceVariantColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 按钮行
            OnboardingNavigationButtons(
                onBack = onBack,
                onNext = {
                    isCompleting = true
                    onComplete()
                },
                nextButtonText = "完成",
                showCheckIcon = true,
                nextButtonColor = primaryColor,
                backButtonContentColor = onSurfaceColor
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * 摘要区块 - Glass 风格
 */
@Composable
private fun SummarySection(
    title: String,
    icon: ImageVector,
    isDark: Boolean,
    onEdit: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val primaryColor = if (isDark) GlassDarkColors.Primary else GlassLightColors.Primary
    val onSurfaceColor = if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface

    Column(
        modifier = Modifier.fillMaxWidth()
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
                        .size(36.dp)
                        .background(
                            color = primaryColor.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurfaceColor
                )
            }

            TextButton(
                onClick = onEdit,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("修改", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.padding(start = 48.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

/**
 * 信息行
 */
@Composable
private fun InfoRow(
    label: String,
    value: String,
    isDark: Boolean,
    valueColor: Color = if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface
) {
    val onSurfaceVariantColor = if (isDark) GlassDarkColors.OnSurfaceVariant else GlassLightColors.OnSurfaceVariant

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = onSurfaceVariantColor
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

/**
 * 引导摘要数据
 */
data class OnboardingSummaryData(
    val gender: Gender? = null,
    val birthDate: Long? = null,
    val height: Float = 0f,
    val currentWeight: Float = 0f,
    val targetWeight: Float? = null,
    val bmi: Float? = null,
    val activityLevel: ActivityLevel? = null,
    val dailyCalorieGoal: Int = 2000,
    val goalType: GoalType? = null,
    val weightLossStrategy: WeightLossStrategy? = null,
    val estimatedWeeks: Int? = null
)

/**
 * 格式化日期
 */
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/**
 * 获取BMI颜色
 */
private fun getBmiColor(bmi: Float): Color {
    return when {
        bmi < 18.5f -> Color(0xFF2196F3) // 偏瘦 - 蓝色
        bmi < 24f -> Color(0xFF4CAF50)   // 正常 - 绿色
        bmi < 28f -> Color(0xFFFF9800)   // 偏胖 - 橙色
        else -> Color(0xFFF44336)        // 肥胖 - 红色
    }
}

/**
 * 获取目标颜色
 */
private fun getGoalColor(goalType: GoalType?): Color {
    return when (goalType) {
        GoalType.LOSE_WEIGHT -> Color(0xFF4CAF50)
        GoalType.GAIN_MUSCLE -> Color(0xFF2196F3)
        GoalType.GAIN_WEIGHT -> Color(0xFFFF9800)
        GoalType.MAINTAIN -> Color(0xFF9C27B0)
        null -> Color.Unspecified
    }
}

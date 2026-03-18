package com.calorieai.app.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.data.model.ActivityLevel
import com.calorieai.app.data.model.Gender
import com.calorieai.app.data.model.GoalType
import com.calorieai.app.data.model.UserSettings
import com.calorieai.app.data.model.WeightLossStrategy
import com.calorieai.app.ui.theme.*
import com.calorieai.app.viewmodel.MyViewModel
import kotlin.math.roundToInt

/**
 * 身体档案页面 - Glass 毛玻璃风格
 * 展示和编辑用户的身体数据和健康目标
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyProfileScreen(
    onEditClick: () -> Unit,
    onNavigateToWeightHistory: () -> Unit,
    onNavigateToGoals: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyViewModel = hiltViewModel()
) {
    val isDark = isSystemInDarkTheme()
    val userSettings by viewModel.userSettings.collectAsState()

    // 页面获得焦点时刷新数据
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refreshSettings()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("身体档案") },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 身体数据概览卡片
            GlassBodyOverviewCard(userSettings, isDark)

            // BMI卡片
            GlassBMICard(userSettings, isDark)

            // 基础代谢率卡片
            GlassMetabolismCard(userSettings, isDark)

            // 目标设定卡片
            GlassGoalsCard(userSettings, onNavigateToGoals, isDark)

            // 生活习惯卡片
            GlassLifestyleCard(userSettings, isDark)

            // 历史记录入口
            GlassHistoryCard(onNavigateToWeightHistory, isDark)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * 身体数据概览卡片 - Glass 风格
 */
@Composable
private fun GlassBodyOverviewCard(userSettings: UserSettings?, isDark: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCardThemed(
                isDark = isDark,
                cornerRadius = 20.dp
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "身体数据",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GlassBodyStatItem(
                    icon = Icons.Default.Person,
                    value = userSettings?.userGender?.let {
                        Gender.fromString(it)?.displayName ?: "--"
                    } ?: "--",
                    label = "性别"
                )

                GlassBodyStatItem(
                    icon = Icons.Default.Cake,
                    value = userSettings?.userAge?.toString() ?: "--",
                    label = "年龄"
                )

                GlassBodyStatItem(
                    icon = Icons.Default.Height,
                    value = userSettings?.userHeight?.roundToInt()?.toString() ?: "--",
                    label = "身高(cm)"
                )

                GlassBodyStatItem(
                    icon = Icons.Default.MonitorWeight,
                    value = userSettings?.userWeight?.roundToInt()?.toString() ?: "--",
                    label = "体重(kg)"
                )
            }
        }
    }
}

/**
 * BMI卡片 - Glass 风格
 */
@Composable
private fun GlassBMICard(userSettings: UserSettings?, isDark: Boolean) {
    val bmi = userSettings?.bmi
    val bmiCategory = getBMICategory(bmi)
    val bmiColor = getGlassBMIColor(bmi, isDark)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCardThemed(
                isDark = isDark,
                cornerRadius = 20.dp
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // BMI圆形指示器
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(bmiColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = bmi?.let { String.format("%.1f", it) } ?: "--",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = bmiColor
                    )
                    Text(
                        text = "BMI",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "BMI指数",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = bmiCategory,
                    style = MaterialTheme.typography.bodyLarge,
                    color = bmiColor,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = getBMIAdvice(bmi),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 基础代谢率卡片 - Glass 风格
 */
@Composable
private fun GlassMetabolismCard(userSettings: UserSettings?, isDark: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCardThemed(
                isDark = isDark,
                cornerRadius = 20.dp
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "代谢数据",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GlassMetabolismItem(
                    label = "基础代谢率 (BMR)",
                    value = "${userSettings?.bmr ?: "--"} 千卡/天",
                    description = "静息状态下的能量消耗"
                )

                GlassMetabolismItem(
                    label = "每日总消耗 (TDEE)",
                    value = "${userSettings?.tdee ?: "--"} 千卡/天",
                    description = "包含活动的总能量消耗"
                )
            }
        }
    }
}

/**
 * 目标设定卡片 - Glass 风格
 */
@Composable
private fun GlassGoalsCard(
    userSettings: UserSettings?,
    onNavigateToGoals: () -> Unit,
    isDark: Boolean
) {
    val goalType = userSettings?.goalType?.let { GoalType.fromString(it) }
    val strategy = userSettings?.weightLossStrategy?.let { WeightLossStrategy.fromString(it) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToGoals() }
            .glassCardThemed(
                isDark = isDark,
                cornerRadius = 20.dp
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "健康目标",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 目标类型
            goalType?.let {
                GlassGoalItem(
                    icon = Icons.Default.Flag,
                    label = "主要目标",
                    value = it.displayName,
                    color = getGlassGoalColor(it, isDark)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 目标体重
            userSettings?.targetWeight?.let { target ->
                GlassGoalItem(
                    icon = Icons.Default.MonitorWeight,
                    label = "目标体重",
                    value = "${target.roundToInt()} kg",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 执行策略
            strategy?.let {
                GlassGoalItem(
                    icon = Icons.Default.TrendingUp,
                    label = "执行策略",
                    value = it.displayName,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 预计达成时间
            userSettings?.estimatedWeeksToGoal?.let { weeks ->
                GlassGoalItem(
                    icon = Icons.Default.CalendarMonth,
                    label = "预计达成",
                    value = "约 $weeks 周",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

/**
 * 生活习惯卡片 - Glass 风格
 */
@Composable
private fun GlassLifestyleCard(userSettings: UserSettings?, isDark: Boolean) {
    val activityLevel = userSettings?.activityLevel?.let { ActivityLevel.fromString(it) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCardThemed(
                isDark = isDark,
                cornerRadius = 20.dp
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "生活习惯",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 活动水平
            activityLevel?.let {
                GlassLifestyleItem(
                    icon = Icons.Default.DirectionsRun,
                    label = "活动水平",
                    value = it.displayName,
                    description = it.description
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 每日热量目标
            GlassLifestyleItem(
                icon = Icons.Default.LocalFireDepartment,
                label = "每日热量目标",
                value = "${userSettings?.dailyCalorieGoal ?: "--"} 千卡",
                description = "根据您的目标计算的建议摄入量"
            )
        }
    }
}

/**
 * 历史记录入口卡片 - Glass 风格
 */
@Composable
private fun GlassHistoryCard(onNavigateToWeightHistory: () -> Unit, isDark: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToWeightHistory() }
            .glassCardThemed(
                isDark = isDark,
                cornerRadius = 20.dp
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "体重历史记录",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "查看完整的体重变化趋势",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// 辅助组件

@Composable
private fun GlassBodyStatItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GlassMetabolismItem(
    label: String,
    value: String,
    description: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun GlassGoalItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
private fun GlassLifestyleItem(
    icon: ImageVector,
    label: String,
    value: String,
    description: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

// 辅助函数

private fun getBMICategory(bmi: Float?): String {
    if (bmi == null) return "未设置"
    return when {
        bmi < 18.5f -> "偏瘦"
        bmi < 24f -> "正常"
        bmi < 28f -> "偏胖"
        else -> "肥胖"
    }
}

private fun getGlassBMIColor(bmi: Float?, isDark: Boolean): Color {
    if (bmi == null) return if (isDark) GlassDarkColors.OnSurfaceVariant else GlassLightColors.OnSurfaceVariant
    return when {
        bmi < 18.5f -> if (isDark) Color(0xFF90CAF9) else Color(0xFF2196F3)
        bmi < 24f -> if (isDark) Color(0xFFA5D6A7) else Color(0xFF4CAF50)
        bmi < 28f -> if (isDark) Color(0xFFFFCC80) else Color(0xFFFF9800)
        else -> if (isDark) Color(0xFFEF9A9A) else Color(0xFFF44336)
    }
}

private fun getBMIAdvice(bmi: Float?): String {
    if (bmi == null) return "请先设置身高和体重数据"
    return when {
        bmi < 18.5f -> "建议适当增加营养摄入，进行力量训练"
        bmi < 24f -> "您的体重在健康范围内，继续保持！"
        bmi < 28f -> "建议适当控制饮食，增加运动量"
        else -> "建议制定减重计划，关注健康饮食"
    }
}

private fun getGlassGoalColor(goalType: GoalType, isDark: Boolean): Color {
    return when (goalType) {
        GoalType.LOSE_WEIGHT -> if (isDark) Color(0xFFA5D6A7) else Color(0xFF4CAF50)
        GoalType.GAIN_MUSCLE -> if (isDark) Color(0xFF90CAF9) else Color(0xFF2196F3)
        GoalType.GAIN_WEIGHT -> if (isDark) Color(0xFFFFCC80) else Color(0xFFFF9800)
        GoalType.MAINTAIN -> if (isDark) Color(0xFFCE93D8) else Color(0xFF9C27B0)
    }
}

package com.calorieai.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calorieai.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthGoalsScreen(
    onNavigateBack: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    var selectedGoalType by remember { mutableStateOf<GoalType?>(null) }
    var targetWeight by remember { mutableStateOf("") }
    var selectedStrategy by remember { mutableStateOf<WeightLossStrategy?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("健康目标") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 目标类型选择
            GoalTypeCard(
                selectedGoal = selectedGoalType,
                onGoalSelected = { selectedGoalType = it },
                isDark = isDark
            )

            // 目标体重设置
            TargetWeightCard(
                currentWeight = "",
                targetWeight = targetWeight,
                onTargetWeightChange = { targetWeight = it },
                isDark = isDark
            )

            // 执行策略选择
            StrategyCard(
                selectedStrategy = selectedStrategy,
                onStrategySelected = { selectedStrategy = it },
                isDark = isDark
            )

            // 保存按钮
            Button(
                onClick = { onNavigateBack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "保存目标",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

enum class GoalType(val displayName: String, val icon: ImageVector, val color: Color) {
    LOSE_WEIGHT("减脂", Icons.Default.TrendingDown, Color(0xFF4CAF50)),
    GAIN_MUSCLE("增肌", Icons.Default.FitnessCenter, Color(0xFF2196F3)),
    GAIN_WEIGHT("增重", Icons.Default.TrendingUp, Color(0xFFFF9800)),
    MAINTAIN("保持现状", Icons.Default.Balance, Color(0xFF9C27B0))
}

enum class WeightLossStrategy(val displayName: String, val description: String, val weeklyChange: Float) {
    AGGRESSIVE("激进", "每周约0.5-1kg", 0.75f),
    BALANCED("平和", "每周约0.3-0.5kg", 0.4f),
    GENTLE("温和", "每周约0.1-0.3kg", 0.2f)
}

@Composable
private fun GoalTypeCard(
    selectedGoal: GoalType?,
    onGoalSelected: (GoalType) -> Unit,
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCardThemed(isDark = isDark, cornerRadius = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "选择目标",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            GoalType.values().forEach { goal ->
                GoalTypeItem(
                    goal = goal,
                    isSelected = selectedGoal == goal,
                    onClick = { onGoalSelected(goal) }
                )
                if (goal != GoalType.values().last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun GoalTypeItem(
    goal: GoalType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) goal.color.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(goal.color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = goal.icon,
                contentDescription = null,
                tint = goal.color,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = goal.displayName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) goal.color else MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.weight(1f))

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = goal.color,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun TargetWeightCard(
    currentWeight: String,
    targetWeight: String,
    onTargetWeightChange: (String) -> Unit,
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCardThemed(isDark = isDark, cornerRadius = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "目标体重",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = targetWeight,
                onValueChange = onTargetWeightChange,
                label = { Text("目标体重 (kg)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                ),
                leadingIcon = {
                    Icon(Icons.Default.MonitorWeight, contentDescription = null)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            if (targetWeight.isNotEmpty() && currentWeight.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                val diff = targetWeight.toFloatOrNull()?.minus(currentWeight.toFloatOrNull() ?: 0f)
                diff?.let {
                    Text(
                        text = when {
                            it > 0 -> "需要增重 ${String.format("%.1f", it)} kg"
                            it < 0 -> "需要减重 ${String.format("%.1f", -it)} kg"
                            else -> "已达到目标体重"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StrategyCard(
    selectedStrategy: WeightLossStrategy?,
    onStrategySelected: (WeightLossStrategy) -> Unit,
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCardThemed(isDark = isDark, cornerRadius = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "执行策略",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            WeightLossStrategy.values().forEach { strategy ->
                StrategyItem(
                    strategy = strategy,
                    isSelected = selectedStrategy == strategy,
                    onClick = { onStrategySelected(strategy) }
                )
                if (strategy != WeightLossStrategy.values().last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun StrategyItem(
    strategy: WeightLossStrategy,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = strategy.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = strategy.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

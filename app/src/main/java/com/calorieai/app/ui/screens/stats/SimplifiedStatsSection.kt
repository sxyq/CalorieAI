package com.calorieai.app.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calorieai.app.utils.WeeklyStat
import kotlin.math.roundToInt

@Composable
fun SimplifiedStatsSection(
    uiState: StatsUiState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IntakeTrendCard(uiState.weeklyStats)
        ConsumptionCard(uiState)
        WeightTrendCard(uiState)
    }
}

@Composable
private fun IntakeTrendCard(weeklyStats: List<WeeklyStat>) {
    val values = weeklyStats.takeLast(7).map { it.totalCalories }
    val maxValue = values.maxOrNull()?.coerceAtLeast(1) ?: 1

    SimplifiedCard(title = "近七日摄入", subtitle = "每日总热量") {
        if (values.isEmpty()) {
            EmptySimpleValue(text = "暂无近七日记录")
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                values.forEachIndexed { index, value ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = value.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((72f * value / maxValue).coerceAtLeast(8f).dp)
                                .background(
                                    color = if (index == values.lastIndex) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.primaryContainer
                                    },
                                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "第${index + 1}日",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsumptionCard(uiState: StatsUiState) {
    val today = uiState.todayStats
    val bmr = today?.bmr ?: 0
    val exercise = today?.exerciseCalories ?: 0
    val total = today?.tdee ?: (bmr + exercise)

    SimplifiedCard(title = "今日消耗", subtitle = "基础代谢与日常活动") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SimpleMetric(label = "基础代谢", value = "$bmr 千卡")
            SimpleMetric(label = "活动消耗", value = "$exercise 千卡")
            SimpleMetric(label = "预计总消耗", value = "$total 千卡", emphasize = true)
        }
    }
}

@Composable
private fun WeightTrendCard(uiState: StatsUiState) {
    val values = uiState.trendChartData.weightData.filterNotNull().takeLast(4)
    val current = values.lastOrNull() ?: uiState.userWeight
    val first = values.firstOrNull() ?: current
    val change = current - first

    SimplifiedCard(title = "体重变化", subtitle = "最近记录") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SimpleMetric(label = "当前体重", value = "${"%.1f".format(current)} 千克", emphasize = true)
            SimpleMetric(
                label = "变化",
                value = when {
                    values.size < 2 -> "暂无对比"
                    change > 0f -> "+${"%.1f".format(change)} 千克"
                    else -> "${"%.1f".format(change)} 千克"
                }
            )
        }
    }
}

@Composable
private fun SimplifiedCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun SimpleMetric(
    label: String,
    value: String,
    emphasize: Boolean = false
) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = value,
            style = if (emphasize) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
            color = if (emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptySimpleValue(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(vertical = 18.dp),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 16.sp
    )
}

package com.calorieai.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

/**
 * 月度总结数据类
 */
data class MonthlySummaryData(
    val yearMonth: YearMonth,
    val totalDays: Int,           // 总天数
    val activeDays: Int,          // 活跃天数（有记录的天数）
    val totalCalories: Int,       // 总热量摄入
    val avgCalories: Int,         // 平均每日热量
    val totalExerciseMinutes: Int, // 总运动时长（分钟）
    val weightChange: Float?,     // 体重变化（kg）
    val goalAchievement: Float    // 目标达成率（0-1）
)

/**
 * 月度总结卡片
 * 整合热力图和统计数据
 */
@Composable
fun MonthlySummaryCard(
    summaryData: MonthlySummaryData,
    heatmapData: List<HeatmapData>,
    modifier: Modifier = Modifier,
    onExpandClick: () -> Unit = {},
    onDayClick: ((LocalDate) -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${summaryData.yearMonth.year}年${summaryData.yearMonth.monthValue}月",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "共记录 ${summaryData.activeDays} 天",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                TextButton(
                    onClick = onExpandClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("查看详情")
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 热力图
            CompactHeatmap(
                data = heatmapData,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 图例
            HeatmapLegend(
                modifier = Modifier.align(Alignment.End)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 统计数据网格
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 平均热量
                StatItem(
                    icon = Icons.Default.LocalFireDepartment,
                    value = "${summaryData.avgCalories}",
                    unit = "千卡/天",
                    color = Color(0xFFFF7043)
                )
                
                // 运动时长
                StatItem(
                    icon = Icons.Default.Timer,
                    value = "${summaryData.totalExerciseMinutes / 60}",
                    unit = "小时运动",
                    color = Color(0xFF66BB6A)
                )
                
                // 体重变化
                summaryData.weightChange?.let { change ->
                    val isPositive = change > 0
                    StatItem(
                        icon = Icons.Default.TrendingUp,
                        value = "${if (isPositive) "+" else ""}${String.format("%.1f", change)}",
                        unit = "kg",
                        color = if (isPositive) Color(0xFFFF7043) else Color(0xFF42A5F5)
                    )
                }
                
                // 目标达成率
                StatItem(
                    icon = Icons.Default.EmojiEvents,
                    value = "${(summaryData.goalAchievement * 100).roundToInt()}%",
                    unit = "达成率",
                    color = Color(0xFFFFCA28)
                )
            }
        }
    }
}

/**
 * 统计项组件
 */
@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    unit: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = unit,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 概览页面月度总结区域
 */
@Composable
fun OverviewMonthlySummary(
    currentMonthData: MonthlySummaryData,
    previousMonthData: MonthlySummaryData?,
    currentMonthHeatmap: List<HeatmapData>,
    modifier: Modifier = Modifier,
    onExpandClick: () -> Unit = {},
    onDayClick: ((LocalDate) -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 标题
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "本月总结",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            // 与上月对比
            previousMonthData?.let { prev ->
                val improvement = currentMonthData.goalAchievement - prev.goalAchievement
                val isBetter = improvement > 0
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isBetter) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (isBetter) Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${if (isBetter) "+" else ""}${(improvement * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isBetter) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 当前月卡片
        MonthlySummaryCard(
            summaryData = currentMonthData,
            heatmapData = currentMonthHeatmap,
            modifier = Modifier.padding(horizontal = 16.dp),
            onExpandClick = onExpandClick,
            onDayClick = onDayClick
        )
    }
}

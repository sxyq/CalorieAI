package com.calorieai.app.ui.components.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 时间维度枚举
 */
enum class TimeDimension {
    DAY, WEEK, MONTH
}

/**
 * 趋势图表数据
 */
data class TrendChartData(
    val dates: List<LocalDate>,
    val calorieIntake: List<Float>,      // 热量摄入
    val exerciseCalories: List<Float>,   // 运动消耗
    val weightData: List<Float?>         // 体重数据（可能为空）
)

/**
 * 统一趋势图表组件
 * 支持按天/周/月切换，显示热量摄入、运动消耗、体重变化
 */
@Composable
fun UnifiedTrendChart(
    data: TrendChartData,
    timeDimension: TimeDimension,
    onTimeDimensionChange: (TimeDimension) -> Unit,
    modifier: Modifier = Modifier,
    showCalories: Boolean = true,
    showExercise: Boolean = true,
    showWeight: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 标题和时间维度选择器
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "趋势分析",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // 时间维度选择器
                TimeDimensionSelector(
                    selected = timeDimension,
                    onSelect = onTimeDimensionChange
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 图例
            TrendChartLegend(
                showCalories = showCalories,
                showExercise = showExercise,
                showWeight = showWeight
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 折线图
            if (data.dates.isNotEmpty()) {
                TrendLineChart(
                    data = data,
                    showCalories = showCalories,
                    showExercise = showExercise,
                    showWeight = showWeight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无数据",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 时间维度选择器
 */
@Composable
private fun TimeDimensionSelector(
    selected: TimeDimension,
    onSelect: (TimeDimension) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        TimeDimension.values().forEach { dimension ->
            val isSelected = dimension == selected
            val text = when (dimension) {
                TimeDimension.DAY -> "按天"
                TimeDimension.WEEK -> "按周"
                TimeDimension.MONTH -> "按月"
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onSelect(dimension) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 图表图例
 */
@Composable
private fun TrendChartLegend(
    showCalories: Boolean,
    showExercise: Boolean,
    showWeight: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showCalories) {
            LegendItem(color = 0xFF2196F3, label = "热量摄入")
            Spacer(modifier = Modifier.width(16.dp))
        }
        if (showExercise) {
            LegendItem(color = 0xFF4CAF50, label = "运动消耗")
            Spacer(modifier = Modifier.width(16.dp))
        }
        if (showWeight) {
            LegendItem(color = 0xFFFF9800, label = "体重")
        }
    }
}

@Composable
private fun LegendItem(color: Long, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(androidx.compose.ui.graphics.Color(color))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 趋势折线图（使用MPAndroidChart）
 */
@Composable
private fun TrendLineChart(
    data: TrendChartData,
    showCalories: Boolean,
    showExercise: Boolean,
    showWeight: Boolean,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    AndroidView(
        modifier = modifier,
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                setDrawGridBackground(false)
                legend.isEnabled = false

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    labelRotationAngle = -45f
                }

                axisLeft.apply {
                    setDrawGridLines(true)
                    axisMinimum = 0f
                }

                axisRight.apply {
                    isEnabled = showWeight // 体重使用右侧Y轴
                    if (showWeight) {
                        setDrawGridLines(false)
                    }
                }
            }
        },
        update = { chart ->
            val lineDataSets = mutableListOf<LineDataSet>()

            // 热量摄入数据集
            if (showCalories && data.calorieIntake.isNotEmpty()) {
                val entries = data.calorieIntake.mapIndexed { index, value ->
                    Entry(index.toFloat(), value)
                }
                val dataSet = LineDataSet(entries, "热量摄入").apply {
                    color = 0xFF2196F3.toInt()
                    setCircleColor(0xFF2196F3.toInt())
                    circleRadius = 4f
                    lineWidth = 2f
                    setDrawValues(false)
                    mode = LineDataSet.Mode.LINEAR
                }
                lineDataSets.add(dataSet)
            }

            // 运动消耗数据集
            if (showExercise && data.exerciseCalories.isNotEmpty()) {
                val entries = data.exerciseCalories.mapIndexed { index, value ->
                    Entry(index.toFloat(), value)
                }
                val dataSet = LineDataSet(entries, "运动消耗").apply {
                    color = 0xFF4CAF50.toInt()
                    setCircleColor(0xFF4CAF50.toInt())
                    circleRadius = 4f
                    lineWidth = 2f
                    setDrawValues(false)
                    mode = LineDataSet.Mode.LINEAR
                }
                lineDataSets.add(dataSet)
            }

            // 体重数据集
            if (showWeight && data.weightData.any { it != null }) {
                val entries = data.weightData.mapIndexedNotNull { index, value ->
                    value?.let { Entry(index.toFloat(), it) }
                }
                if (entries.isNotEmpty()) {
                    val dataSet = LineDataSet(entries, "体重").apply {
                        color = 0xFFFF9800.toInt()
                        setCircleColor(0xFFFF9800.toInt())
                        circleRadius = 4f
                        lineWidth = 2f
                        setDrawValues(false)
                        mode = LineDataSet.Mode.LINEAR
                        axisDependency = com.github.mikephil.charting.components.YAxis.AxisDependency.RIGHT
                    }
                    lineDataSets.add(dataSet)
                }
            }

            // 设置X轴标签
            val dateFormatter = DateTimeFormatter.ofPattern("MM/dd")
            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index in data.dates.indices) {
                        data.dates[index].format(dateFormatter)
                    } else ""
                }
            }

            chart.data = LineData(*lineDataSets.toTypedArray())
            chart.invalidate()
        }
    )
}

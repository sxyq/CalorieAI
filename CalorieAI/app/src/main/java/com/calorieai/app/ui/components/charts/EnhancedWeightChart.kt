package com.calorieai.app.ui.components.charts

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calorieai.app.service.AIPredictionService
import com.calorieai.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

/**
 * 增强体重图表组件 - Glass 毛玻璃风格
 * 支持实际数据、预测曲线、目标线、置信区间
 */
@Composable
fun EnhancedWeightChart(
    actualData: List<WeightDataPoint>,
    prediction: AIPredictionService.WeightPrediction?,
    targetWeight: Float?,
    modifier: Modifier = Modifier,
    showPrediction: Boolean = true,
    showConfidenceInterval: Boolean = true,
    isDark: Boolean = false
) {
    val textMeasurer = rememberTextMeasurer()
    
    val cardBackground = if (isDark) {
        GlassDarkColors.SurfaceContainerHigh.copy(alpha = GlassAlpha.CARD_BACKGROUND)
    } else {
        GlassLightColors.SurfaceContainerHigh.copy(alpha = GlassAlpha.CARD_BACKGROUND)
    }
    
    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.1f)
    } else {
        Color.White.copy(alpha = 0.25f)
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // 标题和图例
            ChartHeader(showPrediction, isDark)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 图表区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val padding = 48f
                    
                    val allData = mutableListOf<WeightDataPoint>()
                    allData.addAll(actualData)
                    
                    prediction?.predictedWeights?.forEach { predicted ->
                        allData.add(
                            WeightDataPoint(
                                date = predicted.date,
                                weight = predicted.weight,
                                isPredicted = true
                            )
                        )
                    }
                    
                    if (allData.isEmpty()) return@Canvas
                    
                    val minWeight = (allData.minOf { it.weight } * 0.98f).coerceAtMost(
                        targetWeight?.times(0.95f) ?: Float.MAX_VALUE
                    )
                    val maxWeight = (allData.maxOf { it.weight } * 1.02f).coerceAtLeast(
                        targetWeight?.times(1.05f) ?: Float.MIN_VALUE
                    )
                    val weightRange = maxWeight - minWeight
                    
                    val minDate = allData.minOf { it.date.time }
                    val maxDate = allData.maxOf { it.date.time }
                    val dateRange = maxDate - minDate
                    
                    // 绘制网格线
                    drawGridLines(
                        canvasWidth,
                        canvasHeight,
                        padding,
                        minWeight,
                        maxWeight,
                        textMeasurer,
                        color = if (isDark) GlassDarkColors.Outline.copy(alpha = 0.3f) 
                                else GlassLightColors.Outline.copy(alpha = 0.3f),
                        isDark = isDark
                    )
                    
                    // 绘制目标线
                    targetWeight?.let { target ->
                        val y = canvasHeight - padding - ((target - minWeight) / weightRange) * (canvasHeight - 2 * padding)
                        val targetColor = if (isDark) GlassDarkColors.Tertiary else GlassLightColors.Tertiary
                        
                        drawLine(
                            color = targetColor,
                            start = Offset(padding, y),
                            end = Offset(canvasWidth - padding, y),
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )
                        
                        drawText(
                            textMeasurer = textMeasurer,
                            text = "目标: ${String.format("%.1f", target)}kg",
                            topLeft = Offset(canvasWidth - 120f, y - 24f),
                            style = TextStyle(
                                color = targetColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                    
                    // 绘制置信区间
                    if (showConfidenceInterval && prediction != null) {
                        drawConfidenceInterval(
                            prediction,
                            canvasWidth,
                            canvasHeight,
                            padding,
                            minWeight,
                            weightRange,
                            minDate,
                            dateRange,
                            isDark
                        )
                    }
                    
                    // 绘制实际数据线和点
                    val primaryColor = if (isDark) GlassDarkColors.Primary else GlassLightColors.Primary
                    
                    if (actualData.size >= 2) {
                        drawWeightLine(
                            actualData,
                            canvasWidth,
                            canvasHeight,
                            padding,
                            minWeight,
                            weightRange,
                            minDate,
                            dateRange,
                            color = primaryColor,
                            strokeWidth = 3f
                        )
                    }
                    
                    // 绘制预测线
                    if (showPrediction && prediction != null) {
                        val predictedData = prediction.predictedWeights.map {
                            WeightDataPoint(it.date, it.weight, true)
                        }
                        
                        if (predictedData.size >= 2) {
                            drawWeightLine(
                                predictedData,
                                canvasWidth,
                                canvasHeight,
                                padding,
                                minWeight,
                                weightRange,
                                minDate,
                                dateRange,
                                color = primaryColor.copy(alpha = 0.4f),
                                strokeWidth = 2f,
                                isDashed = true
                            )
                        }
                    }
                    
                    // 绘制数据点
                    actualData.forEach { point ->
                        val x = padding + ((point.date.time - minDate).toFloat() / dateRange) * (canvasWidth - 2 * padding)
                        val y = canvasHeight - padding - ((point.weight - minWeight) / weightRange) * (canvasHeight - 2 * padding)
                        
                        drawCircle(
                            color = primaryColor,
                            radius = 6f,
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = if (isDark) GlassDarkColors.Surface else GlassLightColors.Surface,
                            radius = 4f,
                            center = Offset(x, y)
                        )
                    }
                }
            }
            
            // 底部统计
            ChartFooter(actualData, prediction, isDark)
        }
    }
}

/**
 * 体重数据点
 */
data class WeightDataPoint(
    val date: Date,
    val weight: Float,
    val isPredicted: Boolean = false
)

/**
 * 图表头部 - Glass 风格
 */
@Composable
private fun ChartHeader(showPrediction: Boolean, isDark: Boolean) {
    val onSurfaceColor = if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface
    val onSurfaceVariantColor = if (isDark) GlassDarkColors.OnSurfaceVariant else GlassLightColors.OnSurfaceVariant
    val primaryColor = if (isDark) GlassDarkColors.Primary else GlassLightColors.Primary
    val tertiaryColor = if (isDark) GlassDarkColors.Tertiary else GlassLightColors.Tertiary
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "体重趋势",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = onSurfaceColor
        )
        
        // 图例
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendItem(
                color = primaryColor,
                text = "实际",
                isDashed = false
            )
            
            if (showPrediction) {
                LegendItem(
                    color = primaryColor.copy(alpha = 0.4f),
                    text = "预测",
                    isDashed = true
                )
            }
            
            LegendItem(
                color = tertiaryColor,
                text = "目标",
                isDashed = true
            )
        }
    }
}

/**
 * 图例项 - Glass 风格
 */
@Composable
private fun LegendItem(color: Color, text: String, isDashed: Boolean) {
    val onSurfaceVariantColor = LocalContentColor.current
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(2.dp)
                .background(
                    color = color,
                    shape = RoundedCornerShape(1.dp)
                )
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = onSurfaceVariantColor.copy(alpha = 0.8f)
        )
    }
}

/**
 * 图表底部统计 - Glass 风格
 */
@Composable
private fun ChartFooter(
    actualData: List<WeightDataPoint>,
    prediction: AIPredictionService.WeightPrediction?,
    isDark: Boolean
) {
    val onSurfaceColor = if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface
    val onSurfaceVariantColor = if (isDark) GlassDarkColors.OnSurfaceVariant else GlassLightColors.OnSurfaceVariant
    val successColor = if (isDark) GlassDarkColors.Tertiary else GlassLightColors.Tertiary
    val errorColor = if (isDark) GlassDarkColors.Error else GlassLightColors.Error
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // 当前体重
        actualData.firstOrNull()?.let { latest ->
            StatItem(
                label = "当前",
                value = "${String.format("%.1f", latest.weight)}kg",
                isDark = isDark
            )
        }
        
        // 变化
        if (actualData.size >= 2) {
            val change = actualData.first().weight - actualData.last().weight
            val changeText = if (change >= 0) "-${String.format("%.1f", change)}" else "+${String.format("%.1f", abs(change))}"
            StatItem(
                label = "变化",
                value = "${changeText}kg",
                valueColor = if (change > 0) successColor else errorColor,
                isDark = isDark
            )
        }
        
        // 预测达成时间
        prediction?.targetDate?.let { date ->
            val daysLeft = ((date.time - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
            StatItem(
                label = "预计达成",
                value = "${daysLeft}天",
                isDark = isDark
            )
        }
        
        // 置信度
        prediction?.let {
            StatItem(
                label = "预测置信度",
                value = "${(it.confidence * 100).toInt()}%",
                isDark = isDark
            )
        }
    }
}

/**
 * 统计项 - Glass 风格
 */
@Composable
private fun StatItem(
    label: String,
    value: String,
    valueColor: Color? = null,
    isDark: Boolean
) {
    val onSurfaceColor = if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface
    val onSurfaceVariantColor = if (isDark) GlassDarkColors.OnSurfaceVariant else GlassLightColors.OnSurfaceVariant
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor ?: onSurfaceColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = onSurfaceVariantColor.copy(alpha = 0.8f)
        )
    }
}

/**
 * 绘制网格线 - Glass 风格
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGridLines(
    canvasWidth: Float,
    canvasHeight: Float,
    padding: Float,
    minWeight: Float,
    maxWeight: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    color: Color,
    isDark: Boolean
) {
    val gridLines = 5
    val weightStep = (maxWeight - minWeight) / gridLines
    val textColor = if (isDark) GlassDarkColors.OnSurfaceVariant else GlassLightColors.OnSurfaceVariant
    
    for (i in 0..gridLines) {
        val weight = minWeight + weightStep * i
        val y = canvasHeight - padding - (i.toFloat() / gridLines) * (canvasHeight - 2 * padding)
        
        // 水平网格线
        drawLine(
            color = color,
            start = Offset(padding, y),
            end = Offset(canvasWidth - padding, y),
            strokeWidth = 1f
        )
        
        // Y轴标签
        drawText(
            textMeasurer = textMeasurer,
            text = "${weight.toInt()}",
            topLeft = Offset(0f, y - 10f),
            style = TextStyle(
                color = textColor,
                fontSize = 11.sp
            )
        )
    }
}

/**
 * 绘制置信区间 - Glass 风格
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawConfidenceInterval(
    prediction: AIPredictionService.WeightPrediction,
    canvasWidth: Float,
    canvasHeight: Float,
    padding: Float,
    minWeight: Float,
    weightRange: Float,
    minDate: Long,
    dateRange: Long,
    isDark: Boolean
) {
    val path = Path()
    
    prediction.predictedWeights.forEachIndexed { index, predicted ->
        val x = padding + ((predicted.date.time - minDate).toFloat() / dateRange) * (canvasWidth - 2 * padding)
        val yUpper = canvasHeight - padding - ((predicted.confidenceInterval.second - minWeight) / weightRange) * (canvasHeight - 2 * padding)
        val yLower = canvasHeight - padding - ((predicted.confidenceInterval.first - minWeight) / weightRange) * (canvasHeight - 2 * padding)
        
        if (index == 0) {
            path.moveTo(x, yUpper)
        } else {
            path.lineTo(x, yUpper)
        }
    }
    
    prediction.predictedWeights.reversed().forEach { predicted ->
        val x = padding + ((predicted.date.time - minDate).toFloat() / dateRange) * (canvasWidth - 2 * padding)
        val yLower = canvasHeight - padding - ((predicted.confidenceInterval.first - minWeight) / weightRange) * (canvasHeight - 2 * padding)
        path.lineTo(x, yLower)
    }
    path.close()
    
    // 绘制置信区间填充
    val primaryColor = if (isDark) GlassDarkColors.Primary else GlassLightColors.Primary
    drawPath(
        path = path,
        color = primaryColor.copy(alpha = 0.08f)
    )
}

/**
 * 绘制体重曲线
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWeightLine(
    data: List<WeightDataPoint>,
    canvasWidth: Float,
    canvasHeight: Float,
    padding: Float,
    minWeight: Float,
    weightRange: Float,
    minDate: Long,
    dateRange: Long,
    color: Color,
    strokeWidth: Float,
    isDashed: Boolean = false
) {
    if (data.size < 2) return
    
    val path = Path()
    
    data.forEachIndexed { index, point ->
        val x = padding + ((point.date.time - minDate).toFloat() / dateRange) * (canvasWidth - 2 * padding)
        val y = canvasHeight - padding - ((point.weight - minWeight) / weightRange) * (canvasHeight - 2 * padding)
        
        if (index == 0) {
            path.moveTo(x, y)
        } else {
            val prevPoint = data[index - 1]
            val prevX = padding + ((prevPoint.date.time - minDate).toFloat() / dateRange) * (canvasWidth - 2 * padding)
            val prevY = canvasHeight - padding - ((prevPoint.weight - minWeight) / weightRange) * (canvasHeight - 2 * padding)
            
            val cp1x = prevX + (x - prevX) / 2
            val cp1y = prevY
            val cp2x = prevX + (x - prevX) / 2
            val cp2y = y
            
            path.cubicTo(cp1x, cp1y, cp2x, cp2y, x, y)
        }
    }
    
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            pathEffect = if (isDashed) PathEffect.dashPathEffect(floatArrayOf(10f, 10f)) else null
        )
    )
}

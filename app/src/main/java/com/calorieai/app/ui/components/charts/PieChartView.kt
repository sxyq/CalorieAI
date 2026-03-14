package com.calorieai.app.ui.components.charts

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF

/**
 * 饼状图组件
 * 用于展示今日摄入状态（热量来源分布）
 */
@Composable
fun PieChartView(
    data: List<Pair<String, Float>>,
    colors: List<Int>,
    modifier: Modifier = Modifier,
    showPercentages: Boolean = true,
    centerText: String? = null
) {
    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                isRotationEnabled = true
                isHighlightPerTapEnabled = true
                dragDecelerationFrictionCoef = 0.95f

                // 中心文字
                this.centerText = centerText
                setCenterTextSize(16f)
                setCenterTextColor(Color.DKGRAY)

                // 图例
                legend.isEnabled = true
                legend.textColor = Color.DKGRAY
                legend.textSize = 11f
                legend.formSize = 10f

                // 饼图配置
                setUsePercentValues(showPercentages)
                setEntryLabelColor(Color.WHITE)
                setEntryLabelTextSize(12f)
                setDrawCenterText(centerText != null)
                setDrawEntryLabels(true)
                holeRadius = 45f
                transparentCircleRadius = 50f
            }
        },
        modifier = modifier,
        update = { chart ->
            val entries = data.map { (label, value) ->
                PieEntry(value, label)
            }

            val dataSet = PieDataSet(entries, "").apply {
                this.colors = colors.take(data.size)
                setDrawIcons(false)
                sliceSpace = 3f
                iconsOffset = MPPointF(0f, 40f)
                selectionShift = 5f
                valueTextColor = Color.WHITE
                valueTextSize = 12f
                valueFormatter = if (showPercentages) {
                    PercentFormatter(chart)
                } else {
                    object : com.github.mikephil.charting.formatter.ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return "${value.toInt()}"
                        }
                    }
                }
            }

            chart.data = PieData(dataSet)
            chart.invalidate()
        }
    )
}

/**
 * 环形图组件
 * 用于展示营养素比例
 */
@Composable
fun DonutChartView(
    data: List<Pair<String, Float>>,
    colors: List<Int>,
    modifier: Modifier = Modifier,
    centerText: String = ""
) {
    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                isRotationEnabled = false
                isHighlightPerTapEnabled = true

                legend.isEnabled = false

                this.centerText = centerText
                setCenterTextSize(14f)
                setCenterTextColor(Color.DKGRAY)

                setUsePercentValues(true)
                setDrawCenterText(true)
                setDrawEntryLabels(false)
                holeRadius = 70f
                transparentCircleRadius = 75f
            }
        },
        modifier = modifier,
        update = { chart ->
            val entries = data.map { (label, value) ->
                PieEntry(value, label)
            }

            val dataSet = PieDataSet(entries, "").apply {
                this.colors = colors.take(data.size)
                setDrawIcons(false)
                sliceSpace = 2f
                selectionShift = 5f
                valueTextColor = Color.TRANSPARENT
            }

            chart.data = PieData(dataSet)
            chart.invalidate()
        }
    )
}

// 预定义颜色
object ChartColors {
    val PRIMARY = listOf(
        Color.parseColor("#2196F3"), // 蓝色
        Color.parseColor("#4CAF50"), // 绿色
        Color.parseColor("#FF9800"), // 橙色
        Color.parseColor("#9C27B0"), // 紫色
        Color.parseColor("#F44336"), // 红色
        Color.parseColor("#00BCD4"), // 青色
        Color.parseColor("#FFEB3B"), // 黄色
        Color.parseColor("#795548")  // 棕色
    )

    val NUTRITION = listOf(
        Color.parseColor("#FF6B6B"), // 蛋白质 - 红色
        Color.parseColor("#4ECDC4"), // 碳水 - 青色
        Color.parseColor("#FFE66D")  // 脂肪 - 黄色
    )

    val MEALS = listOf(
        Color.parseColor("#FF9F43"), // 早餐
        Color.parseColor("#FECA57"), // 早加餐
        Color.parseColor("#FF6B6B"), // 午餐
        Color.parseColor("#48DBFB"), // 午加餐
        Color.parseColor("#1DD1A1"), // 晚餐
        Color.parseColor("#5F27CD"), // 晚加餐
        Color.parseColor("#8395A7")  // 其他加餐
    )
}

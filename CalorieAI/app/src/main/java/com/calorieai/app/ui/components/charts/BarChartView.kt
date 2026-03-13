package com.calorieai.app.ui.components.charts

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

/**
 * 柱状图组件
 * 用于展示各餐次摄入对比
 */
@Composable
fun BarChartView(
    data: List<Pair<String, Float>>,
    colors: List<Int>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                setDrawGridBackground(false)
                isDragEnabled = true
                setScaleEnabled(false)
                legend.isEnabled = false

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    textColor = Color.GRAY
                    textSize = 10f
                }

                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = Color.LTGRAY
                    textColor = Color.GRAY
                    textSize = 10f
                    axisMinimum = 0f
                }
                axisRight.isEnabled = false
            }
        },
        modifier = modifier,
        update = { chart ->
            val entries = data.mapIndexed { index, pair ->
                BarEntry(index.toFloat(), pair.second)
            }

            val dataSet = BarDataSet(entries, "摄入量").apply {
                this.colors = colors.take(data.size)
                valueTextSize = 10f
                valueTextColor = Color.DKGRAY
            }

            chart.xAxis.valueFormatter = IndexAxisValueFormatter(data.map { it.first })
            chart.data = BarData(dataSet).apply {
                barWidth = 0.6f
            }
            chart.invalidate()
        }
    )
}

/**
 * 分组柱状图
 * 用于对比多个数据系列
 */
@Composable
fun GroupedBarChartView(
    data: List<Pair<String, List<Float>>>,
    labels: List<String>,
    colors: List<Int>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                setDrawGridBackground(false)
                isDragEnabled = true
                setScaleEnabled(false)
                legend.isEnabled = true
                legend.textColor = Color.DKGRAY
                legend.textSize = 11f

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    textColor = Color.GRAY
                    textSize = 10f
                }

                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = Color.LTGRAY
                    textColor = Color.GRAY
                    textSize = 10f
                    axisMinimum = 0f
                }
                axisRight.isEnabled = false
            }
        },
        modifier = modifier,
        update = { chart ->
            val barDataSets = labels.mapIndexed { index, label ->
                val entries = data.mapIndexed { i, pair ->
                    BarEntry(i.toFloat(), pair.second.getOrElse(index) { 0f })
                }

                BarDataSet(entries, label).apply {
                    color = colors.getOrElse(index) { Color.GRAY }
                    valueTextSize = 9f
                }
            }

            chart.xAxis.valueFormatter = IndexAxisValueFormatter(data.map { it.first })
            chart.data = BarData(barDataSets).apply {
                barWidth = 0.6f / labels.size
            }
            chart.groupBars(0f, 0.2f, 0.05f)
            chart.invalidate()
        }
    )
}

/**
 * 水平柱状图
 * 用于展示运动消耗排名等
 */
@Composable
fun HorizontalBarChartView(
    data: List<Pair<String, Float>>,
    colors: List<Int>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            com.github.mikephil.charting.charts.HorizontalBarChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                setDrawGridBackground(false)
                isDragEnabled = true
                setScaleEnabled(false)
                legend.isEnabled = false

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    textColor = Color.GRAY
                    textSize = 10f
                }

                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = Color.LTGRAY
                    textColor = Color.GRAY
                    textSize = 10f
                    axisMinimum = 0f
                }
                axisRight.isEnabled = false
            }
        },
        modifier = modifier,
        update = { chart ->
            val entries = data.mapIndexed { index, pair ->
                BarEntry(index.toFloat(), pair.second)
            }

            val dataSet = BarDataSet(entries, "").apply {
                this.colors = colors.take(data.size)
                valueTextSize = 10f
                valueTextColor = Color.DKGRAY
            }

            chart.xAxis.valueFormatter = IndexAxisValueFormatter(data.map { it.first })
            chart.data = BarData(dataSet).apply {
                barWidth = 0.6f
            }
            chart.invalidate()
        }
    )
}

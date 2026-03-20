package com.calorieai.app.ui.components.charts

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter

/**
 * 折线图组件
 * 用于展示摄入趋势
 */
@Composable
fun LineChartView(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    lineColor: Int = Color.parseColor("#6750A4"),
    fillColor: Int = Color.parseColor("#336750A4"),
    showLabels: Boolean = true,
    darkTheme: Boolean = false
) {
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                ChartConfigFactory.configureLineChart(this, context, darkTheme, showLegend = false)
            }
        },
        modifier = modifier,
        update = { chart ->
            val entries = data.mapIndexed { index, pair ->
                Entry(index.toFloat(), pair.second)
            }

            val dataSet = ChartConfigFactory.createLineDataSet(
                entries = entries,
                label = "摄入量",
                color = lineColor,
                darkTheme = darkTheme,
                fillGradient = true
            ).apply {
                this.fillColor = fillColor
            }

            chart.xAxis.valueFormatter = IndexAxisValueFormatter(data.map { it.first })
            chart.data = LineData(dataSet)

            val pointCount = data.size
            if (pointCount > 0) {
                val maxVisible = when {
                    pointCount <= 7 -> pointCount.toFloat()
                    pointCount <= 30 -> 14f
                    else -> 20f
                }
                chart.setVisibleXRangeMaximum(maxVisible)
                chart.isDragEnabled = pointCount > maxVisible
                chart.setScaleXEnabled(pointCount > maxVisible)
                chart.setScaleYEnabled(false)
                chart.moveViewToX((pointCount - 1).toFloat())
            } else {
                chart.isDragEnabled = false
                chart.setScaleXEnabled(false)
                chart.setScaleYEnabled(false)
            }

            chart.invalidate()
        }
    )
}

/**
 * 多线折线图
 * 用于对比多个数据系列
 */
@Composable
fun MultiLineChartView(
    dataSets: List<Pair<String, List<Pair<String, Float>>>>,
    colors: List<Int>,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = false
) {
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                ChartConfigFactory.configureLineChart(this, context, darkTheme, showLegend = true)
            }
        },
        modifier = modifier,
        update = { chart ->
            val lineDataSets = dataSets.mapIndexed { index, (label, data) ->
                val entries = data.mapIndexed { i, pair ->
                    Entry(i.toFloat(), pair.second)
                }

                ChartConfigFactory.createLineDataSet(
                    entries = entries,
                    label = label,
                    color = colors.getOrElse(index) { Color.GRAY },
                    darkTheme = darkTheme,
                    fillGradient = false
                ).apply {
                    setDrawCircles(true)
                    circleRadius = 3f
                    mode = LineDataSet.Mode.LINEAR
                }
            }

            if (dataSets.isNotEmpty()) {
                chart.xAxis.valueFormatter = IndexAxisValueFormatter(dataSets[0].second.map { it.first })
            }
            chart.data = LineData(lineDataSets)
            chart.invalidate()
        }
    )
}

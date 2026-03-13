package com.calorieai.app.ui.components.charts

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

/**
 * 雷达图组件
 * 用于展示营养素均衡分析
 */
@Composable
fun RadarChartView(
    labels: List<String>,
    data: List<Float>,
    modifier: Modifier = Modifier,
    fillColor: Int = Color.parseColor("#332196F3"),
    strokeColor: Int = Color.parseColor("#2196F3")
) {
    AndroidView(
        factory = { context ->
            RadarChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                isRotationEnabled = false

                // Y轴
                yAxis.apply {
                    axisMinimum = 0f
                    setDrawLabels(false)
                }

                // X轴
                xAxis.apply {
                    textColor = Color.DKGRAY
                    textSize = 11f
                }
            }
        },
        modifier = modifier,
        update = { chart ->
            val entries = data.map { value ->
                RadarEntry(value)
            }

            val dataSet = RadarDataSet(entries, "营养素").apply {
                color = strokeColor
                this.fillColor = fillColor
                setDrawFilled(true)
                lineWidth = 2f
                valueTextSize = 10f
                valueTextColor = Color.DKGRAY
            }

            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            chart.data = RadarData(dataSet)
            chart.invalidate()
        }
    )
}

/**
 * 多系列雷达图
 * 用于对比不同日期的营养素摄入
 */
@Composable
fun MultiRadarChartView(
    labels: List<String>,
    dataSets: List<Pair<String, List<Float>>>,
    colors: List<Int>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            RadarChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = true
                legend.textColor = Color.DKGRAY
                legend.textSize = 11f
                isRotationEnabled = false

                yAxis.apply {
                    axisMinimum = 0f
                    setDrawLabels(false)
                }

                xAxis.apply {
                    textColor = Color.DKGRAY
                    textSize = 11f
                }
            }
        },
        modifier = modifier,
        update = { chart ->
            val radarDataSets = dataSets.mapIndexed { index, (label, values) ->
                val entries = values.map { value ->
                    RadarEntry(value)
                }

                val color = colors.getOrElse(index) { Color.GRAY }

                RadarDataSet(entries, label).apply {
                    this.color = color
                    fillColor = Color.argb(50, Color.red(color), Color.green(color), Color.blue(color))
                    setDrawFilled(true)
                    lineWidth = 2f
                    valueTextSize = 9f
                }
            }

            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            chart.data = RadarData(radarDataSets)
            chart.invalidate()
        }
    )
}

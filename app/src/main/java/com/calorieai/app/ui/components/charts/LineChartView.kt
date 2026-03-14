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
    lineColor: Int = Color.parseColor("#2196F3"),
    fillColor: Int = Color.parseColor("#332196F3"),
    showLabels: Boolean = true
) {
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                setDrawGridBackground(false)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
                legend.isEnabled = false

                // X轴配置
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    textColor = Color.GRAY
                    textSize = 10f
                }

                // Y轴配置
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
                Entry(index.toFloat(), pair.second)
            }

            val dataSet = LineDataSet(entries, "摄入量").apply {
                color = lineColor
                setDrawCircles(true)
                setCircleColor(lineColor)
                circleRadius = 4f
                lineWidth = 2f
                setDrawFilled(true)
                this.fillColor = fillColor
                fillAlpha = 50
                valueTextSize = 10f
                valueTextColor = Color.DKGRAY
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }

            chart.xAxis.valueFormatter = IndexAxisValueFormatter(data.map { it.first })
            chart.data = LineData(dataSet)
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
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                setDrawGridBackground(false)
                isDragEnabled = true
                setScaleEnabled(true)
                legend.isEnabled = true
                legend.textColor = Color.DKGRAY
                legend.textSize = 12f

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
            val lineDataSets = dataSets.mapIndexed { index, (label, data) ->
                val entries = data.mapIndexed { i, pair ->
                    Entry(i.toFloat(), pair.second)
                }

                LineDataSet(entries, label).apply {
                    color = colors.getOrElse(index) { Color.GRAY }
                    setDrawCircles(true)
                    setCircleColor(colors.getOrElse(index) { Color.GRAY })
                    circleRadius = 3f
                    lineWidth = 2f
                    setDrawFilled(false)
                    valueTextSize = 9f
                    setDrawValues(false)
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

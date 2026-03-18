package com.calorieai.app.ui.components.charts

import android.content.Context
import android.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.calorieai.app.ui.theme.AppColors
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

object ChartConfigFactory {
    
    fun configureBarChart(
        chart: BarChart,
        context: Context,
        darkTheme: Boolean = false,
        showLegend: Boolean = true,
        showGridLines: Boolean = true
    ) {
        val colors = AppColors.getColorsSync(darkTheme)
        
        chart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setHighlightFullBarEnabled(false)
            setPinchZoom(true)
            setScaleEnabled(true)
            
            axisRight.isEnabled = false
            
            axisLeft.apply {
                setDrawGridLines(showGridLines)
                gridColor = colors.Divider.toArgb()
                textColor = colors.TextSecondary.toArgb()
                textSize = 10f
                setDrawAxisLine(false)
            }
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = colors.TextSecondary.toArgb()
                textSize = 10f
                setDrawAxisLine(false)
            }
            
            legend.apply {
                isEnabled = showLegend
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(true)
                textColor = colors.TextSecondary.toArgb()
                textSize = 10f
            }
            
            animateY(500)
        }
    }
    
    fun configureLineChart(
        chart: LineChart,
        context: Context,
        darkTheme: Boolean = false,
        showLegend: Boolean = true,
        showGridLines: Boolean = true
    ) {
        val colors = AppColors.getColorsSync(darkTheme)
        
        chart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBorders(false)
            setPinchZoom(true)
            setScaleEnabled(true)
            
            axisRight.isEnabled = false
            
            axisLeft.apply {
                setDrawGridLines(showGridLines)
                gridColor = colors.Divider.toArgb()
                textColor = colors.TextSecondary.toArgb()
                textSize = 10f
                setDrawAxisLine(false)
            }
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = colors.TextSecondary.toArgb()
                textSize = 10f
                setDrawAxisLine(false)
            }
            
            legend.apply {
                isEnabled = showLegend
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(true)
                textColor = colors.TextSecondary.toArgb()
                textSize = 10f
            }
            
            animateX(500)
        }
    }
    
    fun configureRadarChart(
        chart: RadarChart,
        context: Context,
        darkTheme: Boolean = false,
        showLegend: Boolean = true
    ) {
        val colors = AppColors.getColorsSync(darkTheme)
        
        chart.apply {
            description.isEnabled = false
            setDrawWeb(true)
            webColor = colors.Divider.toArgb()
            webAlpha = 100
            webLineWidth = 1f
            
            yAxis.apply {
                setDrawLabels(false)
                textColor = colors.TextSecondary.toArgb()
            }
            
            xAxis.apply {
                textColor = colors.TextSecondary.toArgb()
                textSize = 10f
            }
            
            legend.apply {
                isEnabled = showLegend
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                textColor = colors.TextSecondary.toArgb()
                textSize = 10f
            }
            
            animateXY(500, 500)
        }
    }
    
    fun createBarDataSet(
        entries: List<BarEntry>,
        label: String,
        color: Int,
        darkTheme: Boolean = false
    ): BarDataSet {
        val colors = AppColors.getColorsSync(darkTheme)
        return BarDataSet(entries, label).apply {
            this.color = color
            setDrawValues(false)
            formLineWidth = 2f
            formSize = 8f
        }
    }
    
    fun createLineDataSet(
        entries: List<com.github.mikephil.charting.data.Entry>,
        label: String,
        color: Int,
        darkTheme: Boolean = false,
        fillGradient: Boolean = false
    ): LineDataSet {
        val colors = AppColors.getColorsSync(darkTheme)
        return LineDataSet(entries, label).apply {
            this.color = color
            setDrawCircles(true)
            circleRadius = 4f
            circleHoleRadius = 2f
            setCircleColor(color)
            setDrawValues(false)
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2f
            setDrawFilled(fillGradient)
            if (fillGradient) {
                fillColor = color
                fillAlpha = 50
            }
        }
    }
    
    fun createRadarDataSet(
        entries: List<com.github.mikephil.charting.data.RadarEntry>,
        label: String,
        color: Int,
        darkTheme: Boolean = false
    ): RadarDataSet {
        return RadarDataSet(entries, label).apply {
            this.color = color
            setDrawFilled(true)
            fillColor = color
            fillAlpha = 80
            setDrawValues(false)
            lineWidth = 2f
        }
    }
}

class DayAxisValueFormatter(
    private val dates: List<String>
) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        val index = value.toInt()
        return if (index >= 0 && index < dates.size) dates[index] else ""
    }
}

class CalorieAxisValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return "${value.toInt()} kcal"
    }
}

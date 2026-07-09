package ru.koolmax.cycoffline.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ru.koolmax.cycoffline.data.media.FitFile
import ru.koolmax.cycoffline.data.media.FitListType
import ru.koolmax.cycoffline.presentation.MeasureUtil
import ru.koolmax.cycoffline.presentation.ui.statistics.ChartType
import ru.koolmax.cycoffline.ui.theme.LocalCustomColorsPalette

@Composable
fun ColoredIcon(modifier: Modifier = Modifier, drawable: Int = Int.MIN_VALUE, color: Color = Color.Unspecified) {
    Image(modifier = modifier,
        painter = painterResource(id = drawable),
        contentDescription = null,
        colorFilter = ColorFilter.tint(color)
    )
}

object ColorUtil {
    @Composable
    fun getColor(type: ChartType): Color {
        return when (type) {
            ChartType.DISTANCE -> LocalCustomColorsPalette.current.distanceColor
            ChartType.ASCENT -> LocalCustomColorsPalette.current.ascentColor
            ChartType.AVG_SPEED -> LocalCustomColorsPalette.current.speedColor
            ChartType.MOVING_TIME -> LocalCustomColorsPalette.current.movingTime
            ChartType.AVG_HEART_RATE -> LocalCustomColorsPalette.current.heartColor
            ChartType.MAX_HEART_RATE -> LocalCustomColorsPalette.current.heartColor
        }
    }

    @Composable
    fun getColor(type: FitListType) =
        when(type) {
            FitListType.ALTITUDE -> LocalCustomColorsPalette.current.altitudeColor
            FitListType.GRADE -> LocalCustomColorsPalette.current.gradeColor
            FitListType.SPEED -> LocalCustomColorsPalette.current.speedColor
            FitListType.HEART -> LocalCustomColorsPalette.current.heartColor
            FitListType.CADENCE -> LocalCustomColorsPalette.current.cadenceColor
            FitListType.TEMPERATURE -> LocalCustomColorsPalette.current.temperatureColor
            //FitListType.HEART_TIME -> LocalCustomColorsPalette.current.heartTimeColor
        }
}

@Composable
fun getValueFormatter(type: ChartType) =
    when (type) {
        ChartType.DISTANCE -> HorizontalIndicatorProperties(
            textStyle = MaterialTheme.typography.labelLarge,
            contentBuilder = {
                MeasureUtil.getDistance(it.toInt()).first
            })
        ChartType.ASCENT -> HorizontalIndicatorProperties(
            textStyle = MaterialTheme.typography.labelLarge,
            contentBuilder = {
                MeasureUtil.getAscent(it.toInt()).first
            })
        ChartType.AVG_SPEED -> HorizontalIndicatorProperties(
            textStyle = MaterialTheme.typography.labelLarge,
            contentBuilder = {
                MeasureUtil.getSpeed(it).first
            })
        ChartType.MOVING_TIME -> HorizontalIndicatorProperties(
            textStyle = MaterialTheme.typography.labelLarge,
            contentBuilder = {
                MeasureUtil.getDuration(it.toInt())
            })
        ChartType.AVG_HEART_RATE -> HorizontalIndicatorProperties(
            textStyle = MaterialTheme.typography.labelLarge,
            contentBuilder = {
                MeasureUtil.getHeartRate(it.toInt()).first
            })
        ChartType.MAX_HEART_RATE -> HorizontalIndicatorProperties(
            textStyle = MaterialTheme.typography.labelLarge,
            contentBuilder = {
                MeasureUtil.getHeartRate(it.toInt()).first
            })
    }

fun Iterable<Number>.minChart(): Pair<Double, Double> {
    val min = this.minWithOrNull( compareBy{ it.toDouble() } ) ?: 0
    val max = this.maxWithOrNull( compareBy{ it.toDouble() } ) ?: 0
    return Pair(min.toDouble(), max.toDouble())
}

fun interpolateY(yValues: List<Double>, xValues: List<Int>, steps: Int): List<Double> {
    require(yValues.size == xValues.size) {
        "Lists must be of the same size"
    }
    require(steps > 1) {
        "Number of steps must be greater than 1"
    }

    val minX = xValues.first()
    val maxX = xValues.last()

    val stepSize = (maxX - minX).toDouble() / (steps - 1)
    val interpolatedPoints = MutableList(steps) { 0.0 }

    var idx = 0
    var currentIndex = 0

    for (i in 0 until steps) {
        val x = minX + i * stepSize

        // Поиск интервала для интерполяции
        while (currentIndex < xValues.size - 1 && xValues[currentIndex + 1] <= x) {
            currentIndex++
        }

        val x0 = xValues[currentIndex].toDouble()
        val y0 = yValues[currentIndex]
        val x1 = if (currentIndex < xValues.size - 1) xValues[currentIndex + 1].toDouble() else x0
        val y1 = if (currentIndex < yValues.size - 1) yValues[currentIndex + 1] else y0

        val yInterp = if (x1 == x0) {
            y0 // избегаем деления на ноль, если точки совпадают
        } else {
            // линейная интерполяция
            y0 + (y1 - y0) * ((x - x0) / (x1 - x0))
        }

        interpolatedPoints[idx] = yInterp
        idx += 1
    }
    return interpolatedPoints
}

fun interpolateX(distanceValues: List<Int>, steps: Int): List<Int> {
    require(steps > 1) {
        "Number of steps must be greater than 1"
    }
    val minX = distanceValues.first()
    val maxX = distanceValues.last()

    val stepSize = (maxX - minX).toDouble() / (steps - 1)
    val interpolatedPoints = MutableList(steps) { 0 }

    var idx = 0
    for (i in 0 until steps) {
        val x = minX + i * stepSize

        interpolatedPoints[idx] = x.toInt()
        idx += 1
    }
    return interpolatedPoints
}
package ru.koolmax.cycoffline.presentation.ui.lib.сhart

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.lineChart.TypeLine
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.lineChart.calculateOffset
import kotlin.div
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min
import kotlin.times

sealed class PathData(var path: Path = Path()) {
    object Empty: PathData()

    class Line(
        val yData: List<Float>,
        val xData: List<Int>,
        val yMin: Float,
        val yMax: Float,
        val close: Boolean,
        val typeLine: TypeLine,
        val rounded: Boolean,
        val rect: Rect,
    ): PathData() {
        fun getData(position: Offset) : Pair<Float, Float> {
            when (typeLine) {
                TypeLine.Linear -> {
                    val idx = (position.x - rect.left) / rect.width * (yData.size - 1)
                    val idx1 = floor(idx).toInt()
                    val idx2 = ceil(idx).toInt()
                    val coefficient = if (idx2 == idx1) 0f else (idx - idx1) / (idx2 - idx1)
                    return Pair(
                        (xData[idx2] - xData[idx1]) * coefficient + xData[idx1],
                        (yData[idx2] - yData[idx1]) * coefficient + yData[idx1]
                    )
                }
                TypeLine.Step -> {
                    val idx = (position.x - rect.left) / rect.width * (yData.size - 1)
                    val idx1 = floor(idx).toInt()
                    return Pair(xData[idx1].toFloat(), yData[idx1])
                }
            }
        }

        init {
            require(yData.size == xData.size)
            if (!yData.isEmpty()) {
                val calculateHeight = { value: Float ->
                    calculateOffset(
                        maxValue = yMax,
                        minValue = yMin,
                        total = rect.height,
                        value = value
                    )
                }
                val yValClose = when {
                        (yMin >= 0 && yMax >= 0) -> yMin
                        (yMin <= 0 && yMax <= 0) -> yMax
                        else -> 0f
                    }

                if (close) {
                    val y = rect.bottom - calculateHeight(yValClose)
                    path.moveTo(rect.left, y)
                    path.lineTo(rect.left, (rect.bottom - calculateHeight(yData[0])))
                } else {
                    path.moveTo(rect.left, (rect.bottom - calculateHeight(yData[0])))
                }

                val yStep = rect.width / (xData.size - 1)

                when (typeLine) {
                    TypeLine.Linear -> {
                        for (i in 0 until yData.size - 1) {
                            val x1 = rect.left + (i * yStep)
                            val y1 = -rect.top + rect.height - calculateHeight(yData[i])
                            val x2 = rect.left + ((i + 1) * yStep)
                            val y2 = -rect.top + rect.height - calculateHeight(yData[i + 1])

                            if (rounded) {
                                val cx = (x1 + x2) / 2f
                                path.cubicTo(x1 = cx, y1 = y1, x2 = cx, y2 = y2, x3 = x2, y3 = y2)
                            } else {
                                path.cubicTo(x1, y1, x1, y1, (x1 + x2) / 2, (y1 + y2) / 2)
                                path.cubicTo((x1 + x2) / 2, (y1 + y2) / 2, x2, y2, x2, y2)
                            }
                        }
                    }
                    TypeLine.Step -> {
                        for (i in 0 until yData.size - 1) {
                            val x1 = rect.left + (i * yStep)
                            val y1 = -rect.top + rect.height - calculateHeight(yData[i])
                            val x2 = rect.left + ((i + 1) * yStep)
                            val y2 = -rect.top + rect.height - calculateHeight(yData[i + 1])
                            path.lineTo(x2, y1)
                            path.lineTo(x2, y2)
                        }
                    }
                }

                if (close) {
                    val x = rect.left + ((yData.size - 1) * yStep)
                    val y = rect.bottom - calculateHeight(yValClose)
                    path.lineTo(x, y)
                    path.close()
                }
            }
        }
    }

    class Bar(val dataPoints: List<Float>,
              val yMin: Float,
              val yMax: Float,
              val rect: Rect
    ): PathData() {

        fun getIndex(position: Offset) : Int {
            return rectList.indexOfFirst { it.contains(position) }
        }

        private val rectList = mutableListOf<Rect>()

        init {
            val calculateHeight = { value: Float ->
                calculateOffset(
                    minValue = yMin,
                    maxValue = yMax,
                    total = rect.height,
                    value = value
                )
            }

            val barHeight = min((rect.width - rect.left) / dataPoints.size.toFloat() / 4.0, 20.0).toInt()

            val shift = (rect.width - ((dataPoints.size-1) * (rect.width / dataPoints.size))) / 2

            for ((idx, itm) in dataPoints.withIndex()) {
                val x1 = rect.left + (idx * (rect.width / (dataPoints.size)))
                val y1 = -rect.top + rect.height - calculateHeight(yMin)
                val y2 = -rect.top + rect.height - calculateHeight(itm)

                val rectArea = Rect(shift + x1, rect.top, shift + x1 + barHeight, rect.bottom)
                val rect = Rect(shift + x1, y2, shift + x1 + barHeight, y1)
                rectList.add(rectArea)
                path.addRect(rect)
            }
        }
    }

    /*class XZone(val x1: Float,
                val x2: Float,
                val color: Color,
                val alpha: Float
                ): PathData() {
        init {

        }
    }*/
}
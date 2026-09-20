package ru.koolmax.cycoffline.presentation.ui.lib.сhart.lineChart

import android.util.Log
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.AxisProperties
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.AxisType
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.ChartGeometry
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.ChartZone
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.GridProperties
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.PathData
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.PopupProperties
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.drawPopup
import kotlin.math.max

@Composable
fun LineChart(
    modifier: Modifier = Modifier,
    data: Line,
    xData: List<Int>,
    xZoneList: List<ChartZone> = listOf(),
    yZoneList: List<ChartZone> = listOf(),
    axisProperties: AxisProperties = AxisProperties(textMeasurer = rememberTextMeasurer()),
    gridProperties: GridProperties = GridProperties(),
    popupProperties: PopupProperties = PopupProperties()
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    var chartSize by remember(density) { mutableStateOf(Size(0f, 0f)) }

    val pathMeasure = remember(chartSize) {
        PathMeasure()
    }

    var pathData by remember(chartSize, data) {
        mutableStateOf(PathData.Empty as PathData)
    }

    val yGridLinesList = remember(chartSize, data) {
        mutableStateListOf<Pair<Offset, Offset>>()
    }

    val xGridLinesList = remember(chartSize, data) {
        mutableStateListOf<Pair<Offset, Offset>>()
    }

    val geometry by remember(chartSize, data) {
        mutableStateOf(ChartGeometry.create(chartSize, axisProperties.textMeasurer,
            axisProperties.xIndicatorBuilder,xData.min(), xData.max(),
            axisProperties.yIndicatorBuilder, data.values.min(), data.values.max()))
    }

    var yAxis = remember(data) {
        xGridLinesList.clear()
        AxisEmpty() as Axis
    }

    var xAxis = remember(xData) {
        yGridLinesList.clear()
        AxisEmpty() as Axis
    }

    var popup by remember(data) {
        mutableStateOf<Popup>(Popup.Empty)
    }

    fun PointerInputScope.showPopup(position: Offset) {
        //yIndicatorSize.width.toFloat()
        val data = pathData
        if(data is PathData.Line) {

            val position = Offset(
                x = position.x.coerceIn(geometry.chart.left, geometry.chart.right),
                y = position.y.coerceIn(geometry.chart.top, geometry.chart.bottom)
            )
            val value = data.getData(position)
            popup = Popup.Show(
                position = position,
                x = value.first.toInt(),
                y = value.second,
                min = yAxis.max,
                max = yAxis.min,
                property = popupProperties,
                geometry.chart,
                textMeasurer = axisProperties.textMeasurer,
                null
            )
        }
    }

    Box(modifier = modifier) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Canvas(modifier = Modifier
                .fillMaxSize().pointerInput(data, pathData) {
                    detectTapGestures(
                        onPress = {
                            showPopup(
                                //data = data,
                                //size = size,
                                position = it
                            )
                            tryAwaitRelease()
                        }
                    )
                }.pointerInput(data, pathData) {
                    detectHorizontalDragGestures(
                    onDragEnd = {
                        scope.launch {
                            //hidePopup()
                        }
                    },
                    onHorizontalDrag = { change, _ ->
                        if(popupProperties.enabled) {
                            showPopup(
                                position = change.position
                            )
                        }
                    })
                }) {
                chartSize = size

                //Log.i("cycoffline1", "${data.type} size = ${size}")

                if(geometry.isEmpty) return@Canvas

                //Log.i("cycoffline1", "size = ${size} chart = ${geometry.chart} ${geometry.xIndicatorSize} ${geometry.yIndicatorSize}")
                if(data.isNotEmpty && geometry.isNotEmpty) {
                    if(yAxis is AxisEmpty) {
                        yAxis = when(axisProperties.yType) {
                            AxisType.INT -> AxisNumber.create(
                                data.values.minOfOrNull { it } ?: 0f,
                                data.values.maxOfOrNull { it } ?: 0f,
                                (geometry.chart.height / geometry.yIndicatorSize.height).toInt()
                            )
                            AxisType.TIME -> AxisTime.create(
                                data.values.minOfOrNull { it } ?: 0f,
                                data.values.maxOfOrNull { it } ?: 0f,
                                (geometry.chart.height / geometry.yIndicatorSize.height).toInt()
                            )
                        }
                    }
                    if(xAxis is AxisEmpty) {
                        xAxis = when(axisProperties.xType) {
                            AxisType.INT -> AxisNumber.create(xData.firstOrNull()?.toFloat() ?: 0f, xData.lastOrNull()?.toFloat() ?: 0f, (geometry.chart.width / geometry.xIndicatorSize.width).toInt())
                            AxisType.TIME -> AxisTime.create(xData.firstOrNull()?.toFloat() ?: 0f, xData.lastOrNull()?.toFloat() ?: 0f, (geometry.chart.width / geometry.xIndicatorSize.width).toInt())
                        }
                    }
                }

                if (pathData is PathData.Empty && data.isNotEmpty && geometry.isNotEmpty) {
                    pathData = PathData.Line(
                        yData = data.values,
                        xData = xData,
                        yMin = yAxis.min,
                        yMax = yAxis.max,
                        close = data.drawStyle == DrawStyle.Fill,
                        typeLine = data.type,
                        rounded = false,
                        rect = geometry.chart
                    )
                }

                //if(pathZone.value.isEmpty && xZoneList.isNotEmpty()) {
                //    xZoneList.forEach {
                //        getYLine((xData.size - 1).toFloat(), 0f, 30f,  geometry.chart)
                //    }
                //}


                if (xGridLinesList.isEmpty()) {
                    xGridLinesList.addAll(yAxis.values.map {
                        getXLine(yAxis.min, yAxis.max,it, geometry.chart)
                    })
                }
                xGridLinesList.forEach {
                    drawLine(
                        start = it.first, end = it.second, color = gridProperties.color,
                        strokeWidth = gridProperties.thickness.toPx()
                    )
                }

                if (xAxis.values.isNotEmpty() && yGridLinesList.isEmpty()) {
                    val matchingIndices = mutableListOf<Int>()

                    var idx = 0
                    xData.forEachIndexed { index, i ->
                        val nextIdx = index + 1
                        if (nextIdx < xData.size && idx < xAxis.values.size) {
                            val v = xAxis.values[idx]
                            if ((i >= v) && (v < xData[nextIdx])) {
                                matchingIndices.add(index)
                                idx++
                            }
                        }
                    }
                    yGridLinesList.addAll(matchingIndices.map {
                        getYLine((xData.size - 1).toFloat(), 0f, it.toFloat(),  geometry.chart)
                    })
                }

                yGridLinesList.forEach {
                    drawLine(
                        start = it.first, end = it.second, color = gridProperties.color,
                        strokeWidth = gridProperties.thickness.toPx()
                    )
                }

                var pathEffect: PathEffect? = null
                val stroke: Float = when (val drawStyle = data.drawStyle) {
                    is DrawStyle.Fill -> 0f
                    is DrawStyle.Stroke -> drawStyle.width.toPx()
                }

                if(data.drawStyle is DrawStyle.Fill) {
                    drawPath(
                        path = pathData.path,
                        color = data.fillColor
                    )
                } else {
                    drawPath(
                        path = pathData.path,
                        brush = data.color,
                        style = Stroke(width = stroke, pathEffect = pathEffect)
                    )
                }

                if (xAxis.values.isNotEmpty() && yGridLinesList.isNotEmpty()) {
                    var beforeLabelPos = 0f
                    yGridLinesList.forEachIndexed { index, itm ->
                        if (index > 0) {
                            val str = axisProperties.xIndicatorBuilder(xAxis.values[index].toInt())
                            val textLayoutResult = axisProperties.textMeasurer.measure(str)
                            val textWidthInDp = textLayoutResult.size.width
                            if (beforeLabelPos < itm.first.x - textWidthInDp / 2) {
                                beforeLabelPos = (itm.first.x - textWidthInDp / 2 + textWidthInDp)
                                drawText(
                                    textMeasurer = axisProperties.textMeasurer,
                                    text = str,
                                    style = axisProperties.textStyle,
                                    topLeft = Offset(itm.first.x - textWidthInDp / 2, geometry.chart.bottom)
                                )
                            }
                        }
                    }
                }

                if (yAxis.values.isNotEmpty() && xGridLinesList.isNotEmpty()) {
                    xGridLinesList.forEachIndexed { index, itm ->
                        val str = axisProperties.yIndicatorBuilder(yAxis.values[index])
                        val textLayoutResult = axisProperties.textMeasurer.measure(str)
                        val textHeightInDp = textLayoutResult.size.height
                        drawText(
                            textMeasurer = axisProperties.textMeasurer,
                            text = str,
                            style = axisProperties.textStyle,
                            topLeft = Offset(0f, itm.first.y - textHeightInDp / 2)
                        )
                    }
                }
                if(xZoneList.isNotEmpty() && geometry.isNotEmpty) {
                    xZoneList.forEach {
                        val area = getYBar(xAxis.min, xAxis.max, it.begin, it.end, geometry.chart)
                        drawRect(color = it.color, topLeft = area.first, size = area.second, alpha = it.alpha)
                        //Log.i("cycoffline1", "$it $area")
                    }
                }

                if(yZoneList.isNotEmpty() && geometry.isNotEmpty) {
                    yZoneList.forEach {
                        val area = getXBar(yAxis.min, yAxis.max, it.begin, it.end, geometry.chart)
                        drawRect(color = it.color, topLeft = area.first, size = area.second, alpha = it.alpha)
                        //Log.i("cycoffline1", "$it $area")
                    }
                }

                if(popup is Popup.Show<*>) {
                    drawPopup(
                        popup = popup as Popup.Show<*>,
                        property = popupProperties,
                        textMeasurer = axisProperties.textMeasurer,
                    )
                }
            }
        }
    }
}


sealed class Popup {
    object Empty : Popup()

    class Show<T>(position: Offset, x: Int, y: Float, min: Float, max: Float, property : PopupProperties, rect: Rect, textMeasurer: TextMeasurer, val obj: T): Popup() {
        val linePath = Path()
        val rectPath: Pair<Offset, Size>
        val point: Offset
        val text: String

        init {
            text = "${property.yIndicatorBuilder(y)}\n${property.xIndicatorBuilder(x)}"
            val textLayoutResult = textMeasurer.measure(text)
            linePath.moveTo(position.x, textLayoutResult.size.height.toFloat())
            linePath.lineTo(position.x, rect.bottom)

            point = Offset(position.x, calculateOffset(minValue = min, maxValue = max, total = rect.height, value = y))

            val topLeft = Offset((position.x - 6f).coerceIn(rect.left, rect.right - textLayoutResult.size.width), 0f)

            rectPath = Pair(topLeft, textLayoutResult.size.toSize())
        }
    }
}

internal data class Value(
    val calculatedValue: Double,
    val offset: Offset,
)

fun max(v1: IntSize, v2: IntSize) =
    IntSize(max(v1.width, v2.width),
        max(v1.height, v2.height))

fun calculateOffset(
    minValue: Float,
    maxValue: Float,
    total: Float,
    value:Float
): Float {
    val range = maxValue - minValue
    val percentage = (value - minValue) / range
    val offset = total * percentage
    return offset
}

sealed class IndicatorProperties(
    open val enabled:Boolean,
    open val textStyle: TextStyle,
    open val padding: Dp,
    open val contentBuilder: (Double) -> String,
    open val indicators:List<Double> = emptyList()
)

data class HorizontalIndicatorProperties(
    override val enabled: Boolean = true,
    override val textStyle: TextStyle = TextStyle.Default.copy(fontSize = 12.sp),
    override val padding: Dp = 12.dp,
    override val contentBuilder: (Double) -> String = {
        it.toString()
    },
    override val indicators: List<Double> = emptyList(),
) : IndicatorProperties(
    enabled = enabled,
    textStyle = textStyle,
    contentBuilder = contentBuilder,
    padding = padding ,
    indicators = indicators
)

internal fun DrawScope.getYBar(
    min: Float,
    max: Float,
    x1: Float,
    x2: Float,
    rect: Rect
): Pair<Offset, Size> {
    val offset1 = calculateOffset(
        minValue = min,
        maxValue = max,
        total = rect.width,
        value = x1.coerceIn(min, max)
    )
    val offset2 = calculateOffset(
        minValue = min,
        maxValue = max,
        total = rect.width,
        value = x2.coerceIn(min, max)
    )
    //Log.i("cycoffline1", "$min = $x1 $max = $x2")
    //return Pair(Offset((rect.left + offset1).coerceIn(rect.left, rect.right), 0f), Size(20f, rect.bottom))
    return Pair(Offset(rect.left + offset1, 0f), Size(offset2 - offset1, rect.bottom))
}

internal fun DrawScope.getXBar(
    min: Float,
    max: Float,
    y1: Float,
    y2: Float,
    rect: Rect
): Pair<Offset, Size> {
    val offset1 = calculateOffset(
        minValue = min,
        maxValue = max,
        total = rect.height,
        value = y1.coerceIn(min, max)
    )
    val offset2 = calculateOffset(
        minValue = min,
        maxValue = max,
        total = rect.height,
        value = y2.coerceIn(min, max)
    )
    return Pair(Offset(rect.left, rect.bottom - offset2), Size(rect.width, offset2 - offset1))
}

internal fun DrawScope.getYLine(
    maxValue: Float,
    minValue: Float,
    value: Float,
    rect: Rect
): Pair<Offset, Offset> {
    val offset = calculateOffset(
        minValue = minValue,
        maxValue = maxValue,
        total = rect.width,
        value = value
    )
    return Pair(Offset(rect.left + offset, 0f), Offset(rect.left + offset, rect.height))
}

internal fun DrawScope.getXLine(
    minValue: Float,
    maxValue: Float,
    value: Float,
    rect: Rect
): Pair<Offset, Offset> {
    val offset = calculateOffset(
        minValue = minValue,
        maxValue = maxValue,
        total = rect.height,
        value = value
    )
    return Pair(Offset(rect.left, rect.bottom - offset), Offset(rect.right, rect.bottom - offset))
}
package ru.koolmax.cycoffline.presentation.ui.lib.сhart.barChart

import android.util.Log
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.lineChart.Axis
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.lineChart.AxisEmpty
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.lineChart.AxisNumber
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.lineChart.AxisTime
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.GridProperties
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.AxisType
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.AxisProperties
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.ChartGeometry
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.PathData
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.PopupProperties
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.lineChart.Popup
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.drawPopup
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.lineChart.getXLine

@Composable
fun <T>BarChart(
    modifier: Modifier = Modifier,
    data: Bars<T>,
    xData: List<Int>,
    axisProperties: AxisProperties = AxisProperties(textMeasurer = rememberTextMeasurer()),
    gridProperties: GridProperties = GridProperties(),
    popupProperties: PopupProperties = PopupProperties(),
    onClick: (T) -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var chartSize by remember(density) { mutableStateOf(Size(0f, 0f)) }

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
        AxisEmpty() as Axis
    }

    var xAxis = remember(xData) {
        AxisEmpty() as Axis
    }

    LaunchedEffect(data, yAxis, xData) {
        yGridLinesList.clear()
        xGridLinesList.clear()
    }

    var popup by remember(data) {
        mutableStateOf<Popup>(Popup.Empty as Popup)
    }

    fun PointerInputScope.showPopup(position: Offset) {
        //yIndicatorSize.width.toFloat()
        val bar = pathData as PathData.Bar

        val position = Offset(x = position.x.coerceIn(geometry.chart.left, geometry.chart.right),
            y = position.y.coerceIn(geometry.chart.top, geometry.chart.bottom))
        val idx = bar.getIndex(position)
        if (idx != -1) {
            popup = Popup.Show(
                position = position,
                x = xData[idx],
                y = bar.dataPoints[idx],
                min = yAxis.max,
                max = yAxis.min,
                property = popupProperties,
                geometry.chart,
                axisProperties.textMeasurer,
                data.objects[idx]
            )
        }
    }

    var onPressJob: Job? = null

    Box(modifier = modifier) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Canvas(modifier = Modifier
                .fillMaxSize().pointerInput(data, pathData) {
                    detectTapGestures(
                        onPress = {
                            if(popupProperties.enabled) {
                                if (onPressJob?.isActive == true) {
                                    onPressJob?.cancel()
                                    onPressJob = null
                                }

                                onPressJob = scope.launch {
                                    showPopup(
                                        position = it
                                    )

                                    tryAwaitRelease()
                                    //delay(timeMillis = popupProperties.duration)

                                    //hidePopup()
                                }
                                if(popup is Popup.Show<*>) {
                                    onClick((popup as Popup.Show<T>).obj)
                                }
                            }
                            //(pathData as PathData.Bar).let {
                            //    val idx = .getIndex(it)
                            //}
                        }

                        //onPress = { position ->
                        //    (pathData as PathData.Bar)?.let {
                        //        val idx = it.getIndex(position)
                        //        if(idx > 0)
                        //            onClick(data.objects[idx])
                        //    }
                        //}
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

                if (pathData is PathData.Empty && data.values.count()>0 && geometry.isNotEmpty) {
                    yAxis = when(axisProperties.yType) {
                        AxisType.INT -> AxisNumber.create(
                            data.values.minOfOrNull { it } ?: 0f,
                            data.values.maxOfOrNull { it } ?: 0f,
                            (chartSize.height / geometry.yIndicatorSize.width).toInt()
                        )
                        AxisType.TIME -> AxisTime.create(
                            data.values.minOfOrNull { it } ?: 0f,
                            data.values.maxOfOrNull { it } ?: 0f,
                            (chartSize.height / geometry.yIndicatorSize.width).toInt())
                    }

                    //Log.i("cycoffline1", "${geometry.chart.width} ${geometry.xIndicatorSize.width}")

                    xAxis = when(axisProperties.xType) {
                        AxisType.INT -> AxisNumber.create(xData.firstOrNull()?.toFloat() ?: 0f, xData.lastOrNull()?.toFloat() ?: 0f, (geometry.chart.width / geometry.xIndicatorSize.width).toInt())
                        AxisType.TIME -> AxisTime.create(xData.firstOrNull()?.toFloat() ?: 0f, xData.lastOrNull()?.toFloat() ?: 0f, (geometry.chart.width / geometry.xIndicatorSize.width).toInt())
                    }

                    pathData = PathData.Bar(
                        dataPoints = data.values.map { it },
                        yMin = yAxis.min,
                        yMax = yAxis.max,
                        rect = geometry.chart
                    )
                }

                if (yGridLinesList.isEmpty()) {
                    yGridLinesList.addAll(yAxis.values.map {
                        getXLine(yAxis.min, yAxis.max,it, geometry.chart)
                    })
                }

                yGridLinesList.forEach {
                    drawLine(
                        start = it.first, end = it.second, color = gridProperties.color,
                        strokeWidth = gridProperties.thickness.toPx()
                    )
                }

                if (!xAxis.values.isEmpty() && xGridLinesList.isEmpty()) {
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

                    //xGridLinesList.addAll(matchingIndices.map {
                    //    getXLine((xData.size - 1).toDouble(), 0.toDouble(), yIndicatorSize.width.toFloat(), it.toFloat(), chartSize)
                    //})
                }

                xGridLinesList.forEach {
                    drawLine(
                        start = it.first, end = it.second, color = gridProperties.color,
                        strokeWidth = gridProperties.thickness.toPx()
                    )
                }

                drawPath(
                    path = pathData.path,
                    brush = data.color)

                xGridLinesList.forEachIndexed { index, itm ->
                    if (index > 0) {
                        val str = axisProperties.xIndicatorBuilder(xAxis.values[index].toInt())
                        val textLayoutResult = axisProperties.textMeasurer.measure(str)
                        val textWidthInDp = textLayoutResult.size.width
                        drawText(
                            textMeasurer = axisProperties.textMeasurer,
                            text = str,
                            style = axisProperties.textStyle,
                            topLeft = Offset(itm.first.x - textWidthInDp / 2, chartSize.height + 8)
                        )
                    }
                }

                yGridLinesList.forEachIndexed { index, itm ->
                    val str = axisProperties.yIndicatorBuilder(yAxis.values[index])
                    val textLayoutResult = axisProperties.textMeasurer.measure(str)
                    val textHeightInDp = textLayoutResult.size.height
                    drawText(
                        textMeasurer = axisProperties.textMeasurer,
                        text = str,
                        style = axisProperties.textStyle,
                        topLeft = Offset(0f, itm.first.y - textHeightInDp / 2))
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
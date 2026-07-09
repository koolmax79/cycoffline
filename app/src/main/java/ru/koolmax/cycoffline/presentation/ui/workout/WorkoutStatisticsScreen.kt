package ru.koolmax.cycoffline.presentation.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.PopupProperties
import ir.ehsannarmani.compose_charts.models.StrokeStyle
import ru.koolmax.cycoffline.R
import ru.koolmax.cycoffline.data.media.FitListType
import ru.koolmax.cycoffline.data.media.XMeasurement
import ru.koolmax.cycoffline.presentation.getText
import ru.koolmax.cycoffline.presentation.ui.ColorUtil
import ru.koolmax.cycoffline.presentation.ui.lib.ChartData
import ru.koolmax.cycoffline.presentation.ui.lib.LineChart
import ru.koolmax.cycoffline.presentation.ui.lib.MeasurementText
import ru.koolmax.cycoffline.ui.theme.LightCustomColorsPalette
import ru.koolmax.cycoffline.ui.theme.LocalCustomColorsPalette
import ru.koolmax.cycoffline.ui.theme.LocalSpacing

@Composable
fun WorkoutStatisticsScreen(viewModel: WorkoutViewModel) {
    LaunchedEffect(Unit) {
        viewModel.getTimeByMonitoring()
    }
    val timeByMonitoring by remember { viewModel.timeByMonitoring }.collectAsState()

    Column(Modifier.verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        timeByMonitoring.forEach {
            Chart(Modifier.fillMaxWidth().padding(top = LocalSpacing.current.space25, bottom = LocalSpacing.current.space25), it.first, it.second, viewModel)
        }
    }
}

@Composable
fun Chart(modifier: Modifier = Modifier, type: FitListType, chartData: ChartData, viewModel: WorkoutViewModel) {
    if(chartData.yValues.isEmpty()) return
    val title = when (type) {
        FitListType.ALTITUDE -> stringResource(R.string.altitude)
        FitListType.GRADE -> stringResource(R.string.grade)
        FitListType.SPEED -> stringResource(R.string.speed)
        FitListType.HEART -> stringResource(R.string.heart)
        FitListType.CADENCE -> stringResource(R.string.cadence)
        FitListType.TEMPERATURE -> stringResource(R.string.temperature)
        //FitFile.FitListType.HEART_TIME -> stringResource(R.string.heartByTime)
    }
    val lineColor = ColorUtil.getColor(type)
    val heartZoneColor = LocalCustomColorsPalette.current.heartZoneColor

    val data = remember(chartData) {
        when(type) {
            FitListType.HEART -> {
                val yValues = viewModel.separateHeartByZone(chartData)
                yValues.map {
                    Line(
                        values = it.second.map { it.toDouble() },
                        color = SolidColor(heartZoneColor.getValue(it.first.idx)),
                        drawStyle = DrawStyle.Fill,
                        curvedEdges = true
                    )
                }
            }
            else -> listOf(
                Line(
                    values = chartData.yValues.map { it.toDouble() },
                    color = SolidColor(lineColor),
                    drawStyle = DrawStyle.Fill,
                    curvedEdges = true
                ),
            )
        }
    }

    val yAxis = remember(data) {
        AxisY.create(chartData.yValues.min().toDouble(), chartData.yValues.max().toDouble())
    }

    Column(modifier = modifier.background(MaterialTheme.colorScheme.surface)) {
        Card(
            modifier = modifier.fillMaxWidth().height(270.dp)
                .border(LocalSpacing.current.space25, MaterialTheme.colorScheme.surface, RoundedCornerShape(LocalSpacing.current.space25)),
        ) {

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = LocalSpacing.current.space25).padding(horizontal = LocalSpacing.current.space100),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                Text(modifier = Modifier, text = title, style = MaterialTheme.typography.headlineSmall)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    MeasurementText(modifier = Modifier, value = chartData.sum, measurementType = XMeasurement.TIME)
                    Text(text = "общее время", style = MaterialTheme.typography.labelSmall)
                }
            }

            LineChart(
                modifier = Modifier.fillMaxSize()
                    .padding(LocalSpacing.current.space100),
                data = data,
                animationMode = AnimationMode.None,
                //animationMode = AnimationMode.Together(delayBuilder = {
                //    it * 500L
                //}),
                gridProperties = GridProperties(
                    yAxisProperties = GridProperties.AxisProperties(enabled = false),
                    xAxisProperties = GridProperties.AxisProperties(
                        lineCount = yAxis.values.size,
                        thickness = 1.dp,
                        color = SolidColor(LightCustomColorsPalette.iconColorActive),
                        style = StrokeStyle.Dashed(),
                    )
                ),
                dividerProperties = dividerProperties,
                popupProperties = PopupProperties(
                    textStyle = MaterialTheme.typography.labelSmall,
                    contentBuilder = { popup ->
                        //popup.dataIndex.toString()
                        "${getText(popup.value.toInt(), XMeasurement.TIME)} \n ${getText(chartData.getX(popup.valueIndex).toDouble(), type)}"
                    },
                    confirmDraw = { popup ->
                        if (type == FitListType.HEART) {
                            popup.value != 0.0
                        } else {
                            true
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                //indicatorProperties = HorizontalIndicatorProperties(
                //    indicators = yAxis.values,
                //    textStyle = MaterialTheme.typography.labelSmall,
                //    contentBuilder = {
                //        it.format(0)
                //    }
                //),
                labelHelperProperties = labelHelperProperties,
                labelProperties = LabelProperties(
                    enabled = true,
                    labels = listOf(getText(chartData.xMax.toDouble(), type)),
                ),
                minValue = yAxis.min,
                maxValue = yAxis.max,
                curvedEdges = false
            )
        }
    }
}

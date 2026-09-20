package ru.koolmax.cycoffline.presentation.ui.workout

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import ru.koolmax.cycoffline.data.media.FitListType
import ru.koolmax.cycoffline.data.media.XMeasurement
import ru.koolmax.cycoffline.data.media.Zone
import ru.koolmax.cycoffline.presentation.getText
import ru.koolmax.cycoffline.presentation.getTextForChart
import ru.koolmax.cycoffline.presentation.ui.ColorUtil
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.ChartData
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.lineChart.DrawStyle
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.GridProperties
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.lineChart.Line
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.lineChart.LineChart
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.AxisType
import ru.koolmax.cycoffline.presentation.ui.lib.MeasurementText
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.AxisProperties
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.ChartZone
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.PopupProperties
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.lineChart.TypeLine
import ru.koolmax.cycoffline.ui.theme.LocalCustomColorsPalette
import ru.koolmax.cycoffline.ui.theme.LocalSpacing

@Composable
fun WorkoutStatisticsScreen(viewModel: WorkoutViewModel) {
    LaunchedEffect(Unit) {
        viewModel.getTimeByMonitoring()
    }
    val timeByMonitoring by remember { viewModel.timeByMonitoring }.collectAsState()
    val heartZone by remember { viewModel.heartZone }.collectAsState()

    Column(Modifier.verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        timeByMonitoring.forEach {
            Chart(Modifier
                .fillMaxWidth()
                .padding(top = LocalSpacing.current.space25, bottom = LocalSpacing.current.space25), it.first, it.second, heartZone)
        }
    }
}

@Composable
fun Chart(modifier: Modifier = Modifier, type: FitListType, chartData: ChartData, heartZone: List<Zone>) {
    //if(chartData.yValues.isEmpty()) return

    val chartInfo = CharItem.list.getValue(type)

    val lineColor = ColorUtil.getColor(type)
    val heartZoneColor = LocalCustomColorsPalette.current.heartZoneColor

    val data = remember(chartData) {
        Line(
            values = chartData.yValues.map { it.toFloat() },
            color = SolidColor(lineColor),
            fillColor = lineColor,
            type = TypeLine.Step,
            drawStyle = DrawStyle.Fill
        )
    }

    Column(modifier = modifier.background(MaterialTheme.colorScheme.surface)) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(270.dp)
                .border(LocalSpacing.current.space25, MaterialTheme.colorScheme.surface, RoundedCornerShape(LocalSpacing.current.space25)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = LocalSpacing.current.space25)
                    .padding(horizontal = LocalSpacing.current.space100),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(chartInfo.icon),
                        contentDescription = null
                    )
                    Text(modifier = Modifier, text = stringResource(chartInfo.name), style = MaterialTheme.typography.headlineSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    MeasurementText(modifier = Modifier, value = chartData.sum, measurementType = XMeasurement.TIME)
                    Text(text = "общее время", style = MaterialTheme.typography.labelSmall)
                }
            }

            val xZoneList = if(type == FitListType.HEART) heartZone.map { ChartZone(it.begin.toFloat(), it.end.toFloat() + 1, heartZoneColor[it.zoneInfo.idx]!!, 0.4f) } else listOf()

            LineChart(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(LocalSpacing.current.space100),
                data = data,
                xData = chartData.xValues,
                xZoneList = xZoneList,
                axisProperties = AxisProperties(
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                    textMeasurer = rememberTextMeasurer(),
                    xType = AxisType.INT,
                    yType = AxisType.TIME,
                    xIndicatorBuilder = {
                        getTextForChart(it.toFloat(), type)
                    },
                    yIndicatorBuilder = {
                        getTextForChart(it.toInt(), XMeasurement.TIME)
                    }),
                popupProperties = PopupProperties(
                    backgroundColor = MaterialTheme.colorScheme.inverseOnSurface,
                    lineColor =  lineColor,
                    xIndicatorBuilder = {
                        getText(it.toFloat(), type)
                    }, yIndicatorBuilder = {
                        getText(it.toInt(), XMeasurement.TIME)
                    }
                ),
                gridProperties = GridProperties(color = MaterialTheme.colorScheme.onSurface)
            )
        }
    }
}

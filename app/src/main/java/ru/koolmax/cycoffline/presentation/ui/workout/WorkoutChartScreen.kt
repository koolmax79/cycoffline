package ru.koolmax.cycoffline.presentation.ui.workout

import android.content.res.Configuration
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.koolmax.cycoffline.data.media.FitListType
import ru.koolmax.cycoffline.data.media.MonitoringChart
import ru.koolmax.cycoffline.data.media.MonitoringData
import ru.koolmax.cycoffline.data.media.XMeasurement
import ru.koolmax.cycoffline.data.media.XPause
import ru.koolmax.cycoffline.data.media.Zone
import ru.koolmax.cycoffline.presentation.getText
import ru.koolmax.cycoffline.presentation.getTextForChart
import ru.koolmax.cycoffline.presentation.ui.ColorUtil
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.lineChart.DrawStyle
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.GridProperties
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.lineChart.Line
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.lineChart.LineChart
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.AxisType
import ru.koolmax.cycoffline.presentation.ui.lib.MeasurementText
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.AxisProperties
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.ChartZone
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.PopupProperties
import ru.koolmax.cycoffline.ui.theme.LocalCustomColorsPalette
import ru.koolmax.cycoffline.ui.theme.LocalSpacing

@Composable
fun WorkoutChartScreen(viewModel: WorkoutViewModel) {
    val monitoringData by viewModel.monitoringData.collectAsState()
    val xMeasurementMode by viewModel.xMeasurementMode.collectAsState()
    val xPauseMode by viewModel.xPauseMode.collectAsState()
    val heartZone by remember { viewModel.heartZone }.collectAsState()

    when(LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> {
            WorkoutChartScreenPortrait(viewModel, monitoringData, xMeasurementMode, xPauseMode, heartZone)
        }
        Configuration.ORIENTATION_LANDSCAPE -> {
            WorkoutChartScreenLandscape(viewModel, monitoringData, xMeasurementMode, xPauseMode, heartZone)
        }
    }
}

@Composable
fun WorkoutChartScreenPortrait(viewModel: WorkoutViewModel, monitoringData: MonitoringData, xMeasurementMode: XMeasurement, xPauseMode: XPause, heartZone: List<Zone>) {
    Column() {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = LocalSpacing.current.space100),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically) {
            SingleChoiceSegmentedButton(modifier = Modifier, xMeasurementMode, onClick = {
                viewModel.setMode(it, xPauseMode)
            })
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Switch(
                    checked = xPauseMode == XPause.SHOW,
                    enabled = xMeasurementMode == XMeasurement.TIME,
                    onCheckedChange = {
                        viewModel.setMode(xMeasurementMode, if (xPauseMode == XPause.SHOW) XPause.HIDE else XPause.SHOW)
                    }
                )
                Text(text = "отображать паузы", style = MaterialTheme.typography.labelSmall)
            }
        }
        Column(Modifier.verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            monitoringData.chartData.forEach {
                Chart(Modifier.fillMaxWidth().padding(top = LocalSpacing.current.space25, bottom = LocalSpacing.current.space25),
                    it.key, xMeasurementMode, it.value, monitoringData.xValues, heartZone)
            }
        }
    }
}

@Composable
fun WorkoutChartScreenLandscape(viewModel: WorkoutViewModel, monitoringData: MonitoringData, xMeasurementMode: XMeasurement, xPauseMode: XPause, heartZone: List<Zone>) {
    Row(modifier = Modifier.fillMaxSize().padding(vertical = LocalSpacing.current.space100)) {
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                monitoringData.chartData.forEach {
                    Chart(
                        modifier = Modifier.fillMaxSize().padding(top = LocalSpacing.current.space25, bottom = LocalSpacing.current.space25),
                        it.key, xMeasurementMode, it.value, monitoringData.xValues, heartZone
                    )
                }
        }
        Column(
            modifier = Modifier.padding(vertical = LocalSpacing.current.space100),
            horizontalAlignment = Alignment.End
        ) {
            SingleChoiceSegmentedButton(modifier = Modifier, xMeasurementMode, onClick = {
                viewModel.setMode(it, xPauseMode)
            })
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Switch(
                    checked = xPauseMode == XPause.SHOW,
                    enabled = xMeasurementMode == XMeasurement.TIME,
                    onCheckedChange = {
                        viewModel.setMode(xMeasurementMode, if (xPauseMode == XPause.SHOW) XPause.HIDE else XPause.SHOW)
                    }
                )
                Text(text = "отображать паузы", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun Chart(modifier: Modifier = Modifier, type: FitListType, xMeasurement: XMeasurement, fitRecords: MonitoringChart, xValues: List<Int>, heartZone: List<Zone>) {

    val chartInfo = CharItem.list.getValue(type)
    val lineColor = ColorUtil.getColor(type)
    val heartZoneColor = LocalCustomColorsPalette.current.heartZoneColor
    val data = remember(fitRecords.yValues) {
        Line(
            values = fitRecords.yValues,
            color = SolidColor(lineColor),
            //fillColor = lineColor,
            drawStyle = DrawStyle.Stroke(1.dp)
        )
    }

    Column(modifier = modifier.background(MaterialTheme.colorScheme.surface)) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(270.dp)//height(LocalSpacing.current.chartHeight)
                .border(LocalSpacing.current.space25, MaterialTheme.colorScheme.surface, RoundedCornerShape(LocalSpacing.current.space25)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(vertical = LocalSpacing.current.space25).padding(horizontal = LocalSpacing.current.space100),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(chartInfo.icon),
                        contentDescription = null
                    )
                    Text(modifier = Modifier, text = stringResource(chartInfo.name), style = MaterialTheme.typography.headlineSmall)
                }
                fitRecords.info.forEach {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        MeasurementText(modifier = Modifier, value = it.second, measurementType = type)
                        Text(text = it.first, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            val yZoneList = if(type == FitListType.HEART) heartZone.map { ChartZone(it.begin.toFloat(), it.end.toFloat() + 1, heartZoneColor[it.zoneInfo.idx]!!, 0.4f) } else listOf()
            LineChart(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                        .padding(LocalSpacing.current.space100),
                data = data,
                xData = xValues,
                yZoneList = yZoneList,
                axisProperties = AxisProperties(
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                    textMeasurer = rememberTextMeasurer(),
                    xType = when (xMeasurement) {
                        XMeasurement.DISTANCE -> AxisType.INT
                        XMeasurement.TIME -> AxisType.TIME
                    },
                    xIndicatorBuilder = {
                        getTextForChart(it, xMeasurement)
                    },
                    yIndicatorBuilder = {
                        getTextForChart(it, type)
                    }),
                popupProperties = PopupProperties(
                    backgroundColor = MaterialTheme.colorScheme.inverseOnSurface,
                    lineColor = lineColor,
                    xIndicatorBuilder = {
                        getText(it, xMeasurement)
                    }, yIndicatorBuilder = {
                        getText(it, type)
                    }
                ),
                gridProperties = GridProperties(color = MaterialTheme.colorScheme.onSurface)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleChoiceSegmentedButton(modifier: Modifier = Modifier, measurement: XMeasurement, onClick: (XMeasurement) -> Unit) {
    val options = listOf(Pair(XMeasurement.DISTANCE, "расстояние"), Pair(XMeasurement.TIME, "время"))
    SingleChoiceSegmentedButtonRow() {
        options.forEachIndexed { index, itm ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                onClick = { onClick(itm.first) },
                selected = itm.first == measurement,
                label = { Text(itm.second) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Greeting() {
/*
    val data =
        listOf(
            ru.koolmax.cycoffline.presentation.ui.lib.LineChart.Line(
                values = listOf(1.0, 2.5, 3.0, 4.0, 2.0, 2.0, 1.3, 0.0),
                color = SolidColor(Color.Red),
            ),
        )
    //Text("dsfdsf")
    LineChart(modifier = Modifier.fillMaxWidth().height(400.dp), data = data)
*/
}

/*
val labelHelperProperties: LabelHelperProperties @Composable get()  = LabelHelperProperties(textStyle = TextStyle(fontSize = 12.sp, color = Color.White))
val labelProperties: LabelProperties
    @Composable get()  = LabelProperties(
        enabled = true,
        textStyle = TextStyle(fontSize = 12.sp, color = Color.White)
)

val gridProperties: GridProperties
    @Composable get() = GridProperties(
    yAxisProperties = GridProperties.AxisProperties(enabled = false),
    xAxisProperties = GridProperties.AxisProperties(
        thickness = 1.dp,
        color = SolidColor(MaterialTheme.colorScheme.onSurface),
        style = StrokeStyle.Normal,
    ),
)

val dividerProperties = DividerProperties(
    xAxisProperties = LineProperties(
        thickness = .2.dp,
        color = SolidColor(Color.Gray.copy(alpha = .5f)),
        style = StrokeStyle.Dashed(intervals = floatArrayOf(15f, 15f), phase = 10f),
    ),
    yAxisProperties = LineProperties(
        thickness = .2.dp,
        color = SolidColor(Color.Gray.copy(alpha = .5f)),
        style = StrokeStyle.Dashed(intervals = floatArrayOf(15f, 15f), phase = 10f),
    )
)*/

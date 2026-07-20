package ru.koolmax.cycoffline.presentation.ui.statistics

import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.Bars.*
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ru.koolmax.cycoffline.R
import ru.koolmax.cycoffline.data.db.FitSessionItem
import ru.koolmax.cycoffline.data.FitStatisticItem
import ru.koolmax.cycoffline.presentation.MeasureUtil
import ru.koolmax.cycoffline.presentation.pairToString
import ru.koolmax.cycoffline.presentation.ui.ColorUtil
import ru.koolmax.cycoffline.presentation.ui.calendar.InfoRow
import ru.koolmax.cycoffline.presentation.ui.getValueFormatter
import ru.koolmax.cycoffline.presentation.ui.lib.HorizontalPicker
import ru.koolmax.cycoffline.presentation.ui.lib.PickerValueFormatter
import ru.koolmax.cycoffline.presentation.ui.lib.rememberPickerState
import ru.koolmax.cycoffline.ui.theme.CustomColorsPalette
import ru.koolmax.cycoffline.ui.theme.LocalCustomColorsPalette
import ru.koolmax.cycoffline.ui.theme.LocalSpacing
import java.time.Month

@Composable
fun rememberChartTypeState(type: ChartType) = remember { ChartTypeState(type) }

class ChartTypeState(type: ChartType) {
    var chartType by mutableStateOf(type)
}

@Composable
fun StatisticsScreen(navController: NavController, viewModel: StatisticsViewModel = hiltViewModel()) {
    val years by remember {  viewModel.years }.collectAsState()
    val yearState = remember { mutableIntStateOf(0) }
    val months by remember { viewModel.months }.collectAsState()
    val monthState = remember { mutableIntStateOf(0) }
    val sessionStatistic by remember { viewModel.sessionStatistic }.collectAsState()
    val fitSessionList by remember { viewModel.fitSessionList }.collectAsState()
    //val entryList by remember { viewModel.entryList }.collectAsState()
    val chartTypeState = remember { mutableStateOf(setOf<ChartType>()) }

    LaunchedEffect(years) {
        if(years.isEmpty()) {
            viewModel.getRangeYear()
        }
        yearState.intValue = years.lastIndex
    }

    LaunchedEffect(yearState.intValue) {
        if(yearState.intValue!=0)
            viewModel.getRangeMonth(yearState.intValue)
    }

    LaunchedEffect(yearState.intValue, monthState.intValue) {
        Log.i("cycoffline1","LaunchedEffect ${yearState.intValue} ${monthState.intValue}")
        if(yearState.intValue !=0) {
            viewModel.getStatistic(yearState.intValue, monthState.intValue)
        }
    }

    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally) {
        FitStatistics(sessionStatistic)

        DataChart(modifier = Modifier.fillMaxWidth().padding(top = LocalSpacing.current.space25, bottom = LocalSpacing.current.space25),
            fitSessionList, chartTypeState.value)

        if(yearState.intValue != -1) {
            HorizontalPicker(
                modifier = Modifier.fillMaxWidth(),
                items = years,
                selectedState = yearState,
                textStyle = MaterialTheme.typography.titleLarge,
            )

            HorizontalPicker(
                modifier = Modifier.fillMaxWidth(),
                items = months,
                selectedState = monthState,
                textStyle = MaterialTheme.typography.titleMedium,
                //selectedTextStyle = TextStyle(fontSize = MaterialTheme.typography.titleLarge.fontSize * 1.2),
                formatter = PickerValueFormatter { value ->
                    value.toString()
                    when(value) {
                        0 -> "все"
                        else -> MeasureUtil.getMonth(value as Int)
                    }
                },
            )
        }
        MeasurementSelector(Modifier.fillMaxWidth(), chartTypeState)
    }
}

@Composable
fun FitStatistics(statistic: FitStatisticItem) {
    Column(modifier = Modifier.fillMaxWidth().padding( LocalSpacing.current.space100),
        horizontalAlignment = Alignment.CenterHorizontally) {
        InfoRow(stringResource(R.string.number_trips), statistic.count.toString())
        InfoRow(
            stringResource(R.string.distance), MeasureUtil.getDistance(statistic.totalDistance).toList()
                .joinToString(" ")
        )
        InfoRow(
            stringResource(R.string.ascent), MeasureUtil.getAscent(statistic.totalAscent).toList()
                .joinToString(" ")
        )
        InfoRow(
            "падение", MeasureUtil.getAscent(statistic.totalDescent).toList()
                .joinToString(" ")
        )
        InfoRow(
            stringResource(R.string.totalMovingTime), MeasureUtil.getDuration(statistic.totalMovingTime).toList()
                .joinToString(" ")
        )
    }
}

@Composable
fun DataChart(modifier: Modifier = Modifier, fitSessionList: List<FitSessionItem>, chartType: Set<ChartType>) {
    val сhartTypeColor = LocalCustomColorsPalette.current.сhartTypeColor
    val data = remember(fitSessionList, chartType) {
        fitSessionList.map {
            Bars(
                label = MeasureUtil.getDateTime(it.startTime),
                values = chartType.map { type ->
                    val color = сhartTypeColor[type] ?: Color.White
                    when (type) {
                        ChartType.DISTANCE -> Data(label = type.text, value = it.totalDistance?.toDouble() ?: 0.0, color = SolidColor(color))
                        ChartType.AVG_HEART_RATE -> Data(
                            label = type.text,
                            value = it.avgHeartRate?.toDouble() ?: 0.0,
                            color = SolidColor(color)
                        )

                        ChartType.AVG_SPEED -> Data(label = type.text, value = it.avgSpeed ?: 0.0, color = SolidColor(color))
                        ChartType.ASCENT -> Data(label = type.text, value = it.totalAscent?.toDouble() ?: 0.0, color = SolidColor(color))
                        ChartType.MOVING_TIME -> Data(
                            label = type.text,
                            value = it.totalMovingTime?.toDouble() ?: 0.0,
                            color = SolidColor(color)
                        )

                        ChartType.MAX_HEART_RATE -> Data(
                            label = type.text,
                            value = it.maxHeartRate?.toDouble() ?: 0.0,
                            color = SolidColor(color)
                        )
                    }
                }
            )
        }
    }

    Box(modifier = modifier.fillMaxWidth().height(LocalSpacing.current.chartHeight)) {
        if(fitSessionList.isNotEmpty() && chartType.isNotEmpty()) {
            Card(modifier = modifier.fillMaxWidth()
                 .border(LocalSpacing.current.space25, MaterialTheme.colorScheme.surface, RoundedCornerShape(LocalSpacing.current.space25)),
                ) {
                if (fitSessionList.isNotEmpty()) {
                    ColumnChart(
                        modifier = Modifier.fillMaxSize()
                            .padding(LocalSpacing.current.space100),
                        data = data,
                        labelProperties = LabelProperties(enabled = false),
                        barProperties = BarProperties(
                            thickness = 2.dp,
                            //radius = Bars.Data.Radius.Rectangle(topRight = 6.dp, topLeft = 6.dp),
                            spacing = 3.dp,
                            //strokeWidth = 20.dp
                        ),
                        animationMode = AnimationMode.None,
                        //animationSpec = spring(
                        //    dampingRatio = Spring.DampingRatioMediumBouncy,
                        //    stiffness = Spring.StiffnessLow
                        //),
                    )
                }
            }
        }
    }
}

@Composable
fun MeasurementSelector(modifier: Modifier = Modifier, chartTypeState: MutableState<Set<ChartType>>) {
    Column(modifier = modifier) {
        ChartType.entries.forEach { type ->
            Row(Modifier.fillMaxWidth()
                    .padding(horizontal = LocalSpacing.current.space25),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = chartTypeState.value.contains(type),
                    onCheckedChange = {
                        if(chartTypeState.value.contains(type))
                            chartTypeState.value = chartTypeState.value.toMutableSet().also { it.remove(type) }
                        else
                            chartTypeState.value = chartTypeState.value.toMutableSet().also { it.add(type) }
                    }
                )
                Text(
                    text = type.text,
                    style = MaterialTheme.typography.bodyLarge,
                    //modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

/*@Composable
fun StatisticChip(
    state: Boolean,
    text: String,
    onChangeState: ((selected: Boolean) -> Unit)? = null,
) {
    var selected by remember { mutableStateOf(state) }

    FilterChip(
        onClick = {
            selected = !selected
            onChangeState?.invoke(selected) },
        label = {
            Text(text)
        },
        selected = selected,
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = "Done icon",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else {
            null
        },
    )
}*/

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val list = listOf(1, 2, 3, 4, 5)
    val listStr = listOf("ojjne", "ts sda", "fdssd", "fdsfa", "fdsfdsfsd")
    val state = remember { mutableIntStateOf(1) }
    Column() {
        HorizontalPicker(
            modifier = Modifier.fillMaxWidth(),
            items = list,
            selectedState = state,
            textStyle = MaterialTheme.typography.titleLarge,
        )
        HorizontalPicker(
            modifier = Modifier.fillMaxWidth(),
            items = list,
            selectedState = state,
            formatter = PickerValueFormatter { value -> listStr[value as Int] },
            textStyle = MaterialTheme.typography.titleLarge,
        )
    }
}
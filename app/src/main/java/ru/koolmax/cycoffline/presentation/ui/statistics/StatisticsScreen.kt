package ru.koolmax.cycoffline.presentation.ui.statistics

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dagger.Module
import ru.koolmax.cycoffline.data.db.FitSessionItem
import ru.koolmax.cycoffline.navigation.Screen
import ru.koolmax.cycoffline.presentation.MeasureUtil
import ru.koolmax.cycoffline.presentation.getText
import ru.koolmax.cycoffline.presentation.getTextForChart
import ru.koolmax.cycoffline.presentation.ui.calendar.InfoRow
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.barChart.Bars
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.barChart.BarChart
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.GridProperties
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.AxisType
import ru.koolmax.cycoffline.presentation.ui.lib.HorizontalPicker
import ru.koolmax.cycoffline.presentation.ui.lib.PickerValueFormatter
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.AxisProperties
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.PopupProperties
import ru.koolmax.cycoffline.ui.theme.LocalCustomColorsPalette
import ru.koolmax.cycoffline.ui.theme.LocalSpacing

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
    val chartTypeState = remember { mutableStateOf(ChartType.DISTANCE) }

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
        //Log.i("cycoffline1","LaunchedEffect ${yearState.intValue} ${monthState.intValue}")
        if(yearState.intValue !=0) {
            viewModel.getStatistic(yearState.intValue, monthState.intValue)
        }
    }

    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally) {
        InfoRow(modifier = Modifier.fillMaxWidth(), statistic = sessionStatistic)

        DataChart(modifier = Modifier.fillMaxWidth().padding(top = LocalSpacing.current.space25, bottom = LocalSpacing.current.space25),
            fitSessionList, chartTypeState.value, onClick = {
                navController.navigate(
                    Screen.Workout.route.replace(
                        "{fit}",
                        it.fileName
                    )
                )
            })

        if(yearState.intValue != -1) {
            HorizontalPicker(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                items = years,
                selectedState = yearState,
                textStyle = MaterialTheme.typography.titleLarge,
            )

            HorizontalPicker(
                modifier = Modifier.fillMaxWidth().height(60.dp),
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
fun DataChart(modifier: Modifier = Modifier, fitSessionList: List<FitSessionItem>, chartType: ChartType, onClick: (FitSessionItem) -> Unit ) {
    val color = LocalCustomColorsPalette.current.getChartTypeColor(chartType)

    val xData = remember(fitSessionList) {
        fitSessionList.mapIndexed { index, item -> index }
    }
    if(chartType == ChartType.NONE) return

    val data = remember(fitSessionList, chartType) {
        Bars(
            label = "",
            color = SolidColor(color),
            values =
                when (chartType) {
                    ChartType.DISTANCE -> fitSessionList.map { it.totalDistance?.toFloat() ?: 0f }
                    ChartType.AVG_HEART_RATE -> fitSessionList.map { it.avgHeartRate?.toFloat() ?: 0f }
                    ChartType.AVG_SPEED -> fitSessionList.map { it.avgSpeed?.toFloat() ?: 0f }
                    ChartType.ASCENT -> fitSessionList.map { it.totalAscent?.toFloat() ?: 0f }
                    ChartType.MOVING_TIME -> fitSessionList.map { it.totalMovingTime?.toFloat() ?: 0f }
                    ChartType.MAX_HEART_RATE -> fitSessionList.map { it.maxHeartRate?.toFloat() ?: 0f }
                    ChartType.NONE -> throw Exception("")
                },
            objects = fitSessionList.map { it }
        )
    }

    Box(modifier = modifier.fillMaxWidth().height(LocalSpacing.current.chartHeight)) {
        if (fitSessionList.isNotEmpty()) {
            Card(
                modifier = modifier.fillMaxWidth()
                    .border(LocalSpacing.current.space25, MaterialTheme.colorScheme.surface, RoundedCornerShape(LocalSpacing.current.space25)),
            ) {
                BarChart(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)
                        .padding(LocalSpacing.current.space100),
                    data = data,
                    xData = xData,

                    axisProperties = AxisProperties(
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                        textMeasurer = rememberTextMeasurer(),
                        xType = AxisType.INT,
                        yType = chartType.axisType,
                        xIndicatorBuilder = {
                            ""//getTextForChart(it, XMeasurement.DISTANCE)
                        },
                        yIndicatorBuilder = {
                            getTextForChart(it, chartType)
                        }),
                    gridProperties = GridProperties(color = MaterialTheme.colorScheme.onPrimaryContainer),
                    popupProperties = PopupProperties(
                        lineColor = color,
                        backgroundColor = MaterialTheme.colorScheme.inverseOnSurface,
                        xIndicatorBuilder = {
                            MeasureUtil.getDateTime(data.objects[it].startTime)
                        }, yIndicatorBuilder = {
                            getTextForChart(it, chartType, true)
                        },
                        dot = false
                    ),
                    onClick = {
                        onClick(it)
                    }
                )
            }
        }
    }
}

@Composable
fun MeasurementSelector(modifier: Modifier = Modifier, chartTypeState: MutableState<ChartType>) {
    Column(modifier = modifier.selectableGroup()) {
        ChartType.entries.forEach { type ->
            if(type != ChartType.NONE) {
                Row(
                    Modifier.padding(LocalSpacing.current.space100).fillMaxWidth()
                        .selectable(
                            selected = (type == chartTypeState.value),
                            onClick = { chartTypeState.value = type },
                            role = Role.RadioButton
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (type == chartTypeState.value),
                        onClick = null
                    )
                    Text(
                        text = type.text,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
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
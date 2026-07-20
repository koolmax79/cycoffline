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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.ehsannarmani.compose_charts.extensions.format
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DividerProperties
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.LineProperties
import ir.ehsannarmani.compose_charts.models.PopupProperties
import ir.ehsannarmani.compose_charts.models.StrokeStyle
import ru.koolmax.cycoffline.R
import ru.koolmax.cycoffline.data.media.FitListType
import ru.koolmax.cycoffline.data.media.MonitoringChart
import ru.koolmax.cycoffline.data.media.XMeasurement
import ru.koolmax.cycoffline.data.media.XPause
import ru.koolmax.cycoffline.presentation.getText
import ru.koolmax.cycoffline.presentation.ui.ColorUtil
import ru.koolmax.cycoffline.presentation.ui.lib.LineChart
import ru.koolmax.cycoffline.presentation.ui.lib.MeasurementText
import ru.koolmax.cycoffline.ui.theme.LightCustomColorsPalette
import ru.koolmax.cycoffline.ui.theme.LocalSpacing
import kotlin.math.pow

data class AxisY(val min: Double, val max: Double, val values: List<Double>) {
    companion object {
        fun create(min: Double, max: Double, maxCount: Int = 10): AxisY {
            val intervalVal = max - min
            val step = roundStep(intervalVal / maxCount);

            val label = mutableSetOf<Double>()
            var l = 0.0
            while(l < max + step) {
                if(l > min - step)
                    label.add(l)
                l += step
            }

            l = 0.0
            while(l > min - step) {
                if(l < max + step)
                    label.add(l)
                l -= step
            }
            return AxisY(label.min(), label.max(), label.toList().sortedDescending())
        }

        private fun roundStep(step: Double): Double {
            val roundTemplate = arrayOf(10, 2, 5, 5, 5, 10, 10, 10, 10, 10)
            val n = getBaseMantissa(step)
            var str = n.first.toString()
            val r = roundTemplate[str[0].toString().toInt()]
            str = str.removeRange(0, 1)
            str = r.toString() + str
            str = replaceAllExceptFirst(str)
            return str.toDouble() * 10.0.pow(n.second)
        }

        private fun replaceAllExceptFirst(input: String): String {
            if (input.length <= 1) return input
            val firstChar = input[0]
            val rest = input.substring(1)
            val replacedRest = rest.map { '0' }.joinToString("")
            return firstChar + replacedRest
        }

        private fun getBaseMantissa(value: Double): Pair<Int, Int> {
            val str = String.format("%.6e", value)
            val re = Regex("(^[+-]?[0-9]*[.,]?[0-9]+)e([+-]?[0-9]+)")
            val match = re.find(str)

            if(match != null) {
                val (b, m) = match.destructured
                return Pair((b.replace(',', '.').toDouble() * 10.0.pow(6)  / 10.0).toInt(), m.toInt() - 5)
            }
            return Pair(0,0)
        }
    }
}

@Composable
fun WorkoutChartScreen(viewModel: WorkoutViewModel) {
    val monitoringData by viewModel.monitoringData.collectAsState()
    val xMeasurementMode by viewModel.xMeasurementMode.collectAsState()
    val xPauseMode by viewModel.xPauseMode.collectAsState()

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
                    it.key, xMeasurementMode, it.value, monitoringData.xValues)
            }
        }
    }
}

@Composable
fun Chart(modifier: Modifier = Modifier, type: FitListType, xMeasurement: XMeasurement, fitRecords: MonitoringChart, xValues: List<Int>) {
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
    val data = remember(fitRecords.yValues) {
        listOf(
            Line(
                values = fitRecords.yValues,
                color = SolidColor(lineColor),
                drawStyle = DrawStyle.Fill,
                curvedEdges = true
            ),
        )
    }
    val yAxis = remember(fitRecords.yValues) {
        AxisY.create(fitRecords.min, fitRecords.max)
    }

    Column(modifier = modifier.background(MaterialTheme.colorScheme.surface)) {
        Card(
            modifier = modifier.fillMaxWidth().height(LocalSpacing.current.chartHeight)
                .border(LocalSpacing.current.space25, MaterialTheme.colorScheme.surface, RoundedCornerShape(LocalSpacing.current.space25)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = LocalSpacing.current.space25).padding(horizontal = LocalSpacing.current.space100),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                Text(modifier = Modifier, text = title, style = MaterialTheme.typography.headlineSmall)
                fitRecords.info.forEach {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        MeasurementText(modifier = Modifier, value = it.second, measurementType = type)
                        Text(text = it.first, style = MaterialTheme.typography.labelSmall)
                    }
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
                        "${getText(popup.value, type)} \n ${getText(xValues[popup.valueIndex], xMeasurement)}"

                    },
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                indicatorProperties = HorizontalIndicatorProperties(
                    indicators = yAxis.values,
                    textStyle = MaterialTheme.typography.labelSmall,
                    contentBuilder = {
                        it.format(0)
                    }
                ),
                labelHelperProperties = labelHelperProperties,
                labelProperties = LabelProperties(
                    enabled = true,
                    labels = listOf(getText(xValues.last(), xMeasurement, false)),
                ),
                minValue = yAxis.min,
                maxValue = yAxis.max,
                curvedEdges = false
            )
        }
    }
}

@Composable
fun SingleChoiceSegmentedButton(modifier: Modifier = Modifier, measurement: XMeasurement, onClick: (XMeasurement) -> Unit) {
    val options = listOf(Pair(XMeasurement.DISTANCE, "расстояние"), Pair(XMeasurement.TIME, "время"))

    SingleChoiceSegmentedButtonRow {
        options.forEachIndexed { index, itm ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                onClick = { onClick(itm.first) },
                selected = itm.first == measurement,
                label = { Text(itm.second) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Greeting() {
    Column(Modifier.verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        //LineSample9()
        //LineSample2()
        //LineSample3()
        //LineSample4()
        //LineSample5()
        //LineSample6()
        //LineSample7()
        //LineSample8()
        //LineSample()
    }
}

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
)

//@Composable
//fun LineSample(modifier: Modifier=Modifier) {
//    val popupProperties = PopupProperties(
//        textStyle = TextStyle(
//            fontSize = 11.sp,
//            color = Color.White,
//        ),
//        contentBuilder = { popup ->
//            popup.value.format(1) + " Million, lineIndex: ${popup.dataIndex}, valueIndex: ${popup.valueIndex}"
//        },
//        containerColor = Color(0xff414141)
//    )
//    val data = remember {
//        listOf(
//            Line(
//                label = "Windows",
//                values = listOf(
//                    75.0,
//                    5.0,
//                    70.0,
//                    85.0,
//                    0.0
//                ),
//                color = SolidColor(Color(0xFF2B8130)),
//                firstGradientFillColor = Color(0xFF66BB6A).copy(alpha = .4f),
//                secondGradientFillColor = Color.Transparent,
//                strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
//                gradientAnimationDelay = 1000,
//                drawStyle = DrawStyle.Stroke(.5.dp),
//                curvedEdges = true,
//                popupProperties = popupProperties
//            ),
//            Line(
//                label = "Linux",
//                values = listOf(
//                    1.0,
//                    19.0,
//                    22.0,
//                    0.0,
//                    5.0
//                ),
//                color = SolidColor(Color(0xFFDA860C)),
//                firstGradientFillColor = Color(0xFFFFA726).copy(alpha = .4f),
//                secondGradientFillColor = Color.Transparent,
//                strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
//                gradientAnimationDelay = 1000,
//                drawStyle = DrawStyle.Stroke(.5.dp),
//                popupProperties = popupProperties.copy(enabled = false)
//            ),
//            Line(
//                label = "MacOS",
//                values = listOf(
//                    4.0,
//                    40.0,
//                    58.0,
//                    38.0,
//                    22.0
//                ),
//                color = SolidColor(Color(0xFF0F73C4)),
//                firstGradientFillColor = Color(0xFF42A5F5).copy(alpha = .4f),
//                secondGradientFillColor = Color.Transparent,
//                strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
//                gradientAnimationDelay = 1000,
//                drawStyle = DrawStyle.Stroke(.5.dp),
//                curvedEdges = true,
//                popupProperties = popupProperties
//            ),
//        )
//    }
//    ChartParent(modifier=Modifier) {
//        Box(modifier = Modifier.fillMaxSize().padding(vertical = 12.dp)){
//            LineChart(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(horizontal = 22.dp),
//                data = data,
//                animationMode = AnimationMode.Together(delayBuilder = {
//                    it * 500L
//                }),
//                gridProperties = gridProperties,
//                dividerProperties = dividerProperties,
//                indicatorProperties = HorizontalIndicatorProperties(
//                    textStyle = TextStyle(
//                        fontSize = 11.sp,
//                        color = Color.White,
//                    ),
//                    contentBuilder = {
//                        it.format(1) + " M"
//                    },
//                ),
//                labelHelperProperties = LabelHelperProperties(enabled = false),
//                curvedEdges = false
//            )
//        }
//    }
//}

//@Composable
//fun LineSample2(modifier: Modifier=Modifier) {
//    val data = remember {
//        listOf(
//            Line(
//                label = "Temperature",
//                values = listOf(
//                    28.0,
//                    41.0,
//                    -15.0,
//                    27.0,
//                    54.0
//                ),
//                color = SolidColor(Color(0xFF23af92)),
//                firstGradientFillColor = Color(0xFF2BC0A1).copy(alpha = .5f),
//                secondGradientFillColor = Color.Transparent,
//                strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
//                gradientAnimationDelay = 1000,
//                drawStyle = DrawStyle.Stroke(),
//                curvedEdges = true,
//            ),
//        )
//    }
//    ChartParent(modifier=Modifier) {
//        Box(modifier = Modifier.fillMaxSize().padding(vertical = 12.dp)){
//            LineChart(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(horizontal = 22.dp),
//                data = data,
//                animationMode = AnimationMode.Together(delayBuilder = {
//                    it * 500L
//                }),
//                gridProperties = gridProperties.copy(
//                    yAxisProperties = GridProperties.AxisProperties(enabled = false),
//                    xAxisProperties = gridProperties.xAxisProperties.copy(
//                        thickness = .5.dp
//                    )
//                ),
//                dividerProperties = DividerProperties(
//                    yAxisProperties = LineProperties(enabled = false),
//                    xAxisProperties = LineProperties(
//                        thickness = .5.dp,
//                        color = SolidColor(Color.Gray.copy(alpha = .5f)),
//                        style = StrokeStyle.Dashed(intervals = floatArrayOf(15f,15f), phase = 10f),
//                    )
//                ),
//                popupProperties = PopupProperties(
//                    textStyle = TextStyle(
//                        fontSize = 11.sp,
//                        color = Color.White,
//                    ),
//                    contentBuilder = { popup->
//                        popup.value.format(1) + " °C"
//                    },
//                    containerColor = Color(0xff414141)
//                ),
//                zeroLineProperties = ZeroLineProperties(
//                    enabled = true,
//                    color = SolidColor(Color(0xFFAD1457)),
//                    thickness = 1.dp,
//                ),
//                indicatorProperties = HorizontalIndicatorProperties(
//                    textStyle = TextStyle(
//                        fontSize = 11.sp,
//                        color = Color.White
//                    ),
//                    contentBuilder = {
//                        it.format(1) + " °C"
//                    },
//                ),
//                labelHelperProperties = labelHelperProperties,
//                curvedEdges = false,
//                maxValue = 100.0,
//                minValue = -20.0
//            )
//        }
//    }
//}
/*
@Composable
fun LineSample3(modifier: Modifier=Modifier) {
    val data = remember {
        listOf(
            Line(
                label = "Windows",
                values = listOf(
                    88.0,
                    56.0,
                    70.0,
                    45.0,
                    26.0
                ),
                color = SolidColor(Color(0xffF7B731)),
                strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                gradientAnimationDelay = 1000,
                drawStyle = DrawStyle.Stroke(),
                curvedEdges = false,
                dotProperties = DotProperties(
                    enabled = true,
                    color = SolidColor(Color(0xffE1E2EC)),
                    strokeWidth = 1.dp,
                    strokeColor = SolidColor(Color(0xffF7B731)),
                )
            ),
            Line(
                label = "Linux",
                values = listOf(
                    30.0,
                    70.0,
                    45.0,
                    65.0,
                    17.0,
                    18.0,
                    14.0,
                    15.0
                ),
                color = SolidColor(Color(0xff0FB9B1)),
                strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                gradientAnimationDelay = 1000,
                drawStyle = DrawStyle.Stroke(),
                curvedEdges = false,
                dotProperties = DotProperties(
                    enabled = true,
                    color = SolidColor(Color(0xffE1E2EC)),
                    strokeWidth = 2.dp,
                    strokeColor = SolidColor(Color(0xff0FB9B1)),
                    confirmDraw = {
                        it.valueIndex in listOf(0,4)
                    }
                )
            ),
        )
    }
    ChartParent(modifier=Modifier) {
        Box(modifier = Modifier.fillMaxSize().padding(vertical = 12.dp)){
            LineChart(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp),
                data = data,
                animationMode = AnimationMode.Together(delayBuilder = {
                    it * 500L
                }),
                gridProperties = gridProperties,
                dividerProperties = dividerProperties,
                popupProperties = PopupProperties(
                    textStyle = TextStyle(
                        fontSize = 11.sp,
                        color = Color.White,
                    ),
                    mode = PopupProperties.Mode.PointMode(),
                    contentBuilder = { popup ->
                        popup.value.format(1) + " Million" + " - dataIdx: " + popup.dataIndex + ", valueIdx: " + popup.valueIndex
                    },
                    containerColor = Color(0xff414141)
                ),
                indicatorProperties = HorizontalIndicatorProperties(
                    textStyle = TextStyle(
                        fontSize = 11.sp,
                        color = Color.White
                    ),
                    contentBuilder = {
                        it.format(1) + " M"
                    }
                ),
                labelHelperProperties = labelHelperProperties,
                curvedEdges = false,
                labelProperties = LabelProperties(
                    enabled = true,
                    labels = listOf("Jan","Feb","Mar","Apr","May"),
                    textStyle = TextStyle(
                        fontSize = 11.sp,
                        color = Color.White
                    ),
                ),
            )
        }
    }
}
*/
/*
@Composable
fun LineSample4(modifier: Modifier=Modifier) {
    val data = remember {
        listOf(
            Line(
                label = "Windows",
                values = MutableList(5) { (0..100).random().toDouble() },
                color = SolidColor(Color(0xFF2B8130)),
                strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                gradientAnimationDelay = 1000,
                drawStyle = DrawStyle.Stroke(),
                curvedEdges = true,
            ),
            Line(
                label = "Linux",
                values = MutableList(5) { (0..100).random().toDouble() },
                color = SolidColor(Color(0xFFE65100)),
                strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                gradientAnimationDelay = 1000,
                drawStyle = DrawStyle.Stroke(),
            ),
            Line(
                label = "Android",
                values = MutableList(5) { (0..100).random().toDouble() },
                color = SolidColor(Color(0xFFB71C1C)),
                strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                gradientAnimationDelay = 1000,
                drawStyle = DrawStyle.Stroke(),
            ),
        )
    }
    ChartParent(modifier=Modifier) {
        Box(modifier = Modifier.fillMaxSize().padding(vertical = 12.dp)){
            LineChart(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp),
                data = data,
                animationMode = AnimationMode.Together(delayBuilder = {
                    it * 500L
                }),
                gridProperties = gridProperties,
                dividerProperties = dividerProperties,
                popupProperties = PopupProperties(
                    textStyle = TextStyle(
                        fontSize = 11.sp,
                        color = Color.White,
                    ),
                    contentBuilder = { popup->
                        popup.value.format(1) + " Million"
                    },
                    containerColor = Color(0xff414141)
                ),
                indicatorProperties = HorizontalIndicatorProperties(
                    textStyle = TextStyle(
                        fontSize = 11.sp,
                        color = Color.White
                    ),
                    contentBuilder = {
                        it.format(1) + " M"
                    }
                ),
                labelHelperProperties = labelHelperProperties,
                curvedEdges = true
            )
        }
    }
}
@Composable
fun LineSample5(modifier: Modifier=Modifier) {
    val data = remember {
        listOf(
            Line(
                label = "Windows",
                values = MutableList(5) { (0..100).random().toDouble() },
                color = SolidColor(Color(0xFFF7B731)),
                strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                gradientAnimationDelay = 1000,
                drawStyle = DrawStyle.Stroke(),
            ),
            Line(
                label = "Linux",
                values = MutableList(5) { (0..100).random().toDouble() },
                color = SolidColor(Color(0xFF0FB9B1)),
                strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                gradientAnimationDelay = 1000,
                drawStyle = DrawStyle.Stroke(),
            ),
        )
    }
    ChartParent(modifier=Modifier) {
        Box(modifier = Modifier.fillMaxSize().padding(vertical = 12.dp)){
            LineChart(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp),
                data = data,
                animationMode = AnimationMode.Together(delayBuilder = {
                    it * 500L
                }),
                gridProperties = gridProperties,
                dividerProperties = dividerProperties,
                popupProperties = PopupProperties(
                    textStyle = TextStyle(
                        fontSize = 11.sp,
                        color = Color.White,
                    ),
                    contentBuilder = { popup->
                        popup.value.format(1) + " Million"
                    },
                    containerColor = Color(0xff414141)
                ),
                indicatorProperties = HorizontalIndicatorProperties(
                    textStyle = TextStyle(
                        fontSize = 11.sp,
                        color = Color.White
                    ),
                    contentBuilder = {
                        it.format(1) + " M"
                    }
                ),
                labelHelperProperties = labelHelperProperties,
                curvedEdges = false,
                labelProperties = LabelProperties(
                    enabled = true,
                    labels = listOf("Jan","Feb","Mar","Apr","May"),
                    textStyle = TextStyle(
                        fontSize = 11.sp,
                        color = Color.White
                    ),
                ),
            )
        }
    }
}
@Composable
fun LineSample6(modifier: Modifier=Modifier) {
    val data = remember {
        listOf(
            Line(
                label = "Windows",
                values = listOf(
                    67.0,
                    0.0,
                    88.0,
                    90.0,
                    95.0
                ),
                color = SolidColor(Color(0xFFFB8231)),
                strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                gradientAnimationDelay = 1000,
                drawStyle = DrawStyle.Stroke(
                    width = 3.dp,
                    strokeStyle = StrokeStyle.Dashed(intervals = floatArrayOf(10f,10f), phase = 15f)
                ),
                dotProperties = DotProperties(
                    enabled = true,
                    color = SolidColor(Color(0xFFFB8231)),
                    strokeWidth = 2.dp,
                    radius = 3.dp,
                    strokeColor = SolidColor(Color(0xffffffff)),
                )
            ),
            Line(
                label = "Linux",
                values = listOf(
                    98.0,
                    67.0,
                    15.0,
                    20.0,
                    75.0
                ),
                color = SolidColor(Color(0xff23AF92)),
                strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                gradientAnimationDelay = 1000,
                drawStyle = DrawStyle.Stroke(
                    width = 3.dp,
                    strokeStyle = StrokeStyle.Dashed(intervals = floatArrayOf(10f,10f), phase = 15f)
                ),
                dotProperties = DotProperties(
                    enabled = true,
                    color = SolidColor(Color(0xff23AF92)),
                    strokeWidth = 2.dp,
                    radius = 3.dp,
                    strokeColor = SolidColor(Color(0xffffffff)),
                ),
                popupProperties = PopupProperties(enabled = false)
            ),
        )
    }
    ChartParent(modifier=Modifier) {
        Box(modifier = Modifier.fillMaxSize().padding(vertical = 12.dp)){
            LineChart(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp),
                data = data,
                animationMode = AnimationMode.Together(delayBuilder = {
                    it * 500L
                }),
                gridProperties = gridProperties,
                dividerProperties = dividerProperties,
                popupProperties = PopupProperties(
                    textStyle = TextStyle(
                        fontSize = 11.sp,
                        color = Color.White
                    ),
                    contentBuilder = { popup->
                        popup.value.format(1) + " Million"
                    },
                    containerColor = Color(0xff414141)
                ),
                indicatorProperties = HorizontalIndicatorProperties(
                    textStyle = TextStyle(
                        fontSize = 11.sp,
                        color = Color.White
                    ),
                    contentBuilder = {
                        it.format(1) + " M"
                    }
                ),
                labelProperties = LabelProperties(
                    enabled = true,
                    labels = listOf("Jan","Feb","Mar","Apr","May", "May2", "May3"),
                    textStyle = TextStyle(
                        fontSize = 11.sp,
                        color = Color.White
                    ),
                ),
                labelHelperProperties = labelHelperProperties,
                curvedEdges = true
            )
        }
    }
}
@Composable
fun LineSample7(modifier: Modifier=Modifier) {
    val data = remember {
        listOf(
            Line(
                label = "Linux",
                values = listOf(
                    71.0,
                    0.0,
                    100.0,
                    50.0,
                    50.0
                ),
                color = Brush.radialGradient(
                    listOf(
                        Color(0xFFFFB300),
                        Color(0xFFD81B60)
                    )
                ),
                strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                gradientAnimationDelay = 1000,
            ),
        )
    }
    ChartParent(modifier=Modifier) {
        Box(modifier = Modifier.fillMaxSize().padding(vertical = 12.dp)){
            LineChart(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp),
                data = data,
                animationMode = AnimationMode.Together(delayBuilder = {
                    it * 500L
                }),
                gridProperties = gridProperties,
                dividerProperties = dividerProperties,
                popupProperties = PopupProperties(
                    textStyle = TextStyle(
                        fontSize = 11.sp,
                        color = Color.White,
                    ),
                    contentBuilder = { popup->
                        popup.value.format(1) + " Million"
                    },
                    containerColor = Color(0xff414141),
                ),
                indicatorProperties = HorizontalIndicatorProperties(
                    textStyle = TextStyle(
                        fontSize = 11.sp,
                        color = Color.White
                    ),
                    contentBuilder = {
                        it.format(1) + " M"
                    }
                ),
                labelHelperProperties = labelHelperProperties,
                curvedEdges = true
            )
        }
    }
}

@Composable
fun LineSample8(modifier: Modifier=Modifier) {
    val data = remember {
        listOf(
            Line(
                label = "Linux",
                values = listOf(
                    10.0,
                    20.0,
                    7.0,
                    35.0,
                    20.0
                ),
                color = SolidColor(Color(0xff5A47CF)),
                firstGradientFillColor = Color(0xff6655CF).copy(alpha = .5f),
                secondGradientFillColor = Color.Transparent,
                strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                gradientAnimationDelay = 1000,
                drawStyle = DrawStyle.Stroke(3.dp)
            ),
        )
    }
    ChartParent(modifier=Modifier) {
        Box(modifier = Modifier.fillMaxSize().padding(vertical = 12.dp)){
            LineChart(
                modifier = Modifier
                    .fillMaxSize(),
                data = data,
                animationMode = AnimationMode.Together(delayBuilder = {
                    it * 500L
                }),
                dividerProperties = DividerProperties(enabled = false),
                gridProperties = GridProperties(enabled = false),
                popupProperties = PopupProperties(
                    textStyle = TextStyle(
                        fontSize = 11.sp,
                        color = Color.White,
                    ),
                    contentBuilder = { popup->
                        popup.value.format(1) + " Million"
                    },
                    containerColor = Color(0xff414141)
                ),
                indicatorProperties = HorizontalIndicatorProperties(
                    enabled = false
                ),
                labelProperties = LabelProperties(enabled = false),
                labelHelperProperties = LabelHelperProperties(enabled = false),
                curvedEdges = true
            )
        }
    }
}

@Composable
fun LineSample9(modifier: Modifier=Modifier) {
    val data = remember {
        listOf(
            Line(
                label = "Windows",
                values = MutableList(5) { (0..100).random().toDouble() },
                color = SolidColor(Color(0xFFfd9644)),
                strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                gradientAnimationDelay = 1000,
                drawStyle = DrawStyle.Fill,
                curvedEdges = true,
            ),
        )
    }
    ChartParent(modifier = Modifier) {
        Box(modifier = Modifier.fillMaxSize().padding(vertical = 12.dp)) {
            LineChart(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp),
                data = data,
                animationMode = AnimationMode.Together(delayBuilder = {
                    it * 500L
                }),
                gridProperties = gridProperties,
                dividerProperties = dividerProperties,
                popupProperties = PopupProperties(
                    textStyle = TextStyle(
                        fontSize = 11.sp,
                        color = Color.White,
                    ),
                    contentBuilder = { popup ->
                        popup.value.format(1) + " Million"
                    },
                    confirmDraw = {
                        it.value > 50
                    },
                    containerColor = Color(0xff414141)
                ),
                indicatorProperties = HorizontalIndicatorProperties(
                    textStyle = TextStyle(
                        fontSize = 11.sp,
                        color = Color.White
                    ),
                    contentBuilder = {
                        it.format(1) + " M"
                    }
                ),
                labelHelperProperties = labelHelperProperties,
                curvedEdges = false
            )
        }
    }
}
*/

/*
object FitTypeImpl {
    var d = LocalCustomColorsPalette.current.iconColorActive
    val info = mapOf(
        FitFile.FitListType.ALTITUDE to FitTypeInfo("Высота", R.color.ALTITUDE),
        FitFile.FitListType.GRADE to FitTypeInfo("Градиент", R.color.SPEED),
        FitFile.FitListType.SPEED to FitTypeInfo("Скорость", R.color.SPEED),
        FitFile.FitListType.HEART to FitTypeInfo("ЧСС", R.color.HEART),
        FitFile.FitListType.CADENCE to FitTypeInfo("Каденс", R.color.CADENCE),
        FitFile.FitListType.TEMPERATURE to FitTypeInfo("Температура", R.color.TEMPERATURE),
        FitFile.FitListType.HEART_TIME to FitTypeInfo("ЧСС по времени", R.color.HEART_TIME)
    )
}
 */

/*@Composable
fun Chart(type: FitFile.FitListType, fitRecords: FitFile) {
    AndroidView(factory = { context ->
        LineChart(context).apply {
        }
    }, modifier = Modifier.fillMaxWidth().height(200.dp).padding(5.dp),
    update = {
        updateChartWithData(it, type, fitRecords.records[type], fitRecords.records[FitFile.FitListType.ALTITUDE], fitRecords.xMeasurement)
    })
}

fun updateChartWithData(lineChart: LineChart, type: FitFile.FitListType, data: List<Entry>?, altitudeValues: List<Entry>?, xMeasurement: FitFile.XMeasurement) {
    val typedValue = TypedValue()
    lineChart.context.theme.resolveAttribute(android.R.attr.textColorSecondary, typedValue, true)
    val textColor = ContextCompat.getColor(lineChart.context, typedValue.resourceId)

    val lineData = LineData()
    if ((type == FitFile.FitListType.SPEED || type == FitFile.FitListType.HEART || type == FitFile.FitListType.CADENCE)
        && altitudeValues!=null) {
        lineData.addDataSet(makeDataSet(lineChart.context, FitFile.FitListType.ALTITUDE, altitudeValues))
    }
    val xData = data ?: listOf()
    lineData.addDataSet(makeDataSet(lineChart.context, type, xData))

    with(lineChart) {
        this.data = lineData
        setDrawGridBackground(false)
        val description = Description()
        description.text = FitTypeImpl.info[type]?.caption
        description.textSize = 15f
        lineChart.description = description
        description.isEnabled = false

        legend.textColor = textColor
        xAxis.textColor = textColor
        axisLeft.textColor = textColor
        axisRight.textColor = textColor

        if(xMeasurement==FitFile.XMeasurement.TIME && type != FitFile.FitListType.HEART_TIME) {
            xAxis.valueFormatter = if (xAxis.granularity >= 1800) DurationShortValueFormatter() else DurationValueFormatter()
        } else {
            xAxis.valueFormatter = DistanceValueFormatter()
        }

        setNoDataText("Нет данных")
        setNoDataTextColor(textColor)
        setDrawBorders(true)
        setDrawGridBackground(false)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
    }
    lineChart.invalidate()
}

private fun makeDataSet(context: Context, type: FitFile.FitListType, values: List<Entry>): LineDataSet {
    val dataSet = LineDataSet(values, FitTypeImpl.info[type]?.caption)
    dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
    dataSet.color = ContextCompat.getColor(context, FitTypeImpl.info[type]!!.color)
    dataSet.setDrawCircles(false)
    dataSet.setDrawValues(false)

    dataSet.mode = LineDataSet.Mode.LINEAR
    dataSet.fillColor = dataSet.color
    //dataSet.fillAlpha = 100
    dataSet.setDrawFilled(true)
    return dataSet
}*/

/*@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
fun TabsPreview1() {
    val y = listOf(3.0, 4.0, 2.5, 3.0, 2.0, 9.0)
    val x = listOf(0.0, 1.0, 2.0, 3.0, 4.0, 5.0)

    val modelProducer = remember { CartesianChartModelProducer() }
    val yAxisItemPlacer = remember { FitVerticalAxisItemPlacer(0) }

    val rangeProvider = remember(yAxisItemPlacer.minVal, yAxisItemPlacer.maxVal) {
        mutableStateOf(CartesianLayerRangeProvider.auto())
    }

    LaunchedEffect(Unit) {
        modelProducer.runTransaction {
            lineSeries { series(x, y) }
        }

        yAxisItemPlacer.values = y
        rangeProvider.value = CartesianLayerRangeProvider.fixed(minY = yAxisItemPlacer.minVal,
            maxY = yAxisItemPlacer.maxVal,
            minX = 0.0)
    }

    CartesianChartHost(modifier = Modifier.padding(top = 30.dp).onGloballyPositioned() {
        coordinates -> yAxisItemPlacer.height = coordinates.size.height
    },
        animateIn = false,
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(
                            fill(Color.Red))
                    ),
                ),
                rangeProvider = rangeProvider.value
            ),
            //startAxis = VerticalAxis.rememberStart(),
            startAxis = VerticalAxis.rememberStart(itemPlacer = yAxisItemPlacer ),
            bottomAxis = HorizontalAxis.rememberBottom(),
        ),
        modelProducer = modelProducer,
        scrollState = rememberVicoScrollState(scrollEnabled = false),
    )
}*/
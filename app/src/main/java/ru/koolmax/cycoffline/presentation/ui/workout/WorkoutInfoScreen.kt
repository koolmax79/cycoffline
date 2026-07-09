package ru.koolmax.cycoffline.presentation.ui.workout

import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.ehsannarmani.compose_charts.RowChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ru.koolmax.cycoffline.R
import ru.koolmax.cycoffline.data.HeartZone
import ru.koolmax.cycoffline.data.HeartZoneInfo
import ru.koolmax.cycoffline.data.db.FitSessionItem
import ru.koolmax.cycoffline.data.media.Zone
import ru.koolmax.cycoffline.presentation.MeasureUtil
import ru.koolmax.cycoffline.ui.theme.LocalCustomColorsPalette
import ru.koolmax.cycoffline.ui.theme.LocalSpacing
import java.time.LocalDateTime

@Composable
fun WorkoutInfoScreen(viewModel: WorkoutViewModel) {
    val session by remember { viewModel.fitSessionItem }.collectAsState()
    val heartZone by remember { viewModel.heartZone }.collectAsState()

    Column(modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally) {
        WorkoutInfo(session)
        //HeartZones3(heartZone)
        HeartZones2(heartZone)
        //HeartZones(heartZone)
    }
}

@Composable
fun WorkoutInfo(itm: FitSessionItem) {
    with(itm) {
        SessionValue("время старта", MeasureUtil.getDateTime(startTime), modifier = Modifier.fillMaxWidth())
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SessionValue(stringResource(R.string.elapsedTime), MeasureUtil.getDuration(totalElapsedTime))
                SessionValue(stringResource(R.string.distance), MeasureUtil.getDistance(totalDistance))
                SessionValue("средняя скорость", MeasureUtil.getSpeed(avgSpeed))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SessionValue(stringResource(R.string.movingTime), MeasureUtil.getDuration(totalMovingTime))
                SessionValue("набор высоты", MeasureUtil.getDistance(totalAscent))
                SessionValue("максимальная скорость", MeasureUtil.getSpeed(maxSpeed))
            }
        }
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround) {
            SessionValue("средняя ЧСС", MeasureUtil.getHeartRate(avgHeartRate))
        }
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SessionValue("min ЧСС", MeasureUtil.getHeartRate(minHeartRate))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SessionValue("max ЧСС", MeasureUtil.getHeartRate(maxHeartRate))
            }
        }
    }
}

@Composable
fun SessionValue(description: String, value: String, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineLarge)
        Text(text = description, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun SessionValue(description: String, value: Pair<String, String>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = value.first, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(end = 2.dp))
            Text(text = value.second, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 2.dp))
        }
        Text(text = description, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun HeartZones(zone: List<Zone>) {
    if(zone.isNotEmpty()) {
/*
        zone.forEach {
            Log.i("cycoffline1", "${it.begin} ${it.heartSum.toString()}")
        }
*/
        val data = zone.map { zone ->
            Bars(
                label = zone.begin.toString(),
                values = listOf(
                    Bars.Data(label = "ЧСС", value = zone.heartSum.toDouble(), color = SolidColor(LocalCustomColorsPalette.current.getHeartZoneColor(zone.zoneInfo.idx)))
                )
            )
        }
        RowChart(
            modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp),
            data = data,
            //barProperties = BarProperties(
            //    radius = Bars.Data.Radius.Rectangle(topRight = 6.dp, topLeft = 6.dp),
            //    spacing = 3.dp,
            //    strokeWidth = 20.dp
            //),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
        )
        //val max = zone.maxOf { it.second }
        //val sum = zone.sumOf { it.second }
        //Column(modifier = Modifier.fillMaxWidth()) {
        //    zone.forEach {
        //        Zone(it, sum)
        //    }
        //}
    }
}

@Composable
fun HeartZones2(zone: List<Zone>) {
    if(zone.isNotEmpty()) {
        val max = zone.maxOf { it.heartSum }
        val sum = zone.sumOf { it.heartSum }
        Column(modifier = Modifier.fillMaxWidth()) {
            zone.reversed().forEach {
                Zone(it, sum)
            }
        }
    }
}

@Composable
fun Zone(zone: Zone, sum: Int) {
    val textStyle = TextStyle(color = Color.Black)
    val color = LocalCustomColorsPalette.current.getHeartZoneColor(zone.zoneInfo.idx)
    Row(modifier = Modifier.padding(2.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(6.dp)
                )
                .drawWithContent {
                    with(drawContext.canvas.nativeCanvas) {
                        drawRect(
                            color = color,
                            size = Size(size.width * zone.heartSum.toFloat() / sum, size.height),
                        )
                        drawContent()
                    }
                }
        ) {
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                Text(modifier = Modifier.weight(1f).padding(end = LocalSpacing.current.space600),
                    textAlign = TextAlign.Right,
                    style = MaterialTheme.typography.headlineSmall,
                    text = MeasureUtil.getPercent(zone.heartSum.toFloat() / sum * 100).toList()
                        .joinToString(" ")
                )
                Text(modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    text = MeasureUtil.getDuration(zone.heartSum)
                )
                Text(modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    text = getTextHeartRate(zone.zoneInfo.min, zone.zoneInfo.max))
            }
        }
    }
}

fun getTextHeartRate(min: Int, max: Int) = when {
    min!=Int.MIN_VALUE && max!=Int.MAX_VALUE -> "$min - $max"
    min==Int.MIN_VALUE -> "< $max"
    else -> "> $min"
}

//@Composable
//fun PieChart(entries: List<PieEntry>) {
//    AndroidView(modifier = Modifier.fillMaxWidth().height(300.dp),
//        factory = { context ->
//            val dataSet = PieDataSet(entries, "")
//            var chart = PieChart(context)
//            val data = PieData(dataSet)
//            dataSet.selectionShift = 0f
//            dataSet.sliceSpace = 3f
//            dataSet.colors()
//            chart.data = data
//            chart
//        })
//}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val itm = FitSessionItem(
        fileName = "1111",
        displayed = 1,
        avgHeartRate = 80,
        avgSpeed = 25.0,
        maxAltitude = 0.6,
        maxHeartRate = 180,
        maxNegGrade = -3.0,
        maxPosGrade = 3.0,
        maxSpeed = 40.0,
        minHeartRate = 100,
        startTime = LocalDateTime.now(),
        totalAscent = 100,
        totalDescent = 20,
        totalDistance = 60000,
        totalElapsedTime = 40000,
        totalMovingTime = 23423
    )
    //val calendar = Calendar.getInstance()
    //calendar.set(2000, 0, 1)
    //HeartZone().setBirthDay(calendar.toLocalDate(), 70, Gender.MALE)
    //val zone = HeartZone().list.map { it to 20 }.toList()
}
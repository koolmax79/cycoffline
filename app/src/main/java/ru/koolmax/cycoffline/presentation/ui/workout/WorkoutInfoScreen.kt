package ru.koolmax.cycoffline.presentation.ui.workout

import android.content.res.Configuration
import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.koolmax.cycoffline.R
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

    when(LocalConfiguration.current.orientation) {

        Configuration.ORIENTATION_PORTRAIT -> {
            WorkoutInfoScreenPortrait(session, heartZone)
        }

        Configuration.ORIENTATION_LANDSCAPE -> {
            WorkoutInfoScreenLandscape(session, heartZone)
        }
    }
}

@Composable
fun WorkoutInfoScreenPortrait(session: FitSessionItem, heartZone: List<Zone>) {
    Column(modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally) {
        WorkoutInfo(modifier = Modifier.fillMaxWidth().padding(LocalSpacing.current.space100), session = session)
        HeartZones(modifier = Modifier.padding(LocalSpacing.current.space100), zone = heartZone)
    }
}

@Composable
fun WorkoutInfoScreenLandscape(session: FitSessionItem, heartZone: List<Zone>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        WorkoutInfo(modifier = Modifier.width(IntrinsicSize.Max).padding(LocalSpacing.current.space100), session = session)
        HeartZones(modifier = Modifier.padding(LocalSpacing.current.space100), zone = heartZone)
    }
}

@Composable
fun WorkoutInfo(modifier: Modifier = Modifier, session: FitSessionItem) {
    with(session) {
        Column(modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally) {
            SessionValue(modifier = Modifier.fillMaxWidth(),"время старта", MeasureUtil.getDateTime(startTime))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SessionValue(description = stringResource(R.string.elapsedTime), value = MeasureUtil.getDuration(totalElapsedTime))
                    SessionValue(description = stringResource(R.string.distance), value = MeasureUtil.getDistance(totalDistance))
                    SessionValue(description = "средняя скорость", value = MeasureUtil.getSpeed(avgSpeed?.toFloat()))
                    SessionValue(description = "средняя ЧСС", value = MeasureUtil.getHeartRate(avgHeartRate))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SessionValue(description = stringResource(R.string.movingTime), value = MeasureUtil.getDuration(totalMovingTime))
                    SessionValue(description = "набор высоты", value = MeasureUtil.getDistance(totalAscent))
                    SessionValue(description = "максимальная скорость", value = MeasureUtil.getSpeed(maxSpeed?.toFloat()))
                    SessionValue(description = "max ЧСС", value = MeasureUtil.getHeartRate(maxHeartRate))
                }
            }
        }
    }
}

@Composable
fun SessionValue(modifier: Modifier = Modifier, description: String, value: String) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, style = MaterialTheme.typography.headlineLarge)
            Text(text = description, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun SessionValue(modifier: Modifier = Modifier, description: String, value: Pair<String, String>) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(end = 2.dp), text = buildAnnotatedString {
                append(value.first)
                withStyle(style = MaterialTheme.typography.bodySmall.toSpanStyle()) {
                    append(value.second)
                }
            })
        }
        Text(text = description, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun HeartZones(modifier: Modifier = Modifier, zone: List<Zone>) {
    if(zone.isNotEmpty()) {
        val max = zone.maxOf { it.heartSum }
        val sum = zone.sumOf { it.heartSum }
        Column(modifier = modifier) {
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
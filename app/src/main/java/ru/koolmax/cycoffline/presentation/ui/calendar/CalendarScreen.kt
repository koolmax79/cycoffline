package ru.koolmax.cycoffline.presentation.ui.calendar

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import ru.koolmax.cycoffline.R
import ru.koolmax.cycoffline.data.db.FitSessionItem
import ru.koolmax.cycoffline.presentation.MeasureUtil
import ru.koolmax.cycoffline.presentation.ui.navigation.Route
import ru.koolmax.cycoffline.ui.theme.CycofflineTheme
import ru.koolmax.cycoffline.ui.theme.LocalSpacing
import java.time.DayOfWeek
import java.time.Month
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(navController: NavController, modifier: Modifier, viewModel: CalendarViewModel = hiltViewModel()) {
    val fitSessionList by  remember { viewModel.fitSessionList }.collectAsState()
    val statistic by remember { viewModel.fitSessionStatistic }.collectAsState()
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) } // Adjust as needed
    val endMonth = remember { currentMonth.plusMonths(100) } // Adjust as needed
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() } // Available from the library
    val showDialog = remember { mutableStateOf(false) }
    val sessionList = remember { mutableStateOf(listOf<FitSessionItem>()) }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    if(showDialog.value) {
        WorkoutListDialog(sessionList.value,
            setShowDialog = {
                showDialog.value = it
            }, onConfirmation = {
                navController.navigate(Route.Workout.route.replace("{fit}", it.fileName))
        } )
    }

    Column(modifier = Modifier.fillMaxWidth().padding( LocalSpacing.current.space100),
        horizontalAlignment = Alignment.CenterHorizontally) {
        InfoRow(stringResource(R.string.number_trips), statistic.count.toString())
        InfoRow(stringResource(R.string.distance), MeasureUtil.getDistance(statistic.totalDistance).toList()
            .joinToString(" "))
        InfoRow(stringResource(R.string.ascent), MeasureUtil.getAscent(statistic.totalAscent).toList()
            .joinToString(" "))
        InfoRow("падение", MeasureUtil.getAscent(statistic.totalDescent).toList()
            .joinToString(" "))
        InfoRow(stringResource(R.string.totalMovingTime), MeasureUtil.getDuration(statistic.totalMovingTime).toList()
            .joinToString(" "))
        HorizontalCalendar(
            state = state,
            modifier = Modifier.background(color = MaterialTheme.colorScheme.surface),
            dayContent = {
                val list = fitSessionList.getOrDefault(it.date, listOf())
                Day(it, list.size, onClick = {
                    if(list.count() > 0) {
                        sessionList.value = list
                        showDialog.value = true
                    }
                    if(list.count() == 1)  {
                        navController.navigate(
                            Route.Workout.route.replace(
                            "{fit}",
                            list.first().fileName
                            )
                        )
                    }
                })
            },
            monthHeader = {
                viewModel.loadFitSessionList(
                    state.firstVisibleMonth.yearMonth.atStartOfMonth(),
                    state.firstVisibleMonth.yearMonth.atEndOfMonth()
                )
                MonthHeader(it)
            }
        )
    }
}

@Composable
fun InfoRow(name: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = name, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun WorkoutListDialog(list: List<FitSessionItem>, setShowDialog: (Boolean) -> Unit, onConfirmation: (FitSessionItem) -> Unit) {
    Dialog(onDismissRequest = { setShowDialog(false) }) {
        Surface(
            shape = RoundedCornerShape(LocalSpacing.current.space100),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(LocalSpacing.current.space100)) {
                for(itm in list) {
                    Row(modifier = Modifier.clickable { onConfirmation(itm) }) {
                        Text(text = MeasureUtil.getTime(itm.startTime), modifier = Modifier.padding(horizontal = LocalSpacing.current.space25))
                        val distance = MeasureUtil.getDistance(itm.totalDistance)
                        Text(text = distance.first,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = LocalSpacing.current.space25)
                        )
                        Text(text = distance.second,
                            style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) } // Adjust as needed
    val endMonth = remember { currentMonth.plusMonths(100) } // Adjust as needed
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() } // Available from the library

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )
    CycofflineTheme {
        HorizontalCalendar(
            state = state,
            dayContent = {  },
            monthHeader = { MonthHeader(it) }
        )
    }
}

@Composable
private fun MonthHeader(calendarMonth: CalendarMonth)
{
    val daysOfWeek = daysOfWeek()
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .background(color = MaterialTheme.colorScheme.surfaceDim)
            .padding(top = 6.dp),
        //verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Column() {
            Text(
//              modifier = Modifier.fillMaxWidth(),
                text = calendarMonth.yearMonth.year.toString(),
                fontStyle = MaterialTheme.typography.displayMedium.fontStyle,
                fontSize = MaterialTheme.typography.displayMedium.fontSize,
                textAlign = TextAlign.Left,
                fontWeight = MaterialTheme.typography.displayMedium.fontWeight
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = calendarMonth.yearMonth.displayText(),
                fontStyle = MaterialTheme.typography.displayMedium.fontStyle,
                fontSize = MaterialTheme.typography.displayMedium.fontSize,
                textAlign = TextAlign.Left,
                fontWeight = MaterialTheme.typography.displayMedium.fontWeight
                )
            Row(modifier = Modifier.fillMaxWidth()) {
                for (dayOfWeek in daysOfWeek) {
                    Text(
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontStyle = MaterialTheme.typography.headlineMedium.fontStyle,
                        fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                        text = dayOfWeek.displayText(),
                        fontWeight = MaterialTheme.typography.headlineMedium.fontWeight
                    )
                }
            }
        }
        //HorizontalDivider(color = Color.Black)
    }
}

@Composable
fun Day(day: CalendarDay, fitSessionCount: Int, onClick: (CalendarDay) -> Unit) {
    Box(
        modifier = Modifier.aspectRatio(1f)
            //.clip(RoundedCornerShape(8.dp))
            .background(color = if (day.position == DayPosition.MonthDate) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surface)
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                onClick = { onClick(day) }
            ),
        contentAlignment = Alignment.Center
    ) {

        BadgedBox(badge = {
            if(fitSessionCount > 0) {
                Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                    Text(text = fitSessionCount.toString(),
                        fontStyle = MaterialTheme.typography.titleSmall.fontStyle,
                        fontSize = MaterialTheme.typography.titleSmall.fontSize,
                        fontWeight = MaterialTheme.typography.titleSmall.fontWeight)
                }
            }
        }) {

            Text(text = day.date.dayOfMonth.toString(),
                fontStyle = MaterialTheme.typography.titleLarge.fontStyle,
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                fontWeight = if(day.position == DayPosition.MonthDate) FontWeight.SemiBold else MaterialTheme.typography.titleLarge.fontWeight,
                color = if(day.position == DayPosition.MonthDate) Color.Unspecified else MaterialTheme.colorScheme.onSurfaceVariant )
        }
    }
}

fun YearMonth.displayText() = MeasureUtil.getMonth(month.value)

fun Month.displayText(short: Boolean = true) = MeasureUtil.getMonth(value)

fun DayOfWeek.displayText() = MeasureUtil.getWeek(value)
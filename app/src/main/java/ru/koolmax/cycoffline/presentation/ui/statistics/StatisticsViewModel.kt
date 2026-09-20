package ru.koolmax.cycoffline.presentation.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.koolmax.cycoffline.data.db.FitConverter
import ru.koolmax.cycoffline.data.db.FitRepository
import ru.koolmax.cycoffline.data.db.FitSessionItem
import ru.koolmax.cycoffline.data.FitStatisticItem
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.AxisType
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

enum class ChartType(val code: Int, val text: String, val axisType: AxisType) {
    DISTANCE(1, "Дистанция", AxisType.INT),
    AVG_HEART_RATE(2, "Средняя ЧСС", AxisType.INT),
    AVG_SPEED(3, "Средняя скорость", AxisType.INT),
    ASCENT(4, "Подъем", AxisType.INT),
    MOVING_TIME(5, "Время в движении", AxisType.TIME),
    MAX_HEART_RATE(6, "Максимальная ЧСС", AxisType.INT),
    NONE(0, "", AxisType.INT)
}

@HiltViewModel
class StatisticsViewModel @Inject constructor(private val repository: FitRepository) : ViewModel() {
    val years = MutableStateFlow(listOf<Int>())
    val months = MutableStateFlow(listOf<Int>())
    //val entryList = MutableStateFlow(Pair(listOf<FitSessionItem>(), listOf<Number>()))
    val sessionStatistic = MutableStateFlow(FitStatisticItem())
    val fitSessionList = MutableStateFlow(listOf<FitSessionItem>())
    private var curYear = 0

    suspend fun getRangeYear() {
        val range = repository.rangeStartTime()?.let { Pair(it.first.year, it.second.year) } ?: Pair(LocalDate.now().year, LocalDate.now().year)
        years.emit( (range.first .. range.second).map { it })
        getStatistic(range.second, 0)
    }

    suspend fun getRangeMonth(year: Int) {
        val begin = LocalDateTime.of(year, 1, 1, 0, 0)
        val end = begin.plusYears(1)

        //Log.i("cycoffline1","getRangeMonth ${begin} ${end}")

        val range = repository.rangeStartTime(FitConverter().toInt(begin), FitConverter().toInt(end))?.let { Pair(it.first.monthValue, it.second.monthValue) } ?: Pair(1, 12)
        val list= (range.first.. range.second).map { it }.toMutableList()
        list.add(0, 0)
        months.emit(list)
    }

    fun getStatistic(year: Int, month: Int) {
        viewModelScope.launch {
            val (begin, end) = if(month==0) {
                val begin = LocalDateTime.of(year, 1, 1, 0, 0)
                Pair(begin, begin.plusYears(1))
            }
            else {
                val begin = LocalDateTime.of(year, month, 1, 0, 0)
                Pair(begin, begin.plusMonths(1))
            }

            //Log.i("cycoffline1","getStatistic ${begin} ${end}")

            val sessionlist = repository.allByInterval(FitConverter().toInt(begin), FitConverter().toInt(end))
            fitSessionList.emit(sessionlist)
            sessionStatistic.emit(FitStatisticItem.make(sessionlist))

            //val firstMonth = sessionlist.firstOrNull()?.startTime?.month?.value ?: 0
            //val lastMonth = sessionlist.lastOrNull()?.startTime?.month?.value ?: 0
            //months.emit(((firstMonth..lastMonth).map { it } + 0).toSortedSet().toList())
        }
    }
}
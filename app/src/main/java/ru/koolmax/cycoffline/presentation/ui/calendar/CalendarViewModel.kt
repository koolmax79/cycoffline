package ru.koolmax.cycoffline.presentation.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.koolmax.cycoffline.data.db.FitConverter
import ru.koolmax.cycoffline.data.db.FitRepository
import ru.koolmax.cycoffline.data.db.FitSessionItem
import ru.koolmax.cycoffline.data.FitStatisticItem
import ru.koolmax.cycoffline.data.db.toLocalDateTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Calendar
import javax.inject.Inject

fun LocalDateTime.toCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    with(this) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, monthValue - 1)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, second)
        calendar.set(Calendar.MILLISECOND, nano / 1000000)
    }
    return calendar
}

fun Calendar.toLocalDate(): LocalDate {
    return LocalDate.of(this.time.year+1900, this.time.month+1, this.time.date)
}

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: FitRepository): ViewModel() {
    private val _fitSessionStatistic = MutableStateFlow(FitStatisticItem())
    val fitSessionStatistic = _fitSessionStatistic.asStateFlow()

    private var _fitSessionList = MutableStateFlow(mapOf<LocalDate, List<FitSessionItem>>())
    val fitSessionList = _fitSessionList.asStateFlow()

    fun loadFitSessionList(begin: LocalDate, end: LocalDate) {
        viewModelScope.launch {
            val fitSessionMap = repository.allByInterval(FitConverter().toInt(begin.plusDays(-6).toLocalDateTime()), FitConverter().toInt(end.plusDays(6).toLocalDateTime())).groupBy {
                val dt = it.startTime!!
                LocalDate.of(dt.year, dt.month, dt.dayOfMonth)
            }
            _fitSessionList.emit(fitSessionMap)
            _fitSessionStatistic.emit(FitStatisticItem.make(fitSessionMap.filter { it.key in begin..end }.values.flatten()))
            //_fitSessionStatistic.emit(repository.getStatistic(FitConverter().toInt(begin.toLocalDateTime()), FitConverter().toInt(end.toLocalDateTime())))
        }
    }
}
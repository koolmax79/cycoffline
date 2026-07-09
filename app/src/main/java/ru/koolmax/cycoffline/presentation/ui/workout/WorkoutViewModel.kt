package ru.koolmax.cycoffline.presentation.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.koolmax.cycoffline.data.HeartZoneInfo
import ru.koolmax.cycoffline.data.db.FitRepository
import ru.koolmax.cycoffline.data.db.FitSessionItem
import ru.koolmax.cycoffline.data.db.SettingsDBRepository
import ru.koolmax.cycoffline.data.media.FileRepository
import ru.koolmax.cycoffline.data.media.FitFile
import ru.koolmax.cycoffline.data.media.FitListType
import ru.koolmax.cycoffline.data.media.MonitoringData
import ru.koolmax.cycoffline.data.media.XMeasurement
import ru.koolmax.cycoffline.data.media.XPause
import ru.koolmax.cycoffline.data.media.Zone
import ru.koolmax.cycoffline.presentation.ui.lib.ChartData
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(private val fileRepository: FileRepository,
                                           private val fitRepository: FitRepository,
                                           private val settingsRepository: SettingsDBRepository
): ViewModel() {

    val xMeasurementMode = MutableStateFlow(XMeasurement.TIME)
    val xPauseMode = MutableStateFlow(XPause.SHOW)
    val fitSessionItem = MutableStateFlow(FitSessionItem())
    val heartZone = MutableStateFlow(listOf<Zone>())
    val monitoringData = MutableStateFlow(MonitoringData())
    val timeByMonitoring = MutableStateFlow(listOf<Pair<FitListType, ChartData>>())
    private lateinit var fitFile: FitFile

    fun getFitSession(fit: String) {
        viewModelScope.launch {
            fitSessionItem.emit (fitRepository.getSession(fit))
            fileRepository.getRecords(fit)?.let {
                fitFile = it
                monitoringData.emit(fitFile.getMonitoringData(xMeasurementMode.value, xPauseMode.value))
                heartZone.emit(it.getHeartZone(settingsRepository.heartZone))
            }

            fitRepository.getSession(fit).let {
                it.displayed = 1
                fitRepository.update(it)
            }
        }
    }

    fun setMode(measurementMode: XMeasurement, pauseMode: XPause) {
        viewModelScope.launch {
            xMeasurementMode.emit(measurementMode)
            xPauseMode.emit(pauseMode)
            monitoringData.emit(fitFile.getMonitoringData(measurementMode, pauseMode))
        }
    }

    fun getTimeByMonitoring() {
        viewModelScope.launch {
            timeByMonitoring.emit(fitFile.timeByMonitoring)
        }
    }

    fun separateHeartByZone(chartData: ChartData): List<Pair<HeartZoneInfo, List<Int>>> {
        return chartData.separateByZone( settingsRepository.heartZone.list.map { Triple(it, it.min, it.max) } )
    }
}
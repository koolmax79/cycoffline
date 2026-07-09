package ru.koolmax.cycoffline.data.media

import com.garmin.fit.RecordMesg
import com.garmin.fit.RecordMesgListener
import ru.koolmax.cycoffline.data.HeartZone
import ru.koolmax.cycoffline.data.HeartZoneInfo
import ru.koolmax.cycoffline.presentation.ui.interpolateX
import ru.koolmax.cycoffline.presentation.ui.interpolateY
import ru.koolmax.cycoffline.presentation.ui.lib.ChartData
import kotlin.collections.listOf
import kotlin.math.min
import kotlin.math.roundToInt


enum class FitListType { SPEED, HEART, CADENCE, GRADE, ALTITUDE, TEMPERATURE
    //, HEART_TIME
}
enum class XMeasurement { DISTANCE, TIME }
enum class XPause { SHOW, HIDE }

data class MonitoringChart(val min: Double, val max: Double, val yValues: List<Double>, val info: List<Pair<String, Double>>) {
    companion object {
        fun create(yValues: List<Double>, info: List<Pair<String, Double>>) = MonitoringChart(yValues.min(), yValues.max(), yValues, info)
    }
}
data class MonitoringData(val chartData: Map<FitListType, MonitoringChart> = mapOf(), val xValues: List<Int> = listOf())
data class Zone(val begin: Int, val end: Int, val zoneInfo: HeartZoneInfo, var heartSum: Int=0)
{
    fun addHeart(v: Int) {
        this.heartSum += v
    }
}

class FitFile(val info: FitInfo): RecordMesgListener {
    private val yInterCount = 1000

    private data class MonitoringValues(var values: MutableList<Double>, var loaded: Boolean = false)

    val timeByMonitoring: List<Pair<FitListType, ChartData>> by lazy {
        val list = mutableListOf<Pair<FitListType, ChartData>>()
        if(heartValues.loaded)
            list.add(Pair(FitListType.HEART, timeByHeart))
        if(speedValues.loaded)
            list.add(Pair(FitListType.SPEED, getTimeByMonitoring(speedValues.values)))
        if(cadenceValues.loaded)
            list.add(Pair(FitListType.CADENCE, getTimeByMonitoring(cadenceValues.values)))
        list
    }

    private val timeByHeart by lazy {
        getTimeByMonitoring(heartValues.values)
    }

    private fun getTimeByMonitoring(monitoringValues: List<Double>): ChartData {
        val valuesWithoutPause = getRemovePauseByTime(monitoringValues, distanceValues).filter { it != 0.0 } //.run { if(removeZero) this.filter { it == 0.0 } else this }
        val xMin = valuesWithoutPause.min().roundToInt()
        val xMax = valuesWithoutPause.max().roundToInt()
        val yValues = MutableList(xMax - xMin + 1) { 0 }
        valuesWithoutPause.forEach {
            val value = it.roundToInt() - xMin
            yValues[value] = (yValues[value] ?: 0) + 1
        }
        return ChartData(yValues, xMin)
    }

    private var distanceValues    = MutableList(info.timeCount) { 0 }
    private var speedValues       = MonitoringValues(MutableList(info.timeCount) { 0.0 })
    private var heartValues       = MonitoringValues(MutableList(info.timeCount) { 0.0 })
    private var cadenceValues     = MonitoringValues(MutableList(info.timeCount) { 0.0 })
    private var gradeValues       = MonitoringValues(MutableList(info.timeCount) { 0.0 })
    private var altitudeValues    = MonitoringValues(MutableList(info.timeCount) { 0.0 })
    private var temperatureValues = MonitoringValues(MutableList(info.timeCount) { 0.0 })
    private val monitoringList = listOf( Pair(FitListType.SPEED, speedValues),
        Pair(FitListType.HEART, heartValues),
        Pair(FitListType.CADENCE, cadenceValues),
        Pair(FitListType.GRADE, gradeValues),
        Pair(FitListType.ALTITUDE, altitudeValues),
        Pair(FitListType.TEMPERATURE, temperatureValues))
        //FitListType.HEART_TIME to Pair(mutableListOf<Number>(), mutableListOf<Number>())

    private val distanceData: MonitoringData by lazy {
        val chartData = monitoringList.filter { it.second.loaded }.associate { it.first to getChart(it.first, XMeasurement.DISTANCE, XPause.HIDE) }
        MonitoringData( chartData, getXValues(XMeasurement.DISTANCE, XPause.HIDE) )
    }

    private val timeShowPauseData: MonitoringData by lazy {
        val chartData = monitoringList.filter { it.second.loaded }.associate { it.first to getChart(it.first, XMeasurement.TIME, XPause.SHOW) }
        MonitoringData( chartData, getXValues(XMeasurement.TIME, XPause.SHOW) )
    }

    private val timeHidePauseData: MonitoringData by lazy {
        val chartData = monitoringList.filter { it.second.loaded }.associate { it.first to getChart(it.first, XMeasurement.TIME, XPause.HIDE) }
        MonitoringData( chartData, getXValues(XMeasurement.TIME, XPause.HIDE) )
    }

    fun getMonitoringData(measurementType: XMeasurement, pause: XPause) = when(measurementType) {
        XMeasurement.DISTANCE -> distanceData
        XMeasurement.TIME ->
            when(pause) {
                XPause.SHOW -> timeShowPauseData
                XPause.HIDE -> timeHidePauseData
            }
    }

    private fun getXValues(xMeasurement: XMeasurement, pause: XPause): List<Int> {
        when (xMeasurement) {
            XMeasurement.TIME -> {
                when (pause) {
                    XPause.SHOW -> return distanceValues.indices.toList()
                    XPause.HIDE -> {
                        var previousDistance = 0
                        return distanceValues.indices.filter {idx -> (distanceValues[idx]!=0 && distanceValues[idx]!=previousDistance).also { previousDistance = distanceValues[idx] } }
                    }
                }
            }
            XMeasurement.DISTANCE -> {
                return interpolateX(distanceValues, min(yInterCount, distanceValues.size))
            }
        }
    }

    private fun getChart(type: FitListType, xMeasurement: XMeasurement, pause: XPause): MonitoringChart {
        return when(type) {
            FitListType.SPEED -> {
                when (xMeasurement) {
                    XMeasurement.TIME -> {
                        when(pause) {
                            XPause.SHOW -> MonitoringChart.create(
                                speedValues.values,
                                listOf(
                                    Pair("max", getMax(info.session?.maxSpeed?.toDouble()?.let { it * 3.6 }, speedValues.values, distanceValues)),
                                    Pair("avg", getAvg(info.session?.avgSpeed?.toDouble()?.let { it * 3.6 }, speedValues.values, distanceValues))
                                )
                            )
                            XPause.HIDE -> MonitoringChart.create(
                                getRemovePauseByTime(speedValues.values, distanceValues),
                                listOf(
                                    Pair("max", getMax(info.session?.maxSpeed?.toDouble()?.let { it * 3.6 }, speedValues.values, distanceValues)),
                                    Pair("avg", getAvg(info.session?.avgSpeed?.toDouble()?.let { it * 3.6 }, speedValues.values, distanceValues))
                                )
                            )
                        }
                    }
                    XMeasurement.DISTANCE -> MonitoringChart.create(
                        getMonitoringByDistance(speedValues.values, distanceValues),
                        listOf(
                            Pair("max", getMax(info.session?.maxSpeed?.toDouble()?.let { it * 3.6 }, speedValues.values, distanceValues)),
                            Pair("avg", getAvg(info.session?.avgSpeed?.toDouble()?.let { it * 3.6 }, speedValues.values, distanceValues))
                        )
                    )
                }
            }
            FitListType.HEART -> {
                when (xMeasurement) {
                    XMeasurement.TIME -> {
                        when (pause) {
                            XPause.SHOW -> MonitoringChart.create(
                                heartValues.values,
                                listOf(
                                    Pair("min", getMin(info.session?.minHeartRate?.toDouble(), heartValues.values, distanceValues)),
                                    Pair("max", getMax(info.session?.maxHeartRate?.toDouble(), heartValues.values, distanceValues)),
                                    Pair("avg", getAvg(info.session?.avgHeartRate?.toDouble(), heartValues.values, distanceValues))
                                )
                            )
                            XPause.HIDE -> MonitoringChart.create(
                                getRemovePauseByTime(heartValues.values, distanceValues),
                                listOf(
                                    Pair("min", getMin(info.session?.minHeartRate?.toDouble(), heartValues.values, distanceValues)),
                                    Pair("max", getMax(info.session?.maxHeartRate?.toDouble(), heartValues.values, distanceValues)),
                                    Pair("avg", getAvg(info.session?.avgHeartRate?.toDouble(), heartValues.values, distanceValues))
                                )
                            )
                        }
                    }
                    XMeasurement.DISTANCE -> MonitoringChart.create(
                        getMonitoringByDistance(heartValues.values, distanceValues),
                        listOf(
                            Pair("min", getMin(info.session?.minHeartRate?.toDouble(), heartValues.values, distanceValues)),
                            Pair("max", getMax(info.session?.maxHeartRate?.toDouble(), heartValues.values, distanceValues)),
                            Pair("avg", getAvg(info.session?.avgHeartRate?.toDouble(), heartValues.values, distanceValues))
                        )
                    )
                }
            }
            FitListType.CADENCE -> {
                when (xMeasurement) {
                    XMeasurement.TIME -> {
                        when (pause) {
                            XPause.SHOW -> MonitoringChart.create(
                                cadenceValues.values,
                                listOf(
                                    Pair("max", getMax(info.session?.maxCadence?.toDouble(), cadenceValues.values, distanceValues)),
                                    Pair("avg", getAvg(info.session?.avgCadence?.toDouble(), cadenceValues.values, distanceValues))
                                )
                            )
                            XPause.HIDE -> MonitoringChart.create(
                                getRemovePauseByTime(cadenceValues.values, distanceValues),
                                listOf(
                                    Pair("max", getMax(info.session?.maxCadence?.toDouble(), cadenceValues.values, distanceValues)),
                                    Pair("avg", getAvg(info.session?.avgCadence?.toDouble(), cadenceValues.values, distanceValues))
                                )
                            )
                        }
                    }
                    XMeasurement.DISTANCE -> MonitoringChart.create(
                        getMonitoringByDistance(cadenceValues.values, distanceValues),
                        listOf(
                            Pair("max", getMax(info.session?.maxCadence?.toDouble(), cadenceValues.values, distanceValues)),
                            Pair("avg", getAvg(info.session?.avgCadence?.toDouble(), cadenceValues.values, distanceValues))
                        )
                    )
                }
            }
            FitListType.GRADE -> {
                when(xMeasurement) {
                    XMeasurement.TIME -> {
                        when (pause) {
                            XPause.SHOW -> MonitoringChart.create(
                                gradeValues.values,
                                listOf(
                                    Pair("max", getMax(info.session?.maxPosGrade?.toDouble(), gradeValues.values, distanceValues)),
                                    Pair("min", getMin(info.session?.maxNegGrade?.toDouble(), gradeValues.values, distanceValues))
                                )
                            )
                            XPause.HIDE -> MonitoringChart.create(
                                getRemovePauseByTime(gradeValues.values, distanceValues),
                                listOf(
                                    Pair("max", getMax(info.session?.maxPosGrade?.toDouble(), gradeValues.values, distanceValues)),
                                    Pair("min", getMin(info.session?.maxNegGrade?.toDouble(), gradeValues.values, distanceValues))
                                )
                            )
                        }
                    }
                    XMeasurement.DISTANCE -> MonitoringChart.create(
                        getMonitoringByDistance(gradeValues.values, distanceValues),
                        listOf(
                            Pair("max", getMax(info.session?.maxPosGrade?.toDouble(), gradeValues.values, distanceValues)),
                            Pair("min", getMin(info.session?.maxNegGrade?.toDouble(), gradeValues.values, distanceValues))
                        )
                    )
                }
            }
            FitListType.ALTITUDE -> {
                when(xMeasurement) {
                    XMeasurement.TIME -> {
                        when (pause) {
                            XPause.SHOW -> MonitoringChart.create(
                                altitudeValues.values,
                                listOf(
                                    Pair("min", getMin(info.session?.minAltitude?.toDouble(), altitudeValues.values, distanceValues)),
                                    Pair("max", getMax(info.session?.maxAltitude?.toDouble(), altitudeValues.values, distanceValues)),
                                    Pair("avg", getAvg(info.session?.avgAltitude?.toDouble(), altitudeValues.values, distanceValues))
                                )
                            )
                            XPause.HIDE -> MonitoringChart.create(
                                getRemovePauseByTime(altitudeValues.values, distanceValues),
                                listOf(
                                    Pair("min", getMin(info.session?.minAltitude?.toDouble(), altitudeValues.values, distanceValues)),
                                    Pair("max", getMax(info.session?.maxAltitude?.toDouble(), altitudeValues.values, distanceValues)),
                                    Pair("avg", getAvg(info.session?.avgAltitude?.toDouble(), altitudeValues.values, distanceValues))
                                )
                            )
                        }
                    }
                    XMeasurement.DISTANCE -> MonitoringChart.create(
                        getMonitoringByDistance(altitudeValues.values, distanceValues),
                        listOf(
                            Pair("min", getMin(info.session?.minAltitude?.toDouble(), altitudeValues.values, distanceValues)),
                            Pair("max", getMax(info.session?.maxAltitude?.toDouble(), altitudeValues.values, distanceValues)),
                            Pair("avg", getAvg(info.session?.avgAltitude?.toDouble(), altitudeValues.values, distanceValues))
                        )
                    )
                }
            }
            FitListType.TEMPERATURE -> {
                when(xMeasurement) {
                    XMeasurement.TIME -> {
                        when (pause) {
                            XPause.SHOW -> MonitoringChart.create(
                                temperatureValues.values,
                                listOf(
                                    Pair("min", getMin(info.session?.minTemperature?.toDouble(), temperatureValues.values, distanceValues)),
                                    Pair("max", getMax(info.session?.maxTemperature?.toDouble(), temperatureValues.values, distanceValues)),
                                    Pair("avg", getAvg(info.session?.avgTemperature?.toDouble(), temperatureValues.values, distanceValues))
                                )
                            )
                            XPause.HIDE -> MonitoringChart.create(
                                getRemovePauseByTime(temperatureValues.values, distanceValues),
                                listOf(
                                    Pair("min", getMin(info.session?.minTemperature?.toDouble(), temperatureValues.values, distanceValues)),
                                    Pair("max", getMax(info.session?.maxTemperature?.toDouble(), temperatureValues.values, distanceValues)),
                                    Pair("avg", getAvg(info.session?.avgTemperature?.toDouble(), temperatureValues.values, distanceValues))
                                )
                            )
                        }
                    }
                    XMeasurement.DISTANCE -> MonitoringChart.create(
                        getMonitoringByDistance(temperatureValues.values, distanceValues),
                        listOf(
                            Pair("min", getMin(info.session?.minTemperature?.toDouble(), temperatureValues.values, distanceValues)),
                            Pair("max", getMax(info.session?.maxTemperature?.toDouble(), temperatureValues.values, distanceValues)),
                            Pair("avg", getAvg(info.session?.avgTemperature?.toDouble(), temperatureValues.values, distanceValues))
                        )
                    )
                }
            }
        }
    }

    private fun getMax(value: Double?, monitoringValues: List<Double>, distanceValues: List<Int>): Double {
        if(value != null) return value
        var start = 0
        return monitoringValues.filterIndexed { idx, itm ->
            if(distanceValues[idx] > start) {
                start = distanceValues[idx]
                true
            }
            else false
        }.max()
    }

    private fun getAvg(value: Double?, monitoringValues: List<Double>, distanceValues: List<Int>): Double {
        if(value != null) return value
        var start = 0
        return monitoringValues.filterIndexed { idx, itm ->
            if(distanceValues[idx] > start) {
                start = distanceValues[idx]
                true
            }
            else false
        }.average()
    }

    private fun getMin(value: Double?, monitoringValues: List<Double>, distanceValues: List<Int>): Double {
        if(value != null) return value
        var start = 0
        return monitoringValues.filterIndexed { idx, itm ->
            if(distanceValues[idx] > start) {
                start = distanceValues[idx]
                true
            }
            else false
        }.min()
    }

    private fun getMonitoringByDistance(monitoringValues: List<Double>, distanceValues: List<Int>): List<Double>{
        return interpolateY(monitoringValues, distanceValues, min(yInterCount, distanceValues.size))
    }

    private fun getRemovePauseByTime(monitoringValues: List<Double>, distanceValues: List<Int>): List<Double>{
        var previousDistance = 0
        return monitoringValues.filterIndexed { idx, itm -> (distanceValues[idx]!=0 && distanceValues[idx]!=previousDistance).also { previousDistance = distanceValues[idx] } }
    }

    override fun onMesg(p0: RecordMesg?) {
        p0?.let{
            val x = (it.timestamp.timestamp - info.timestampStart).toInt()
            //if(x < 1000)
            //    Log.i("cycoffline1", "${x.toString()} ${it.distance}")
            distanceValues[x] = it.distance.roundToInt()
            if (it.speed != null) {
                speedValues.values[x] = it.speed * 3.6
                speedValues.loaded = true
            }
            if (it.heartRate != null && it.heartRate != 0.toShort()) {
                heartValues.values[x] = it.heartRate.toDouble()
                heartValues.loaded = true
            }

            if (it.cadence != null) {
                cadenceValues.values[x] = it.cadence.toDouble()
                cadenceValues.loaded = true
            }
            if (it.altitude != null) {
                altitudeValues.values[x] = it.altitude.toDouble()
                altitudeValues.loaded = true
            }
            if (it.grade != null) {
                gradeValues.values[x] = it.grade.toDouble()
                gradeValues.loaded = true
            }
            if (it.temperature != null) {
                temperatureValues.values[x] = it.temperature.toDouble()
                temperatureValues.loaded = true
            }
        }
    }

    fun endLoad() {
        //chartData.forEach {key, v ->
        //distanceValues.forEachIndexed { index, itm -> if(itm==0) {
        //    Log.i("cycoffline1", "${index.toString()}")
        //    Log.i("cycoffline1", speedValues.values[index].toString())
        //} }
        //}
        //val list = records[FitListType.HEART_TIME]
        //for(i in heart) {
        //    list?.first?.add(i.key)
        //    list?.second?.add(i.value)
        //}
        //records.entries.removeIf { it.value.first.size==0 }
    }

    fun getHeartZone(heartZone: HeartZone): List<Zone> {
        return heartZone.list.map { info -> Zone(info.min, info.max, info,
            timeByHeart.yValues.filterIndexed { xIdx, y ->
                info.inZone(timeByHeart.getX(xIdx).toShort())
            }.sum() )
        }
    }
}
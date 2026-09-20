package ru.koolmax.cycoffline.data.media

import com.garmin.fit.RecordMesg
import com.garmin.fit.RecordMesgListener
import ru.koolmax.cycoffline.data.HeartZone
import ru.koolmax.cycoffline.data.HeartZoneInfo
import ru.koolmax.cycoffline.presentation.ui.interpolateX
import ru.koolmax.cycoffline.presentation.ui.interpolateY
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.ChartData
import kotlin.collections.listOf
import kotlin.math.min
import kotlin.math.roundToInt


enum class FitListType { SPEED, HEART, CADENCE, GRADE, ALTITUDE, TEMPERATURE
    //, HEART_TIME
}
enum class XMeasurement { DISTANCE, TIME }
enum class XPause { SHOW, HIDE }

data class MonitoringChart(val min: Float, val max: Float, val yValues: List<Float>, val info: List<Pair<String, Float>>) {
    companion object {
        fun create(yValues: List<Float>, info: List<Pair<String, Float>>) = MonitoringChart(yValues.min(), yValues.max(), yValues, info)
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

    private data class MonitoringValues(var values: MutableList<Float>, var loaded: Boolean = false)

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

    private fun getTimeByMonitoring(monitoringValues: List<Float>): ChartData {
        val valuesWithoutPause = getRemovePauseByTime(monitoringValues, distanceValues).filter { it != 0f } //.run { if(removeZero) this.filter { it == 0.0 } else this }
        val xMin = valuesWithoutPause.min().roundToInt()
        val xMax = valuesWithoutPause.max().roundToInt()
        //Log.i("cycoffline1", "${xMin} : ${xMax}")
        //Log.i("cycoffline1", "${monitoringValues.min()} : ${monitoringValues.max()}")

        val yValues = MutableList(xMax - xMin + 1) { 0 }
        valuesWithoutPause.forEach {
            val value = it.roundToInt() - xMin
            yValues[value] = yValues[value] + 1
        }

        //if(yValues.size > (180-xMin))
        //    for (x in  xMin..xMax step 5)
        //        Log.i("cycoffline1", "${x+xMin} : ${yValues[x]}")

        return ChartData.create(yValues = yValues, xMin = xMin)
    }

    private var distanceValues    = MutableList(info.timeCount) { 0 }
    private var speedValues       = MonitoringValues(MutableList(info.timeCount) { 0f })
    private var heartValues       = MonitoringValues(MutableList(info.timeCount) { 0f })
    private var cadenceValues     = MonitoringValues(MutableList(info.timeCount) { 0f })
    private var gradeValues       = MonitoringValues(MutableList(info.timeCount) { 0f })
    private var altitudeValues    = MonitoringValues(MutableList(info.timeCount) { 0f })
    private var temperatureValues = MonitoringValues(MutableList(info.timeCount) { 0f })
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
                                    Pair("max", getMax(info.session?.maxSpeed?.let { it * 3.6f }, speedValues.values, distanceValues)),
                                    Pair("avg", getAvg(info.session?.avgSpeed?.let { it * 3.6f }, speedValues.values, distanceValues))
                                )
                            )
                            XPause.HIDE -> MonitoringChart.create(
                                getRemovePauseByTime(speedValues.values, distanceValues),
                                listOf(
                                    Pair("max", getMax(info.session?.maxSpeed?.let { it * 3.6f }, speedValues.values, distanceValues)),
                                    Pair("avg", getAvg(info.session?.avgSpeed?.let { it * 3.6f }, speedValues.values, distanceValues))
                                )
                            )
                        }
                    }
                    XMeasurement.DISTANCE -> MonitoringChart.create(
                        getMonitoringByDistance(speedValues.values, distanceValues),
                        listOf(
                            Pair("max", getMax(info.session?.maxSpeed?.let { it * 3.6f }, speedValues.values, distanceValues)),
                            Pair("avg", getAvg(info.session?.avgSpeed?.let { it * 3.6f }, speedValues.values, distanceValues))
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
                                    Pair("max", getMax(info.session?.maxHeartRate?.toFloat(), heartValues.values, distanceValues)),
                                    Pair("avg", getAvg(info.session?.avgHeartRate?.toFloat(), heartValues.values, distanceValues))
                                )
                            )
                            XPause.HIDE -> MonitoringChart.create(
                                getRemovePauseByTime(heartValues.values, distanceValues),
                                listOf(
                                    Pair("max", getMax(info.session?.maxHeartRate?.toFloat(), heartValues.values, distanceValues)),
                                    Pair("avg", getAvg(info.session?.avgHeartRate?.toFloat(), heartValues.values, distanceValues))
                                )
                            )
                        }
                    }
                    XMeasurement.DISTANCE -> MonitoringChart.create(
                        getMonitoringByDistance(heartValues.values, distanceValues),
                        listOf(
                            Pair("max", getMax(info.session?.maxHeartRate?.toFloat(), heartValues.values, distanceValues)),
                            Pair("avg", getAvg(info.session?.avgHeartRate?.toFloat(), heartValues.values, distanceValues))
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
                                    Pair("max", getMax(info.session?.maxCadence?.toFloat(), cadenceValues.values, distanceValues)),
                                    Pair("avg", getAvg(info.session?.avgCadence?.toFloat(), cadenceValues.values, distanceValues))
                                )
                            )
                            XPause.HIDE -> MonitoringChart.create(
                                getRemovePauseByTime(cadenceValues.values, distanceValues),
                                listOf(
                                    Pair("max", getMax(info.session?.maxCadence?.toFloat(), cadenceValues.values, distanceValues)),
                                    Pair("avg", getAvg(info.session?.avgCadence?.toFloat(), cadenceValues.values, distanceValues))
                                )
                            )
                        }
                    }
                    XMeasurement.DISTANCE -> MonitoringChart.create(
                        getMonitoringByDistance(cadenceValues.values, distanceValues),
                        listOf(
                            Pair("max", getMax(info.session?.maxCadence?.toFloat(), cadenceValues.values, distanceValues)),
                            Pair("avg", getAvg(info.session?.avgCadence?.toFloat(), cadenceValues.values, distanceValues))
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
                                    Pair("max", getMax(info.session?.maxPosGrade, gradeValues.values, distanceValues)),
                                    Pair("min", getMin(info.session?.maxNegGrade, gradeValues.values, distanceValues))
                                )
                            )
                            XPause.HIDE -> MonitoringChart.create(
                                getRemovePauseByTime(gradeValues.values, distanceValues),
                                listOf(
                                    Pair("max", getMax(info.session?.maxPosGrade, gradeValues.values, distanceValues)),
                                    Pair("min", getMin(info.session?.maxNegGrade, gradeValues.values, distanceValues))
                                )
                            )
                        }
                    }
                    XMeasurement.DISTANCE -> MonitoringChart.create(
                        getMonitoringByDistance(gradeValues.values, distanceValues),
                        listOf(
                            Pair("max", getMax(info.session?.maxPosGrade, gradeValues.values, distanceValues)),
                            Pair("min", getMin(info.session?.maxNegGrade, gradeValues.values, distanceValues))
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
                                    Pair("min", getMin(info.session?.minAltitude, altitudeValues.values, distanceValues)),
                                    Pair("max", getMax(info.session?.maxAltitude, altitudeValues.values, distanceValues)),
                                    Pair("avg", getAvg(info.session?.avgAltitude, altitudeValues.values, distanceValues))
                                )
                            )
                            XPause.HIDE -> MonitoringChart.create(
                                getRemovePauseByTime(altitudeValues.values, distanceValues),
                                listOf(
                                    Pair("min", getMin(info.session?.minAltitude, altitudeValues.values, distanceValues)),
                                    Pair("max", getMax(info.session?.maxAltitude, altitudeValues.values, distanceValues)),
                                    Pair("avg", getAvg(info.session?.avgAltitude, altitudeValues.values, distanceValues))
                                )
                            )
                        }
                    }
                    XMeasurement.DISTANCE -> MonitoringChart.create(
                        getMonitoringByDistance(altitudeValues.values, distanceValues),
                        listOf(
                            Pair("min", getMin(info.session?.minAltitude, altitudeValues.values, distanceValues)),
                            Pair("max", getMax(info.session?.maxAltitude, altitudeValues.values, distanceValues)),
                            Pair("avg", getAvg(info.session?.avgAltitude, altitudeValues.values, distanceValues))
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
                                    Pair("min", getMin(info.session?.minTemperature?.toFloat(), temperatureValues.values, distanceValues)),
                                    Pair("max", getMax(info.session?.maxTemperature?.toFloat(), temperatureValues.values, distanceValues)),
                                    Pair("avg", getAvg(info.session?.avgTemperature?.toFloat(), temperatureValues.values, distanceValues))
                                )
                            )
                            XPause.HIDE -> MonitoringChart.create(
                                getRemovePauseByTime(temperatureValues.values, distanceValues),
                                listOf(
                                    Pair("min", getMin(info.session?.minTemperature?.toFloat(), temperatureValues.values, distanceValues)),
                                    Pair("max", getMax(info.session?.maxTemperature?.toFloat(), temperatureValues.values, distanceValues)),
                                    Pair("avg", getAvg(info.session?.avgTemperature?.toFloat(), temperatureValues.values, distanceValues))
                                )
                            )
                        }
                    }
                    XMeasurement.DISTANCE -> MonitoringChart.create(
                        getMonitoringByDistance(temperatureValues.values, distanceValues),
                        listOf(
                            Pair("min", getMin(info.session?.minTemperature?.toFloat(), temperatureValues.values, distanceValues)),
                            Pair("max", getMax(info.session?.maxTemperature?.toFloat(), temperatureValues.values, distanceValues)),
                            Pair("avg", getAvg(info.session?.avgTemperature?.toFloat(), temperatureValues.values, distanceValues))
                        )
                    )
                }
            }
        }
    }

    private fun getMax(value: Float?, monitoringValues: List<Float>, distanceValues: List<Int>): Float {
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

    private fun getAvg(value: Float?, monitoringValues: List<Float>, distanceValues: List<Int>): Float {
        if(value != null) return value
        var start = 0
        return monitoringValues.filterIndexed { idx, itm ->
            if(distanceValues[idx] > start) {
                start = distanceValues[idx]
                true
            }
            else false
        }.average().toFloat()
    }

    private fun getMin(value: Float?, monitoringValues: List<Float>, distanceValues: List<Int>): Float {
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

    private fun getMonitoringByDistance(monitoringValues: List<Float>, distanceValues: List<Int>): List<Float>{
        return interpolateY(monitoringValues, distanceValues, min(yInterCount, distanceValues.size))
    }

    private fun getRemovePauseByTime(monitoringValues: List<Float>, distanceValues: List<Int>): List<Float>{
        var previousDistance = 0
        return monitoringValues.filterIndexed { idx, itm -> (distanceValues[idx]!=0 && distanceValues[idx]!=previousDistance).also { previousDistance = distanceValues[idx] } }
    }

    override fun onMesg(p0: RecordMesg?) {
        p0?.let{
            val x = (it.timestamp.timestamp - info.timestampStart).toInt()
            //if(x < 1000)
            //    Log.i("cycoffline1", "${x.toString()} ${it.distance}")
            distanceValues[x] = (it.distance ?: 0f).roundToInt()
            if (it.speed != null) {
                speedValues.values[x] = it.speed * 3.6f
                speedValues.loaded = true
            }
            if (it.heartRate != null && it.heartRate != 0.toShort()) {
                heartValues.values[x] = it.heartRate.toFloat()
                heartValues.loaded = true
            }

            if (it.cadence != null) {
                cadenceValues.values[x] = it.cadence.toFloat()
                cadenceValues.loaded = true
            }
            if (it.altitude != null) {
                altitudeValues.values[x] = it.altitude.toFloat()
                altitudeValues.loaded = true
            }
            if (it.grade != null) {
                gradeValues.values[x] = it.grade.toFloat()
                gradeValues.loaded = true
            }
            if (it.temperature != null) {
                temperatureValues.values[x] = it.temperature.toFloat()
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
                info.inZone(timeByHeart.xValues[xIdx].toShort())
            }.sum() )
        }
    }
}
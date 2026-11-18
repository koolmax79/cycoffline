package ru.koolmax.cycoffline.data.media

import android.util.Log
import androidx.collection.FloatList
import androidx.collection.MutableFloatList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.fastForEachIndexed
import com.garmin.fit.RecordMesg
import com.garmin.fit.RecordMesgListener
import ru.koolmax.cycoffline.data.HeartZone
import ru.koolmax.cycoffline.data.HeartZoneInfo
import ru.koolmax.cycoffline.data.db.FitSessionItem
import ru.koolmax.cycoffline.presentation.ui.fullDefaultVal
import ru.koolmax.cycoffline.presentation.ui.interpolateX
import ru.koolmax.cycoffline.presentation.ui.interpolateY
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.ChartData
import kotlin.collections.listOf
import kotlin.math.min
import kotlin.math.roundToInt


enum class FitListType { SPEED, POWER, HEART, CADENCE, GRADE, ALTITUDE, TEMPERATURE
    //, HEART_TIME
}
enum class XMeasurement { DISTANCE, TIME }
enum class XPause { SHOW, HIDE }

data class MonitoringChart(val min: Float, val max: Float, val yValues: FloatArray, val info: List<Pair<String, Float>>) {
    companion object {
        fun create(yValues: FloatArray, info: List<Pair<String, Float>>) = MonitoringChart(yValues.min(), yValues.max(), yValues, info)
    }
}
data class MonitoringData(val chartData: Map<FitListType, MonitoringChart> = mapOf(), val xValues: IntArray = intArrayOf())
data class Zone(val begin: Int, val end: Int, val zoneInfo: HeartZoneInfo, var heartSum: Int=0)
{
    fun addHeart(v: Int) {
        this.heartSum += v
    }
}

data class TrackPause(val time: Int, val duration: Int)

private const val yInterCount = 1000
class FitFile(val info: FitInfo = FitInfo()): RecordMesgListener {

    private data class MonitoringValues(var values: FloatArray, var loaded: Boolean = false)

    val timeByMonitoring: List<Pair<FitListType, ChartData>> by lazy {
        val list = mutableListOf<Pair<FitListType, ChartData>>()
        if(heartValues.loaded)
            list.add(Pair(FitListType.HEART, timeByHeart))
        if(powerValues.loaded)
            list.add(Pair(FitListType.POWER, timeByPower))
        if(speedValues.loaded)
            list.add(Pair(FitListType.SPEED, getTimeByMonitoring(speedValues.values)))
        if(cadenceValues.loaded)
            list.add(Pair(FitListType.CADENCE, getTimeByMonitoring(cadenceValues.values)))
        list
    }

    fun getPauseList(filter: Int = 60): List<TrackPause> {
        check(positionLatValues.values.size == positionLongValues.values.size)

        val list = mutableListOf<TrackPause>()
        var durationPause = -1
        if(positionLatValues.loaded && positionLongValues.loaded) {
            var startPauseLat = positionLatValues.values[0]
            var startPauseLong = positionLongValues.values[0]

            for (idx in positionLatValues.values.indices) {
                val lat = positionLatValues.values[idx]
                val long = positionLongValues.values[idx]

                if (startPauseLat == lat && startPauseLong == long) {
                    durationPause++
                } else {
                    if(durationPause > filter) {
                        list.add(TrackPause(idx - durationPause, durationPause))
                    }
                    startPauseLat = lat
                    startPauseLong = long
                    durationPause = 0
                }
            }
        }
        return list
    }

    private val timeByHeart by lazy {
        getTimeByMonitoring(heartValues.values)
    }

    private val timeByPower by lazy {
        getTimeByMonitoring(powerValues.values)
    }

    private fun getTimeByMonitoring(monitoringValues: FloatArray): ChartData {
        val valuesWithoutPause = getRemovePauseByTime(monitoringValues, distanceValues).filter { it != 0f } //.run { if(removeZero) this.filter { it == 0.0 } else this }
        val xMin = valuesWithoutPause.min().roundToInt()
        val xMax = valuesWithoutPause.max().roundToInt()
        //Log.i("cycoffline1", "${xMin} : ${xMax}")
        //Log.i("cycoffline1", "${monitoringValues.min()} : ${monitoringValues.max()}")

        val yValues = IntArray(xMax - xMin + 1)
        valuesWithoutPause.forEach {
            val value = it.roundToInt() - xMin
            yValues[value] = yValues[value] + 1
        }

        //if(yValues.size > (180-xMin))
        //    for (x in  xMin..xMax step 5)
        //        Log.i("cycoffline1", "${x+xMin} : ${yValues[x]}")

        return ChartData.create(yValues = yValues, xMin = xMin)
    }

    private var distanceValues     = IntArray(info.timeCount)
    private var speedValues        = MonitoringValues(FloatArray(info.timeCount))
    private var powerValues        = MonitoringValues(FloatArray(info.timeCount))
    private var heartValues        = MonitoringValues(FloatArray(info.timeCount))
    private var cadenceValues      = MonitoringValues(FloatArray(info.timeCount))
    private var gradeValues        = MonitoringValues(FloatArray(info.timeCount))
    private var altitudeValues     = MonitoringValues(FloatArray(info.timeCount))
    private var temperatureValues  = MonitoringValues(FloatArray(info.timeCount))
    private var positionLatValues  = MonitoringValues(FloatArray(info.timeCount) { 360f })
    private var positionLongValues = MonitoringValues(FloatArray(info.timeCount) { 360f })

    //val latValues: List<Float> get() = if(positionLatValues.values.isNotEmpty()) positionLatValues.values.runningFold(positionLatValues.values.first()) { previous, current -> if(current != 360f) current else previous } else listOf()
    //val longValues: List<Float> get() = if(positionLongValues.values.isNotEmpty()) positionLongValues.values.runningFold(positionLongValues.values.first()) { previous, current -> if(current != 360f) current else previous } else listOf()
    val latValues: FloatArray get() = if(positionLatValues.loaded) positionLatValues.values else FloatArray(0)
    val longValues: FloatArray get() = if(positionLongValues.loaded) positionLongValues.values else FloatArray(0)

    private val monitoringList = listOf( Pair(FitListType.SPEED, speedValues),
        Pair(FitListType.HEART, heartValues),
        Pair(FitListType.POWER, powerValues),
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

    private fun getXValues(xMeasurement: XMeasurement, pause: XPause): IntArray {
        when (xMeasurement) {
            XMeasurement.TIME -> {
                when (pause) {
                    XPause.SHOW -> return distanceValues.indices.toList().toIntArray()
                    XPause.HIDE -> {
                        var previousDistance = 0
                        return distanceValues.indices.filter {idx -> (distanceValues[idx]!=0 && distanceValues[idx]!=previousDistance).also { previousDistance = distanceValues[idx] } }.toIntArray()
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
                val values = getRemovePauseByTime(speedValues.values, distanceValues)
                when (xMeasurement) {
                    XMeasurement.TIME -> {
                        when(pause) {
                            XPause.SHOW ->
                                MonitoringChart.create(
                                speedValues.values,
                                listOf(
                                    Pair("max", info.session?.maxSpeed?.let { it * 3.6f } ?: values.max()),
                                    Pair("avg", info.session?.avgSpeed?.let { it * 3.6f } ?: values.average().toFloat())
                                )
                            )
                            XPause.HIDE -> MonitoringChart.create(
                                values,
                                listOf(
                                    Pair("max", info.session?.maxSpeed?.let { it * 3.6f } ?: values.max()),
                                    Pair("avg", info.session?.avgSpeed?.let { it * 3.6f } ?: values.average().toFloat())
                                )
                            )
                        }
                    }
                    XMeasurement.DISTANCE -> MonitoringChart.create(
                        getMonitoringByDistance(speedValues.values, distanceValues),
                        listOf(
                            Pair("max", info.session?.maxSpeed?.let { it * 3.6f } ?: values.max()),
                            Pair("avg", info.session?.avgSpeed?.let { it * 3.6f } ?: values.average().toFloat())
                        )
                    )
                }
            }
            FitListType.POWER -> {
                val values = getRemovePauseByTime(powerValues.values, distanceValues)
                when (xMeasurement) {
                    XMeasurement.TIME -> {
                        when (pause) {
                            XPause.SHOW -> MonitoringChart.create(
                                powerValues.values,
                                listOf(
                                    Pair("max", info.session?.maxPower?.toFloat() ?: values.max()),
                                    Pair("avg", info.session?.avgPower?.toFloat() ?: values.average().toFloat())
                                )
                            )
                            XPause.HIDE -> MonitoringChart.create(
                                values,
                                listOf(
                                    Pair("max", info.session?.maxPower?.toFloat() ?: values.max()),
                                    Pair("avg", info.session?.avgPower?.toFloat() ?: values.average().toFloat())
                                )
                            )
                        }
                    }
                    XMeasurement.DISTANCE -> MonitoringChart.create(
                        getMonitoringByDistance(powerValues.values, distanceValues),
                        listOf(
                            Pair("max", info.session?.maxPower?.toFloat() ?: values.max()),
                            Pair("avg", info.session?.avgPower?.toFloat() ?: values.average().toFloat())
                        )
                    )
                }
            }
            FitListType.HEART -> {
                val values = getRemovePauseByTime(heartValues.values, distanceValues)
                when (xMeasurement) {
                    XMeasurement.TIME -> {
                        when (pause) {
                            XPause.SHOW -> MonitoringChart.create(
                                heartValues.values,
                                listOf(
                                    Pair("max", info.session?.maxHeartRate?.toFloat() ?: values.max()),
                                    Pair("avg", info.session?.avgHeartRate?.toFloat() ?: values.average().toFloat())
                                )
                            )
                            XPause.HIDE -> MonitoringChart.create(
                                values,
                                listOf(
                                    Pair("max", info.session?.maxHeartRate?.toFloat() ?: values.max()),
                                    Pair("avg", info.session?.avgHeartRate?.toFloat() ?: values.average().toFloat())
                                )
                            )
                        }
                    }
                    XMeasurement.DISTANCE -> MonitoringChart.create(
                        getMonitoringByDistance(heartValues.values, distanceValues),
                        listOf(
                            Pair("max", info.session?.maxHeartRate?.toFloat() ?: values.max()),
                            Pair("avg", info.session?.avgHeartRate?.toFloat() ?: values.average().toFloat())
                        )
                    )
                }
            }
            FitListType.CADENCE -> {
                val values = getRemovePauseByTime(cadenceValues.values, distanceValues)
                when (xMeasurement) {
                    XMeasurement.TIME -> {
                        when (pause) {
                            XPause.SHOW -> MonitoringChart.create(
                                cadenceValues.values,
                                listOf(
                                    Pair("max", info.session?.maxCadence?.toFloat() ?: values.max()),
                                    Pair("avg", info.session?.avgCadence?.toFloat() ?: values.average().toFloat())
                                )
                            )
                            XPause.HIDE -> MonitoringChart.create(
                                values,
                                listOf(
                                    Pair("max", info.session?.maxCadence?.toFloat() ?: values.max()),
                                    Pair("avg", info.session?.avgCadence?.toFloat() ?: values.average().toFloat())
                                )
                            )
                        }
                    }
                    XMeasurement.DISTANCE -> MonitoringChart.create(
                        getMonitoringByDistance(cadenceValues.values, distanceValues),
                        listOf(
                            Pair("max", info.session?.maxCadence?.toFloat() ?: values.max()),
                            Pair("avg", info.session?.avgCadence?.toFloat() ?: values.average().toFloat())
                        )
                    )
                }
            }
            FitListType.GRADE -> {
                val values = getRemovePauseByTime(gradeValues.values, distanceValues)
                when(xMeasurement) {
                    XMeasurement.TIME -> {
                        when (pause) {
                            XPause.SHOW -> MonitoringChart.create(
                                gradeValues.values,
                                listOf(
                                    Pair("max", info.session?.maxPosGrade ?: values.max()),
                                    Pair("min", info.session?.maxNegGrade ?: values.min())
                                )
                            )
                            XPause.HIDE -> MonitoringChart.create(
                                values,
                                listOf(
                                    Pair("max", info.session?.maxPosGrade ?: values.max()),
                                    Pair("min", info.session?.maxNegGrade ?: values.min())
                                )
                            )
                        }
                    }
                    XMeasurement.DISTANCE -> MonitoringChart.create(
                        getMonitoringByDistance(gradeValues.values, distanceValues),
                        listOf(
                            Pair("max", info.session?.maxPosGrade ?: values.max()),
                            Pair("min", info.session?.maxNegGrade ?: values.min())
                        )
                    )
                }
            }
            FitListType.ALTITUDE -> {
                val values = getRemovePauseByTime(altitudeValues.values, distanceValues)
                when(xMeasurement) {
                    XMeasurement.TIME -> {
                        when (pause) {
                            XPause.SHOW -> MonitoringChart.create(
                                altitudeValues.values,
                                listOf(
                                    Pair("min", info.session?.minAltitude ?: values.min()),
                                    Pair("max", info.session?.maxAltitude ?: values.max()),
                                    Pair("avg", info.session?.avgAltitude ?: values.average().toFloat())
                                )
                            )
                            XPause.HIDE -> MonitoringChart.create(
                                values,
                                listOf(
                                    Pair("min", info.session?.minAltitude ?: values.min()),
                                    Pair("max", info.session?.maxAltitude ?: values.max()),
                                    Pair("avg", info.session?.avgAltitude ?: values.average().toFloat())
                                )
                            )
                        }
                    }
                    XMeasurement.DISTANCE -> MonitoringChart.create(
                        getMonitoringByDistance(altitudeValues.values, distanceValues),
                        listOf(
                            Pair("min", info.session?.minAltitude ?: values.min()),
                            Pair("max", info.session?.maxAltitude ?: values.max()),
                            Pair("avg", info.session?.avgAltitude ?: values.average().toFloat())
                        )
                    )
                }
            }
            FitListType.TEMPERATURE -> {
                val values = getRemovePauseByTime(temperatureValues.values, distanceValues)
                when(xMeasurement) {
                    XMeasurement.TIME -> {
                        when (pause) {
                            XPause.SHOW -> MonitoringChart.create(
                                temperatureValues.values,
                                listOf(
                                    Pair("min", info.session?.minTemperature?.toFloat() ?: values.min()),
                                    Pair("max", info.session?.maxTemperature?.toFloat() ?: values.max()),
                                    Pair("avg", info.session?.avgTemperature?.toFloat() ?: values.average().toFloat())
                                )
                            )
                            XPause.HIDE -> MonitoringChart.create(
                                values,
                                listOf(
                                    Pair("min", info.session?.minTemperature?.toFloat() ?: values.min()),
                                    Pair("max", info.session?.maxTemperature?.toFloat() ?: values.max()),
                                    Pair("avg", info.session?.avgTemperature?.toFloat() ?: values.average().toFloat())
                                )
                            )
                        }
                    }
                    XMeasurement.DISTANCE -> MonitoringChart.create(
                        getMonitoringByDistance(temperatureValues.values, distanceValues),
                        listOf(
                            Pair("min", info.session?.minTemperature?.toFloat() ?: values.min()),
                            Pair("max", info.session?.maxTemperature?.toFloat() ?: values.max()),
                            Pair("avg", info.session?.avgTemperature?.toFloat() ?: values.average().toFloat())
                        )
                    )
                }
            }
        }
    }

    private fun getMonitoringByDistance(monitoringValues: FloatArray, distanceValues: IntArray): FloatArray{
        return interpolateY(monitoringValues, distanceValues, min(yInterCount, distanceValues.size))
    }

    private fun getRemovePauseByTime(monitoringValues: FloatArray, distanceValues: IntArray): FloatArray{
        var previousDistance = 0
        return monitoringValues.filterIndexed { idx, itm -> (distanceValues[idx]!=0 && distanceValues[idx]!=previousDistance).also {
            previousDistance = distanceValues[idx]
        } }.toFloatArray()
    }

    override fun onMesg(p0: RecordMesg?) {
        p0?.let{
            val x = (it.timestamp.timestamp - info.timestampStart).toInt()
            distanceValues[x] = (it.distance ?: 0f).roundToInt()
            if (it.speed != null) {
                speedValues.values[x] = it.speed * 3.6f
            }

            if (it.power != null) {
                powerValues.values[x] = it.power.toFloat()
            }

            if (it.heartRate != null && it.heartRate != 0.toShort()) {
                heartValues.values[x] = it.heartRate.toFloat()
            }

            if (it.cadence != null) {
                cadenceValues.values[x] = it.cadence.toFloat()
            }
            if (it.altitude != null) {
                altitudeValues.values[x] = it.altitude.toFloat()
            }
            if (it.grade != null) {
                gradeValues.values[x] = it.grade.toFloat()
            }
            if (it.temperature != null) {
                temperatureValues.values[x] = it.temperature.toFloat()
            }

            if(it.positionLat != null) {
                positionLatValues.values[x] = it.positionLat / 10000000f
            }
            if(it.positionLong != null) {
                positionLongValues.values[x] = it.positionLong / 10000000f
            }
        }
    }

    fun endLoad() {
        speedValues.loaded = speedValues.values.any { it != 0f }
        powerValues.loaded = powerValues.values.any { it != 0f }
        heartValues.loaded = heartValues.values.any { it != 0f }
        cadenceValues.loaded = cadenceValues.values.any { it != 0f }
        altitudeValues.loaded = altitudeValues.values.any { it != 0f }
        gradeValues.loaded = gradeValues.values.any { it != 0f }
        temperatureValues.loaded = temperatureValues.values.any { it != 0f }
        positionLatValues.loaded = positionLatValues.values.any { it != 360f }
        positionLongValues.loaded = positionLongValues.values.any { it != 360f }
        positionLatValues.values.fullDefaultVal(360f)
        positionLongValues.values.fullDefaultVal(360f)

        //info.session?.let {
        //    if(it.totalMovingTime==null) {
        //        it.totalMovingTime = it.totalElapsedTime
                //Log.i("cycoffline1", "endload")
        //    }
        //}

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

    fun init(session: FitSessionItem) {
        if(session.totalMovingTime==null) {
            session.totalMovingTime = session.totalElapsedTime
        }
        if(session.maxSpeed==null && speedValues.loaded) {
            session.maxSpeed = speedValues.values.max().toDouble()
        }
    }

    fun getHeartZone(heartZone: HeartZone): List<Zone> {
        return heartZone.list.map { info -> Zone(info.min, info.max, info,
            timeByHeart.yValues.filterIndexed { xIdx, y ->
                info.inZone(timeByHeart.xValues[xIdx].toShort())
            }.sum() )
        }
    }
}
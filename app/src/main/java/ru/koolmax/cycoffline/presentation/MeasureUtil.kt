package ru.koolmax.cycoffline.presentation

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.jetbrains.annotations.Range
import ru.koolmax.cycoffline.R
import ru.koolmax.cycoffline.data.media.FitListType
import ru.koolmax.cycoffline.data.media.XMeasurement
import ru.koolmax.cycoffline.presentation.ui.statistics.ChartType
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.Locale

@SuppressLint("LocalContextConfigurationRead")
fun getCurrentLocale(context: Context): Locale {
    val configuration = context.resources.configuration
    return configuration.locales[0]
}

class MeasureUtil {
    companion object {
        const val distanceUnit = "км"
        const val ascentUnit = "м"
        const val speedUnit = "км/ч"
        const val heartRateUnit = "уд/м"
        const val cadenceRateUnit = "об/м"

        private val empty = Pair("-", "")
        private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
        private val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        private val week = listOf("пн", "вт", "ср", "чт", "пт", "сб", "вс")
        private val month = listOf("январь", "февраль", "март", "апрель", "май", "июнь", "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь")

        fun getWeek(v: @Range(from=1, to=7) Int) = week[v-1]

        fun getMonth(v: @Range(from=1, to=12) Int): String {
            return month[v - 1]
            //Month.of(v).getDisplayName(java.time.format.TextStyle.FULL_STANDALONE, java.util.Locale("ru"))
        }

        fun getYearMonth(v: LocalDateTime?): String {
            return if (v != null) "${v.year} ${getMonth(v.month.value)}" else "-"
        }

        fun getDateTime(v: LocalDateTime?): String {
            return if (v != null) v.format(dateTimeFormat) else "-"
        }

        fun getTime(v: LocalDateTime?): String {
            return if (v != null) v.format(timeFormat) else "-"
        }

        fun getDuration(v: Int?): String {
            return if (v != null) String.format("%d:%02d:%02d", v / 3600, (v / 60) % 60, (v % 60)) else "-"
        }

        fun getDurationShort(v: Int?): String {
            return if (v != null) String.format("%d:%02d", v / 3600, (v / 60) % 60) else "-"
        }

        fun getDistance(v: Int?, format: String = "%.2f"): Pair<String, String> {
            return if (v != null)
                if (v < 1000) Pair("$v", ascentUnit) else Pair(String.format(format, v.toFloat() / 1000f), distanceUnit)
            else empty
        }

        fun getAscent(v: Int?): Pair<String, String> {
            return if (v != null)
                Pair("$v", ascentUnit)
            else empty
        }

        fun getSpeed(v: Float?, format: String = "%.1f"): Pair<String, String> {
            return if (v != null) Pair(String.format(format, v), speedUnit) else empty
        }

        fun getHeartRate(v: Int?): Pair<String, String> {
            return if (v != null) Pair("$v", heartRateUnit) else empty
        }

        fun getCadence(v: Int?): Pair<String, String> {
            return if (v != null) Pair("$v", cadenceRateUnit) else empty
        }

        fun getGrade(v: Float?, format: String = "%.1f"): Pair<String, String> {
            return if (v != null) Pair(String.format(format, v), "%") else empty
        }

        fun getPercent(v: Float?): Pair<String, String> {
            return if (v != null) Pair(String.format("%.0f", v), "%") else empty
        }

        fun getTemperature(v: Int?): Pair<String, String> {
            return if (v != null) Pair("$v", "°") else empty
        }

        fun getHeartRate(min: Int, max: Int): String {
            if(min!=0 && max!=0) return "$min - $max"
            if(min==0 && max!=0) return "< $max"
            if(min!=0 && max==0) return "> $min"
            return ""
        }

        fun getFileSize(v: Int): String {
            return String.format("%.1fk", v.toFloat() / 1000f)
        }
    }
}

fun Pair<String, String>.pairToString() = "${this.first} ${this.second}"

fun getText(value: Int, measurement: XMeasurement, showMeasurement: Boolean = true) = when(measurement) {
    XMeasurement.DISTANCE -> MeasureUtil.getDistance(value).run { if(showMeasurement) pairToString() else this.first }
    XMeasurement.TIME -> MeasureUtil.getDuration(value)
}

fun getTextForChart(value: Int, measurement: XMeasurement) = when(measurement) {
    XMeasurement.DISTANCE -> MeasureUtil.getDistance(value, "%.0f").first
    XMeasurement.TIME -> MeasureUtil.getDurationShort(value)
}

fun getTextForChart(value: Float, type: ChartType, showMeasurement: Boolean = false) = when(type) {
    ChartType.DISTANCE -> MeasureUtil.getDistance(value.toInt(), "%.0f").run { if(showMeasurement) this.pairToString() else this.first }
    ChartType.AVG_HEART_RATE -> MeasureUtil.getHeartRate(value.toInt()).run { if(showMeasurement) this.pairToString() else this.first }
    ChartType.AVG_SPEED -> MeasureUtil.getSpeed(value, "%.0f").run { if(showMeasurement) this.pairToString() else this.first }
    ChartType.ASCENT -> MeasureUtil.getDistance(value.toInt(), "%.0f").run { if(showMeasurement) this.pairToString() else this.first }
    ChartType.MOVING_TIME -> MeasureUtil.getDurationShort(value.toInt())
    ChartType.MAX_HEART_RATE -> MeasureUtil.getHeartRate(value.toInt()).run { if(showMeasurement) this.pairToString() else this.first }
    ChartType.NONE -> ""
}

fun getTextForChart(value: Int, type: ChartType, showMeasurement: Boolean = false) = when(type) {
    ChartType.DISTANCE -> MeasureUtil.getDistance(value, "%.0f").run { if(showMeasurement) this.pairToString() else this.first }
    ChartType.AVG_HEART_RATE -> MeasureUtil.getHeartRate(value).run { if(showMeasurement) this.pairToString() else this.first }
    ChartType.AVG_SPEED -> MeasureUtil.getSpeed(value.toFloat(), "%.0f").run { if(showMeasurement) this.pairToString() else this.first }
    ChartType.ASCENT -> MeasureUtil.getDistance(value, "%.0f").run { if(showMeasurement) this.pairToString() else this.first }
    ChartType.MOVING_TIME -> MeasureUtil.getDurationShort(value)
    ChartType.MAX_HEART_RATE -> MeasureUtil.getHeartRate(value).run { if(showMeasurement) this.pairToString() else this.first }
    ChartType.NONE -> ""
}

fun getTextForChart(value: Float, type: FitListType) = when(type) {
    FitListType.SPEED -> MeasureUtil.getSpeed(value, "%.0f")
    FitListType.HEART -> MeasureUtil.getHeartRate(value.toInt())
    FitListType.CADENCE -> MeasureUtil.getCadence(value.toInt())
    FitListType.GRADE -> MeasureUtil.getGrade(value, "%.0f")
    FitListType.ALTITUDE -> MeasureUtil.getDistance(value.toInt(), "%.0f")
    FitListType.TEMPERATURE -> MeasureUtil.getTemperature(value.toInt())
}.first


fun getText(value: Float, type: FitListType, showMeasurement: Boolean = true) = when(type) {
    FitListType.SPEED -> MeasureUtil.getSpeed(value)
    FitListType.HEART -> MeasureUtil.getHeartRate(value.toInt())
    FitListType.CADENCE -> MeasureUtil.getCadence(value.toInt())
    FitListType.GRADE -> MeasureUtil.getGrade(value)
    FitListType.ALTITUDE -> MeasureUtil.getDistance (value.toInt())
    FitListType.TEMPERATURE -> MeasureUtil.getTemperature(value.toInt())
}.run { if(showMeasurement) this.pairToString() else this.first }

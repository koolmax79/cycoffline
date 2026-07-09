package ru.koolmax.cycoffline.data.db

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.garmin.fit.SessionMesg
import com.garmin.fit.SessionMesgListener
import java.time.LocalDateTime
import java.time.ZoneId

@Entity(tableName = "session")
@TypeConverters(FitConverter::class)
data class FitSessionItem(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name="file_name")
    var fileName: String = "",
    @ColumnInfo(name="displayed", defaultValue = "0")
    var displayed: Int = 0,
    @ColumnInfo(name="avg_heart_rate")
    var avgHeartRate: Int?=null,
    @ColumnInfo(name="avg_speed")
    var avgSpeed: Double?=null,
    @ColumnInfo(name="max_altitude")
    var maxAltitude: Double?=null,
    @ColumnInfo(name="max_heart_rate")
    var maxHeartRate: Int?=null,
    @ColumnInfo(name="max_neg_grade")
    var maxNegGrade: Double?=null,
    @ColumnInfo(name="max_pos_grade")
    var maxPosGrade: Double?=null,
    @ColumnInfo(name="max_speed")
    var maxSpeed: Double?=null,
    @ColumnInfo(name="min_heart_rate")
    var minHeartRate: Int?=null,
    @ColumnInfo(name="start_time")
    var startTime: LocalDateTime?=null,
    @ColumnInfo(name="total_ascent")
    var totalAscent: Int?=null,
    @ColumnInfo(name="total_descent")
    var totalDescent: Int?=null,
    @ColumnInfo(name="total_distance")
    var totalDistance: Int?=null,
    @ColumnInfo(name="total_elapsed_time")
    var totalElapsedTime: Int?=null,
    @ColumnInfo(name="total_moving_time")
    var totalMovingTime: Int?=null
): SessionMesgListener {

    val isDisplayed get() = displayed !=0

    override fun onMesg(p0: SessionMesg?) {
        if (p0 == null) return
        startTime = LocalDateTime.ofInstant(p0.startTime.date.toInstant(), ZoneId.systemDefault())
        totalElapsedTime = p0.totalElapsedTime?.toInt()
        totalMovingTime = p0.totalMovingTime?.toInt()
        totalDistance = p0.totalDistance?.toInt()
        avgSpeed = p0.avgSpeed.toDouble() * 3.6f
        maxSpeed = p0.maxSpeed.toDouble() * 3.6f
        minHeartRate = p0.minHeartRate?.toInt()
        maxHeartRate = p0.maxHeartRate?.toInt()
        avgHeartRate = p0.avgHeartRate?.toInt()
        maxPosGrade = p0.maxPosGrade?.toDouble()
        maxNegGrade = p0.maxNegGrade?.toDouble()
        totalAscent = p0.totalAscent
        totalDescent = p0.totalDescent
        maxAltitude = p0.maxAltitude?.toDouble()
    }
}
package ru.koolmax.cycoffline.data.db

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class FitConverter {
    @TypeConverter
    fun toInt(v: LocalDateTime): Int {
        return v.toEpochSecond(ZoneOffset.UTC).toInt()
    }

    @TypeConverter
    fun toLocalDateTime(v: Int): LocalDateTime {
        return LocalDateTime.ofEpochSecond(v.toLong(), 0, ZoneOffset.UTC)
    }
}

fun LocalDate.toLocalDateTime(): LocalDateTime {
    return java.time.LocalDate.of(this.year, this.monthValue, this.dayOfMonth).atStartOfDay()
}
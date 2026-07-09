package ru.koolmax.cycoffline.presentation

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Date
import java.util.Locale

class DateTimeUtil {
    companion object {
        fun convertMillisecondToDateStr(millis: Long): String {
            val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            return formatter.format(Date(millis))
        }

        fun millisecondsToLocalDate(utcMilliseconds: Long): LocalDate {
            val instant = Instant.ofEpochMilli(utcMilliseconds)
            val zoneId = ZoneId.systemDefault() // or specify a particular zone, e.g., ZoneId.of("UTC")
            return instant.atZone(zoneId).toLocalDate()
        }

        fun LocalDateToMilliseconds(date: LocalDate): Long {
            val zonedDateTime = date.atStartOfDay()
            return zonedDateTime.toInstant(ZoneOffset.UTC).toEpochMilli()
        }
    }
}
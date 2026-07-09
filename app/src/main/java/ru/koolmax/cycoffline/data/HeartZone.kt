package ru.koolmax.cycoffline.data

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.garmin.fit.Gender
import ru.koolmax.cycoffline.R
import ru.koolmax.cycoffline.ui.theme.LocalCustomColorsPalette
import java.time.LocalDate
import java.time.Period

class HeartZone(val list: List<HeartZoneInfo>, val maxHR: Int, val defaultMaxHR: Int) {
    companion object {
        fun make(date: LocalDate, gender: Gender): HeartZone {
            return make(date, gender, calcMaxHR(date, gender))
        }

        fun make(date: LocalDate, gender: Gender, maxHR: Int): HeartZone {
            val list = MutableList(5) { HeartZoneInfo(it + 1) }
            val step = 0.1f
            for ((idx, info) in list.withIndex()) {
                info.min = (maxHR * (info.idx * step + 0.4f)).toInt()
                val previousIdx = idx - 1
                if(previousIdx >= 0)
                    list[previousIdx].max = info.min - 1
            }
            list[0].min = Int.MIN_VALUE
            list[4].max = Int.MAX_VALUE

            return HeartZone(list, maxHR, calcMaxHR(date, gender))
        }

        fun getAge(date: LocalDate) = Period.between(date, LocalDate.now()).years.toFloat()
        private fun calcMaxHR(date: LocalDate, gender: Gender) = when (gender) {
            Gender.MALE -> 214f - (0.8f * getAge(date))
            Gender.FEMALE -> 209f - (0.9f * getAge(date))
            else -> 206.9f - (0.67f * getAge(date))
        }.toInt()
    }
}



/*
private val infoList = listOf(
    HeartZoneInfo(
        //"50% - 60%\nзона легкой активности",
        //"низкая нагрузка развивает аэробную базу и помогает восстановиться",
        idx = 1,
        0,
        0
    ),
    HeartZoneInfo(
        //"60% - 70%\nначало жиросжигающей зоны",
        //"средняя нагрузка повышает выносливость и оптимально сжигает калории",
        idx = 2,
        0,
        0
    ),
    HeartZoneInfo(
        //"70% - 80%\nаэробная зона",
        //"высокая нагрузка способствует повышению кардиовыносливости",
        idx = 3,
        0,
        0
    ),
    HeartZoneInfo(
        //"80% - 90%\nанаэробная зона",
        //"улучшает физическую выносливость",
        idx = 4,
        0,
        0
    ),
    HeartZoneInfo(
        //"90% - 100%\nзона VO2",
        //"максимальная нагрузка помогает повысить отдачу энергии и скорость",
        idx = 5,
        0,
        0
    )
)
*/

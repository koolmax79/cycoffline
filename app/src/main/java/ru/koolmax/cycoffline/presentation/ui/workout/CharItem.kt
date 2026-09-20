package ru.koolmax.cycoffline.presentation.ui.workout

import androidx.compose.ui.graphics.Color
import ru.koolmax.cycoffline.R
import ru.koolmax.cycoffline.data.media.FitListType
import ru.koolmax.cycoffline.presentation.ui.statistics.ChartType
import ru.koolmax.cycoffline.ui.theme.LocalCustomColorsPalette

sealed class CharItem(val name: Int, val icon: Int) {
    object Altitude : CharItem(R.string.altitude, R.drawable.elevation_gain)
    object Grade : CharItem(R.string.grade, R.drawable.elevation)
    object Speed : CharItem(R.string.speed, R.drawable.speed)
    object Heart : CharItem(R.string.heart, R.drawable.heart_rate)
    object Cadence : CharItem(R.string.cadence, R.drawable.cadence)
    object Temperature : CharItem(R.string.temperature, R.drawable.temperature)

    companion object {
        val list = mapOf(FitListType.ALTITUDE to Altitude,
            FitListType.GRADE to Grade,
            FitListType.SPEED to Speed,
            FitListType.HEART to Heart,
            FitListType.CADENCE to Cadence,
            FitListType.TEMPERATURE to Temperature)
    }
}

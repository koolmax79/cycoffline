package ru.koolmax.cycoffline.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

//val label: String, val description: String,
data class HeartZoneInfo(val idx: Int, var min: Int = 0, var max: Int = 0) {
    //fun inZone(rate: Short) = (rate <= max && min==0) || (rate >= min && max==0) || (rate in min .. max)
    fun inZone(rate: Short) = (rate in min .. max)
}

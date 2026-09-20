package ru.koolmax.cycoffline.presentation.ui.lib.сhart

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle

data class AxisProperties(
    val textStyle: TextStyle = TextStyle(Color.Black),
    val textMeasurer: TextMeasurer,
    val xType: AxisType = AxisType.INT,
    val yType: AxisType = AxisType.INT,
    val xIndicatorBuilder: (Int) -> String = {
        it.toString()
    },
    val yIndicatorBuilder: (Float) -> String = {
        it.toString()
    })

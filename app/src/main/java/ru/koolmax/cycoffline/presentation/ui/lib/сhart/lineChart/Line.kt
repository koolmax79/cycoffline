package ru.koolmax.cycoffline.presentation.ui.lib.сhart.lineChart

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.DrawStyle as CanvasDrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke

data class Line(
    val label: String? = null,
    val values: List<Float>,
    val color: Brush,
    val type: TypeLine = TypeLine.Linear,
    val drawStyle: DrawStyle = DrawStyle.Stroke(2.dp),
    val fillColor: Color = Color.Unspecified) {
    val isNotEmpty = values.isNotEmpty()
}

enum class TypeLine {
    Linear,
    Step
}

sealed class DrawStyle() {
    data class Stroke(val width: Dp = 2.dp) :
        DrawStyle()

    data object Fill : DrawStyle()

    fun getStyle(density: Float):CanvasDrawStyle{
        return when(this){
            is Stroke -> {
                Stroke(
                    width = this.width.value*density,
                )
            }
            is Fill -> {
                androidx.compose.ui.graphics.drawscope.Fill
            }
        }
    }
}
package ru.koolmax.cycoffline.presentation.ui.lib.сhart

import android.R
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.garmin.fit.Bool
import kotlinx.coroutines.CoroutineScope
import ru.koolmax.cycoffline.presentation.ui.lib.сhart.lineChart.Popup

data class PopupProperties(val enabled: Boolean = true,
                           val textStyle: TextStyle = TextStyle.Default.copy(fontSize = 12.sp),
                           val backgroundColor: Color = Color.White,
                           val lineColor: Color = Color.White,
                           val yIndicatorBuilder: (Float) -> String = {
                               it.toString()
                           },
                           val xIndicatorBuilder: (Int) -> String = {
                               it.toString()
                           },
                           val dot: Boolean = true,
                           val onClick: (Any) -> Unit = { })

internal fun DrawScope.drawPopup(popup: Popup.Show<*>, property: PopupProperties, textMeasurer: TextMeasurer) {
    drawPath(popup.linePath,
        brush = SolidColor(property.lineColor),
        style = Stroke(width = 2f)
    )
    if (property.dot)
        drawCircle(center = popup.point, radius = 6f, color = property.lineColor)
    drawRoundRect(color = property.backgroundColor, popup.rectPath.first, size = popup.rectPath.second, cornerRadius = CornerRadius(10f, 10f))
    drawText(
        textMeasurer = textMeasurer,
        text = popup.text,
        style = property.textStyle,
        topLeft = popup.rectPath.first)
}
package ru.koolmax.cycoffline.presentation.ui.lib.сhart

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.IntSize
import kotlin.math.max

data class ChartGeometry(val size: Size, val xIndicatorSize: Size, val yIndicatorSize: Size) {

    val chart: Rect get() = Rect(yIndicatorSize.width, 0f, size.width - xIndicatorSize.width, size.height - xIndicatorSize.height)

    val isEmpty: Boolean get() = (size.width<=0f || size.height<=0f)
    val isNotEmpty: Boolean get() = (size.width>0f && size.height>0f)

    companion object {
        fun create(size: Size,
                   textMeasurer: TextMeasurer,
                   xIndicatorBuilder: (Int) -> String,
                   xMin: Int,
                   xMax: Int,
                   yIndicatorBuilder: (Float) -> String,
                   yMin: Float,
                   yMax: Float): ChartGeometry {

            val xIndicatorSize = Size(max(
                textMeasurer.measure(xIndicatorBuilder(xMin) + "0").size.width,
                textMeasurer.measure(xIndicatorBuilder(xMax) + "0").size.width
                ).toFloat(),
                max(
                textMeasurer.measure(xIndicatorBuilder(xMin) + "0").size.height,
                textMeasurer.measure(xIndicatorBuilder(xMax) + "0").size.height
                ).toFloat())

            val yIndicatorSize = Size(max(
                textMeasurer.measure(yIndicatorBuilder(yMin) + "0").size.width,
                textMeasurer.measure(yIndicatorBuilder(yMax) + "0").size.width
                ).toFloat(),max(
                textMeasurer.measure(yIndicatorBuilder(yMin)).size.height,
                textMeasurer.measure(yIndicatorBuilder(yMax)).size.height
                ).toFloat())

            return ChartGeometry(size, xIndicatorSize, yIndicatorSize)
        }
    }
}
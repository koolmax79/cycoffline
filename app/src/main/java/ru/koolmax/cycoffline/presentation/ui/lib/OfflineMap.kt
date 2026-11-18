package ru.koolmax.cycoffline.presentation.ui.lib

import androidx.compose.ui.geometry.Rect
import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultBlendMode
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.fastForEachIndexed
import ru.koolmax.cycoffline.R
import ru.koolmax.cycoffline.data.media.FitFile
import ru.koolmax.cycoffline.data.media.TrackPause
import ru.koolmax.cycoffline.presentation.MeasureUtil
import kotlin.math.cos

@Composable
fun OfflineMap(
    modifier: Modifier = Modifier,
    fitFile: FitFile,
    trackColor: Color,
    startColor: Color,
    finishColor: Color,
    textMeasurer: TextMeasurer = rememberTextMeasurer()) {
    val density = LocalDensity.current
    var mapSize by remember(density) { mutableStateOf(Size(0f, 0f)) }

    var pathData by remember(mapSize) {
        mutableStateOf(PathData())
    }

    val startImage = ImageBitmap.imageResource(id = R.drawable.start)
    val finishImage = ImageBitmap.imageResource(id = R.drawable.finish)
    val pauseImage = ImageBitmap.imageResource(id = R.drawable.pause)

    Box(modifier = modifier) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize().pointerInput(fitFile) {
                        detectTapGestures(
                            onPress = {
                            }
                        )
                    }.pointerInput(fitFile) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                            },
                            onHorizontalDrag = { change, _ ->
                            })
                    }) {
                mapSize = size

                if (pathData.isEmpty()) {
                    //Log.i("cycoffline1", "${size.toString()} ${pathData.isEmpty()}")
                    pathData = PathData(mapSize, fitFile.latValues, fitFile.longValues, fitFile.getPauseList())
                }

                if (!pathData.isEmpty()) {
                    drawPath(
                        path = pathData.path,
                        color = trackColor,
                        style = Stroke(
                            width = 5f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    drawImage(startImage, topLeft = pathData.start)
                    drawImage(finishImage, topLeft = pathData.end)

                    //    drawRect(center = pathData.end, size = Size(width = 32f, height = 32f), brush = SolidColor(finishColor))
                    //drawTriangle(center = pathData.start, size = Size(width = 32f, height = 32f), brush = SolidColor(startColor))
                    pathData.pauseOffset.forEach {
                        //drawCircle(center = it, radius = 16f, brush = SolidColor(trackColor))
                        drawImage(pauseImage, topLeft = it.first)
                        drawText(textMeasurer = textMeasurer,
                            topLeft = Offset(it.first.x, it.first.y + pauseImage.height),
                            text = MeasureUtil.getDurationShort(it.second.duration))
                    }
                }
            }
        }
    }
}

fun DrawScope.drawRect(
    brush: Brush,
    center: Offset,
    size: Size,
    @FloatRange(from = 0.0, to = 1.0) alpha: Float = 1.0f,
    style: androidx.compose.ui.graphics.drawscope.DrawStyle = Fill,
    colorFilter: ColorFilter? = null,
    blendMode: BlendMode = DefaultBlendMode,
) {
    drawRect(brush = brush,
        topLeft = Offset(center.x - size.width / 2, center.y - size.height / 2),
        size = size,
        alpha = alpha,
        style = style,
        colorFilter = colorFilter,
        blendMode = blendMode)
}

fun DrawScope.drawTriangle(
    brush: Brush,
    center: Offset,
    size: Size,
    @FloatRange(from = 0.0, to = 1.0) alpha: Float = 1.0f,
    style: androidx.compose.ui.graphics.drawscope.DrawStyle = Fill,
) {
    val path = Path()
    path.moveTo(center.x - size.width / 2, center.y - size.height / 2)
    path.lineTo(center.x + size.width / 2, center.y)
    path.lineTo(center.x - size.width / 2, center.y + size.height / 2)
    path.close()
    drawPath(path, brush = brush, style = style, alpha = alpha)
}

class PathData(size: Size = Size.Zero, latValues: FloatArray = FloatArray(0), longValues: FloatArray = FloatArray(0), pauseList: List<TrackPause> = listOf()) {
    var path: Path = Path()
    private val rect: Rect
    val pauseOffset = mutableListOf<Pair<Offset, TrackPause>>()

    fun isEmpty(): Boolean = rect.isEmpty

    var start: Offset = Offset.Zero
    var end: Offset = Offset.Zero

    init {
        rect = Rect(0f, 0f, size.width, size.height)

        if(!rect.isEmpty && latValues.any() && longValues.any()) {
            val minLat = latValues.min()
            val maxLat = latValues.max()
            val minLon = longValues.min()
            val maxLon = longValues.max()

            //for (idx in latValues.indices) {
            //    if(idx > 20) break
            //    Log.i("cycoffline1 lat", "${idx} ${latValues[idx]} ${longValues[idx]}")
            //}
            //Log.i("cycoffline1 lat", "${minLat} ${maxLat} ${minLon} ${maxLon}")

            val centerLat = (minLat + maxLat) / 2.0
            val longitudeScale = cos(Math.toRadians(centerLat))

            // Переводим GPS в условные "метры".
            val points = latValues.indices.map { i ->
                val x = (longValues[i] - minLon) * longitudeScale
                val y = maxLat - latValues[i]

                Offset(
                    x = x.toFloat(),
                    y = y
                )
            }

            val minX = points.minOf { it.x }
            val maxX = points.maxOf { it.x }
            val minY = points.minOf { it.y }
            val maxY = points.maxOf { it.y }

            //Log.i("cycoffline1", "${minX} ${maxX} ${minY} ${maxY}")

            val trackWidth = maxX - minX
            val trackHeight = maxY - minY

            //if (trackWidth == 0f && trackHeight == 0f) {
            //    return@Canvas
            //}

            val availableWidth = rect.width
            val availableHeight = rect.height

            // Масштабируем трек так, чтобы он полностью помещался.
            val scale = when {
                trackWidth == 0f -> availableHeight / trackHeight
                trackHeight == 0f -> availableWidth / trackWidth
                else -> minOf(
                    availableWidth / trackWidth,
                    availableHeight / trackHeight
                )
            }

            val scaledWidth = trackWidth * scale
            val scaledHeight = trackHeight * scale

            // Центрируем трек.
            val offsetX = (rect.width - scaledWidth) / 2f
            val offsetY = (rect.height - scaledHeight) / 2f

            if(points.any()) {
                var pauseIdx = if(pauseList.any()) 0 else -1
                points.fastForEachIndexed { index, point ->
                    val x = (point.x - minX) * scale + offsetX
                    val y = (point.y - minY) * scale + offsetY

                    if(pauseIdx >= 0) {
                        if (pauseList[pauseIdx].time == index) {
                            pauseOffset.add(Pair(Offset(x, y), pauseList[pauseIdx]))
                        }
                    }

                    if (index == 0) {
                        start = Offset(x, y)
                        path.moveTo(x, y)
                    } else if (index == (points.size - 1)) {
                        end = Offset(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }
            }
        }
    }
}

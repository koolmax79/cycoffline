package ru.koolmax.cycoffline.presentation.ui.lib.сhart.barChart

import androidx.compose.ui.graphics.Brush

data class Bars<T>(val label: String,
                val values: List<Float>,
                val objects: List<T>,
                val color: Brush)

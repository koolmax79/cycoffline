package ru.koolmax.cycoffline.presentation.ui.workout

import androidx.compose.runtime.Composable
import ru.koolmax.cycoffline.R

typealias ComposableFun = @Composable () -> Unit
sealed class TabRoute(var icon: Int, var title: Int) {
    object Info : TabRoute(R.drawable.page_info_24px, R.string.info)
    object Charts : TabRoute(R.drawable.line_axis_24px, R.string.charts)
    object Statistics : TabRoute(R.drawable.line_axis_24px, R.string.statistics)
}
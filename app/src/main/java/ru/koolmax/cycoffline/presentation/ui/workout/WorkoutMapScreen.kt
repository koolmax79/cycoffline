package ru.koolmax.cycoffline.presentation.ui.workout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import ru.koolmax.cycoffline.presentation.ui.lib.OfflineMap
import ru.koolmax.cycoffline.ui.theme.LocalCustomColorsPalette
import ru.koolmax.cycoffline.ui.theme.LocalSpacing

@Composable
fun WorkoutMapScreen(viewModel: WorkoutViewModel) {
    val fit by viewModel.fit.collectAsState()
    OfflineMap(modifier = Modifier.fillMaxWidth(1f).aspectRatio(1f).padding(LocalSpacing.current.space100), fit,
        LocalCustomColorsPalette.current.trackColor,
        LocalCustomColorsPalette.current.startColor,
        LocalCustomColorsPalette.current.finishColor)
}
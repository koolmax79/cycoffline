package ru.koolmax.cycoffline.presentation.ui.lib

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun LockScreenOrientation(
    orientation: Int
) {
    val activity = LocalContext.current as Activity

    DisposableEffect(orientation) {
        val previousOrientation = activity.requestedOrientation

        activity.requestedOrientation = orientation

        onDispose {
            activity.requestedOrientation = previousOrientation
        }
    }
}
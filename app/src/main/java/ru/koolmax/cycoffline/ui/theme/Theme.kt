package ru.koolmax.cycoffline.ui.theme

import android.app.Activity
import android.os.Build
import androidx.collection.emptyLongSet
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.koolmax.cycoffline.presentation.ui.statistics.ChartType

private val LightColorScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

@Immutable
data class CustomColorsPalette(
    val iconColorActive: Color = Color.Unspecified,
    val iconColorEnabled: Color = Color.Unspecified,
    val iconColorDisabled: Color = Color.Unspecified,

    val ascentColor: Color = Color.Unspecified,
    val distanceColor: Color = Color.Unspecified,
    val altitudeColor: Color = Color.Unspecified,
    val gradeColor: Color = Color.Unspecified,
    val speedColor: Color = Color.Unspecified,
    val heartColor: Color = Color.Unspecified,
    val cadenceColor: Color = Color.Unspecified,
    val temperatureColor: Color = Color.Unspecified,
    val heartTimeColor: Color = Color.Unspecified,
    val movingTime: Color = Color.Unspecified,

    val heartZone1Color: Color = Color.Unspecified,
    val heartZone2Color: Color = Color.Unspecified,
    val heartZone3Color: Color = Color.Unspecified,
    val heartZone4Color: Color = Color.Unspecified,
    val heartZone5Color: Color = Color.Unspecified,
) {
    val heartZoneColor get() = mapOf(
        1 to heartZone1Color,
        2 to heartZone2Color,
        3 to heartZone3Color,
        4 to heartZone4Color,
        5 to heartZone5Color
    )

    fun getHeartZoneColor(num: Int) = when(num) {
        1 -> heartZone1Color
        2 -> heartZone2Color
        3 -> heartZone3Color
        4 -> heartZone4Color
        5 -> heartZone5Color
        else -> throw IllegalArgumentException("Invalid zone ${num}")
    }

    val сhartTypeColor get() = mapOf(
        ChartType.DISTANCE to distanceColor,
        ChartType.AVG_HEART_RATE to heartColor,
        ChartType.AVG_SPEED to speedColor,
        ChartType.ASCENT to ascentColor,
        ChartType.MOVING_TIME to movingTime,
        ChartType.MAX_HEART_RATE to heartColor
    )

    fun getChartTypeColor(chartType: ChartType) = when(chartType) {
        ChartType.DISTANCE -> distanceColor
        ChartType.AVG_HEART_RATE -> heartColor
        ChartType.AVG_SPEED -> speedColor
        ChartType.ASCENT -> ascentColor
        ChartType.MOVING_TIME -> movingTime
        ChartType.MAX_HEART_RATE -> heartColor
    }
}

val LightCustomColorsPalette = CustomColorsPalette(
    iconColorActive = IconColorActive,
    iconColorEnabled = IconColorEnabled,
    iconColorDisabled = IconDisabled,

    movingTime = MovingTime,
    ascentColor = AscentColor,
    distanceColor = DistanceColor,
    altitudeColor = AltitudeColor,
    gradeColor = GradeColor,
    speedColor = SpeedColor,
    heartColor = HeartColor,
    cadenceColor = CadenceColor,
    temperatureColor = TemperatureColor,
    heartTimeColor = HeartTimeColor,

    heartZone1Color = HeartZone1Color,
    heartZone2Color = HeartZone2Color,
    heartZone3Color = HeartZone3Color,
    heartZone4Color = HeartZone4Color,
    heartZone5Color = HeartZone5Color,
)

val DarkCustomColorsPalette = CustomColorsPalette(
    iconColorActive = IconColorActive,
    iconColorEnabled = IconColorEnabled,
    iconColorDisabled = IconDisabled,

    movingTime = MovingTime,
    ascentColor = AscentColor,
    distanceColor = DistanceColor,
    altitudeColor = AltitudeColor,
    gradeColor = GradeColor,
    speedColor = SpeedColor,
    heartColor = HeartColor,
    cadenceColor = CadenceColor,
    temperatureColor = TemperatureColor,
    heartTimeColor = HeartTimeColor,

    heartZone1Color = HeartZone1Color,
    heartZone2Color = HeartZone2Color,
    heartZone3Color = HeartZone3Color,
    heartZone4Color = HeartZone4Color,
    heartZone5Color = HeartZone5Color,
)

val LocalCustomColorsPalette = staticCompositionLocalOf { CustomColorsPalette() }

@Immutable
data class Spacing(
    val space25: Dp = 2.dp,
    val space100: Dp = 8.dp,
    val space200: Dp = 16.dp,
    val space300: Dp = 24.dp,
    val space400: Dp = 32.dp,
    val space500: Dp = 40.dp,
    val space600: Dp = 50.dp,
    val chartHeight: Dp = 270.dp
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }

@Immutable
data class SizeIcon(
    val size50: Dp = 20.dp,
    val size100: Dp = 40.dp,
)

val LocalIconSize = staticCompositionLocalOf { SizeIcon() }

@Composable
fun CycofflineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        //dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        //    val context = LocalContext.current
        //    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        //}

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val customColorsPalette = when {
        darkTheme -> DarkCustomColorsPalette
        else -> LightCustomColorsPalette
    }

    CompositionLocalProvider(LocalCustomColorsPalette provides customColorsPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
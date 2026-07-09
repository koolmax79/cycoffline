package ru.koolmax.cycoffline.data

import ru.koolmax.cycoffline.R
import ru.koolmax.cycoffline.data.media.FitListType

object FitTypeImpl {
    //var d = LocalCustomColorsPalette.current.iconColorActive
    val info = mapOf(
        FitListType.ALTITUDE to FitTypeInfo("Высота", R.color.ALTITUDE),
        FitListType.GRADE to FitTypeInfo("Градиент", R.color.SPEED),
        FitListType.SPEED to FitTypeInfo("Скорость", R.color.SPEED),
        FitListType.HEART to FitTypeInfo("ЧСС", R.color.HEART),
        FitListType.CADENCE to FitTypeInfo("Каденс", R.color.CADENCE),
        FitListType.TEMPERATURE to FitTypeInfo("Температура", R.color.TEMPERATURE),
        //FitRecords.FitListType.HEART_TIME to FitTypeInfo("ЧСС по времени", R.color.HEART_TIME)
    )
}
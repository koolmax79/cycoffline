package ru.koolmax.cycoffline.data

import ru.koolmax.cycoffline.data.db.FitSessionItem

data class FitStatisticItem(
    var count: Int = 0,
    var avgHeartRate: Int? = null,
    var avgSpeed: Double? = null,
    var maxHeartRate: Int? = null,
    var totalAscent: Int? = null,
    var totalDescent: Int? = null,
    var totalDistance: Int = 0,
    var totalElapsedTime: Int = 0,
    var totalMovingTime: Int = 0
) {
    companion object {
        fun make(list: List<FitSessionItem>) = FitStatisticItem().apply {
                count = list.size
                avgHeartRate = list.mapNotNull{ it.avgHeartRate }.takeIf { it.isNotEmpty() }?.average()?.toInt()
                avgSpeed = list.mapNotNull{ it.avgSpeed }.takeIf { it.isNotEmpty() }?.average()
                maxHeartRate = list.mapNotNull{ it.maxHeartRate }.takeIf { it.isNotEmpty() }?.max()
                totalAscent = list.mapNotNull{ it.totalAscent }.takeIf { it.isNotEmpty() }?.sum()
                totalDescent = list.mapNotNull{ it.totalDescent }.takeIf { it.isNotEmpty() }?.sum()
                totalDistance = list.mapNotNull{ it.totalDistance }.sum()
                totalElapsedTime = list.mapNotNull{ it.totalElapsedTime }.sum()
                totalMovingTime = list.mapNotNull{ it.totalMovingTime }.sum()
        }
    }
}
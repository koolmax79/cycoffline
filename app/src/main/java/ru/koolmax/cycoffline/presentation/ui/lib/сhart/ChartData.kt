package ru.koolmax.cycoffline.presentation.ui.lib.сhart

data class ChartData(val yValues: IntArray = intArrayOf(0), val sum: Int, val xValues: IntArray = intArrayOf(0)) {
    companion object {
        fun create(yValues: IntArray, barrierPercent: Float = 1f, xMin: Int): ChartData {
            val barrier = (yValues.max() / 100f) * barrierPercent

            val beginIdx = yValues.indexOfFirst { it >= barrier }
            val endIdx = yValues.indexOfLast { it >= barrier }
            val barrierYValues = yValues.copyOfRange(beginIdx, endIdx)

            return ChartData(barrierYValues,
                yValues.sum(),
                List(barrierYValues.size) { index -> xMin + beginIdx + index }.toIntArray())
        }
    }

    fun <T> separateByZone(zoneList: List<Triple<T, Int, Int>>): List<Pair<T, List<Int>>> {
        val result = mutableListOf<Pair<T, MutableList<Int>>>()
        zoneList.forEach { zone ->
            val list = mutableListOf<Int>().also { it.addAll(yValues.toList()) }
            list.forEachIndexed { idx, y ->
                if(xValues[idx] !in (if(zone.second!=Int.MIN_VALUE) zone.second - 1 else zone.second) ..
                    (if(zone.third!=Int.MAX_VALUE) zone.third + 1 else zone.third) ) list[idx] = 0
            }
            result.add(Pair(zone.first, list))
        }
        return result
    }
}

data class DataPoint(val x: Float, val y: Float, val idx: Int)
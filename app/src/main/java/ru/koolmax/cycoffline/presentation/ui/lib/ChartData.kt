package ru.koolmax.cycoffline.presentation.ui.lib

data class ChartPoint(val x: Int, val y: Double)

data class ChartData(val yValues: List<Int> = listOf(), val xMin: Int = 0) {
    fun getX(xIdx: Int) = xMin + xIdx
    val xMax get() = xMin + yValues.size
    val sum get() = yValues.sum()

    fun <T> separateByZone(zoneList: List<Triple<T, Int, Int>>): List<Pair<T, List<Int>>> {
        val result = mutableListOf<Pair<T, MutableList<Int>>>()
        zoneList.forEach { zone ->
            val list = mutableListOf<Int>().also { it.addAll(yValues) }
            list.forEachIndexed { idx, y ->
                if(getX(idx) !in (if(zone.second!=Int.MIN_VALUE) zone.second - 1 else zone.second) ..
                    (if(zone.third!=Int.MAX_VALUE) zone.third + 1 else zone.third) ) list[idx] = 0
            }
            result.add(Pair(zone.first, list))
        }
        return result
    }
}

/*
data class ChartData(val yValues: List<Double> = listOf(), val xMin: Int = 0, val xMax: Int = 0): List<ChartPoint> {
    override val size: Int
        get() = TODO("Not yet implemented")

    override fun isEmpty() = yValues.isEmpty()

    override fun contains(element: ChartPoint): Boolean {
        yValues.
    }

    override fun iterator(): Iterator<ChartPoint> {
        TODO("Not yet implemented")
    }

    override fun containsAll(elements: Collection<ChartPoint>): Boolean {
        TODO("Not yet implemented")
    }

    override fun get(index: Int) = ChartPoint(xMin + index, yValues[index])

    override fun indexOf(element: ChartPoint): Int {
        TODO("Not yet implemented")
    }

    override fun lastIndexOf(element: ChartPoint): Int {
        TODO("Not yet implemented")
    }

    override fun listIterator(): ListIterator<ChartPoint> {
        TODO("Not yet implemented")
    }

    override fun listIterator(index: Int): ListIterator<ChartPoint> {
        TODO("Not yet implemented")
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<ChartPoint> {
        TODO("Not yet implemented")
    }
}*/

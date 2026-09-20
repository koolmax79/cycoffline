package ru.koolmax.cycoffline.presentation.ui.lib.сhart.lineChart

import android.util.Log
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sign

interface Axis {
    val min: Float
    val max: Float
    val values: List<Float>
}

data class AxisEmpty(override val min: Float = 0f, override val max: Float = 10f, override val values: List<Float> = listOf()) : Axis

data class AxisNumber(override val min: Float, override val max: Float, override val values: List<Float>)
    : Axis {
    companion object {
        fun create(min: Float, max: Float, maxCount: Int = 10): AxisNumber {
            var minVal = min
            var maxVal = max

            if(max == min) {
                if(min.sign == max.sign) {
                    minVal = min * 0.8f
                    maxVal = max * 1.2f
                } else {
                    minVal = min * 1.2f
                    maxVal = max * 1.2f
                }
            }

            Log.i("AxisNumber cycoffline1", "$maxVal - $minVal $maxCount")

            val intervalVal = maxVal - minVal
            val step = roundStep(intervalVal / maxCount)

            val label = mutableSetOf<Float>()
            var l = 0f
            while(l < maxVal + step) {
                if(l > minVal - step)
                    label.add(l)
                l += step
            }

            l = 0f
            while(l > minVal - step) {
                if(l < maxVal + step)
                    label.add(l)
                l -= step
            }
            return AxisNumber(label.min(), label.max(), label.toList().sorted())
        }

        private fun roundStep(step: Float): Float {
            val roundTemplate = arrayOf(10, 2, 5, 5, 5, 10, 10, 10, 10, 10)
            val n = getBaseMantissa(step)
            var str = n.first.toString()
            Log.i("cycoffline1", "$str $n $step")

            val r = roundTemplate[str[0].toString().toInt()]
            str = str.removeRange(0, 1)
            str = r.toString() + str
            str = replaceAllExceptFirst(str)
            return str.toFloat() * 10f.pow(n.second)
        }

        private fun replaceAllExceptFirst(input: String): String {
            if (input.length <= 1) return input
            val firstChar = input[0]
            val rest = input.substring(1)
            val replacedRest = rest.map { '0' }.joinToString("")
            return firstChar + replacedRest
        }

        private fun getBaseMantissa(value: Float): Pair<Int, Int> {
            val str = String.format("%.6e", value)
            val re = Regex("(^[+-]?[0-9]*[.,]?[0-9]+)e([+-]?[0-9]+)")
            val match = re.find(str)

            if(match != null) {
                val (b, m) = match.destructured
                return Pair((b.replace(',', '.').toDouble() * 10.0.pow(6)  / 10.0).toInt(), m.toInt() - 5)
            }
            return Pair(0,0)
        }
    }
}

data class AxisTime(override val min: Float, override val max: Float, override val values: List<Float>): Axis {
    companion object {
        fun create(min: Float = 0f, max: Float, maxCount: Int = 5): AxisTime {
            var minVal = min
            var maxVal = max

            if(max == min) {
                if(min.sign == max.sign) {
                    minVal = min * 0.8f
                    maxVal = max * 1.2f
                } else {
                    minVal = min * 1.2f
                    maxVal = max * 1.2f
                }
            }

            Log.i("AxisTime cycoffline1", "$maxVal - $minVal $maxCount")

            val intervalVal = maxVal - minVal
            val step = AxisTime.Companion.roundStep(intervalVal / maxCount)

            val label = mutableSetOf<Float>()
            var l = 0f
            while(l < maxVal + step) {
                if(l > minVal - step)
                    label.add(l)
                l += step
            }

            l = 0f
            while(l > minVal - step) {
                if(l < maxVal + step)
                    label.add(l)
                l -= step
            }
            //val intervalVal = max
            //val step = roundStep(intervalVal / maxCount)

            //val label = mutableSetOf<Double>()
            //var l = 0.0
            //while(l < max + step) {
            //    label.add(l)
            //    l += step
            //}

            return AxisTime(0f,label.max(), label.toList().sorted())
        }

        private fun roundStep(step: Float): Int {
            val roundTemplate = arrayOf(60, 3600/12, 3600/6, 3600/4, 3600/2, 3600)
            val idx = min(roundTemplate.indexOfLast { it < step } + 1, roundTemplate.size - 1)
            return roundTemplate[idx]
        }
    }
}

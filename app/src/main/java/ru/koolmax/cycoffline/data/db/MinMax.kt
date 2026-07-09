package ru.koolmax.cycoffline.data.db

import androidx.room.ColumnInfo

data class MinMax(@ColumnInfo(name = "min") val min: Int, @ColumnInfo(name = "max") val max: Int) {
}
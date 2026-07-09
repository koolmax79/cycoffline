package ru.koolmax.cycoffline.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "settings")
data class SettingsItem(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name="key")
    var key: String = "",
    @ColumnInfo(name="value")
    var value: String = ""
)

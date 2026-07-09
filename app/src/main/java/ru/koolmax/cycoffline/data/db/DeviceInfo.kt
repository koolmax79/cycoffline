package ru.koolmax.cycoffline.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import no.nordicsemi.android.kotlin.ble.core.ServerDevice

@Entity(tableName = "devices")
data class DeviceInfo(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name="address")
    var address: String = "",
    @ColumnInfo(name="name")
    var name: String = "",
)

fun ServerDevice.toDeviceInfo() = DeviceInfo(this.address, this.name ?: "")
package ru.koolmax.cycoffline.data

import ru.koolmax.cycoffline.data.db.DeviceInfo

enum class DEVICE_STATUS { OFFLINE, CONNECTED }
//, val batteryLevel: Int? = null
//, batteryLevel: Int?
data class DeviceStatus(var device: DeviceInfo = DeviceInfo(), var status: DEVICE_STATUS = DEVICE_STATUS.OFFLINE, val batteryLevel: Int = -1) {
    constructor(address: String, name: String, status: DEVICE_STATUS, batteryLevel: Int = -1) : this(DeviceInfo(address, name), status, batteryLevel)
}

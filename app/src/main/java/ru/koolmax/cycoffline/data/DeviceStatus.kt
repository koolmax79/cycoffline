package ru.koolmax.cycoffline.data

import ru.koolmax.cycoffline.data.db.DeviceInfo

enum class DEVICE_STATUS { OFFLINE, CONNECTED }

data class DeviceStatus(var device: DeviceInfo = DeviceInfo("", ""), var status: DEVICE_STATUS = DEVICE_STATUS.OFFLINE) {
    constructor(address: String, name: String, status: DEVICE_STATUS) : this(DeviceInfo(address, name), status)
}

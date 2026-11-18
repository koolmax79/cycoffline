package ru.koolmax.cycoffline.data

import ru.koolmax.cycoffline.data.db.DeviceInfo

open class DeviceFileProgressListener(val device: DeviceInfo) {
    open fun onConnect(batteryLevel: Int) {}
    open fun onBegin(file: String, max: Int) {}
    open fun onStep(count: Int) {}
    open fun onFinish(file: String) {}
    open fun onDisconnect() {}
}
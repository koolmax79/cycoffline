package ru.koolmax.cycoffline.data

import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import ru.koolmax.cycoffline.data.db.DeviceInfo

open class OnProgressListener {
    open fun onConnect(device: DeviceInfo) {}
    open fun onBegin(file: String, max: Int) {}
    open fun onStep(count: Int) {}
    open fun onFinish(file: String) {}
    open fun onDisconnect(device: DeviceInfo) {}
}
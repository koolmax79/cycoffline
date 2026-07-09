package ru.koolmax.cycoffline.data.ble

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResult
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResultData
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResults

class BleScanAggregator() {
    private var devices = mutableMapOf<ServerDevice, BleScanResultData>()

    fun aggregate(scanItem: BleScanResult): List<BleScanResult> {
        scanItem.data?.let {
            devices[scanItem.device] = it
            devices = devices.filter { device -> scanItem.data!!.timestampNanos - device.value.timestampNanos < 2000_000_000L }.toMutableMap()
        }
        return devices.map { BleScanResult(it.key, it.value) }
    }
}
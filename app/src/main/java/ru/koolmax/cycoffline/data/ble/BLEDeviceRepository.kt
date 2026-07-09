package ru.koolmax.cycoffline.data.ble

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResult
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScannerSettings
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner
import ru.koolmax.cycoffline.data.DeviceFile
import ru.koolmax.cycoffline.data.OnProgressListener
import ru.koolmax.cycoffline.data.db.DeviceInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BLEDeviceRepository @Inject constructor(private val context: Context) {
    //lateinit var address: String

    @SuppressLint("MissingPermission")
    fun startScan(scope: CoroutineScope, onList : suspend (List<BleScanResult>) -> Unit) {
        //Log.i("FitOpener3", "start scan")
        val settings = BleScannerSettings(includeStoredBondedDevices = false)
        val aggregator = BleScanAggregator()
        //BleScannerSettings
        BleScanner(context).scan(settings = settings)
            .map {
               //it.data.rssi
                //Log.i("cycoffline1", "${it.device.address} ${it.data!!.timestampNanos}")
                onList(aggregator.aggregate(it))
            } //Add new device and return an aggregated list
            //.onEach {
            //    onList(it)
            //}
            .launchIn(scope)
    }

    //@SuppressLint("MissingPermission")
    suspend fun connect(device: DeviceInfo, scope: CoroutineScope, progressListener: OnProgressListener): BLEDevice? {
        //this.address = address
        //Log.i("cycoffline1", address)
        return BLEDevice.connect(device, scope, context, progressListener)
        //Log.i("FitOpener1", "connect")
    }
}
package ru.koolmax.cycoffline.data.ble

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import ru.koolmax.cycoffline.data.DeviceFile
import ru.koolmax.cycoffline.data.OnProgressListener
import ru.koolmax.cycoffline.data.db.DeviceInfo
import java.io.File

class BLEDevice private constructor(val device: DeviceInfo, val gatt: ClientBleGatt, val modem: YModem, val scope: CoroutineScope, val progressListener: OnProgressListener): AutoCloseable {
    lateinit var btGatt: ClientBleGatt

    init {
        btGatt = gatt
    }

    companion object {
        @SuppressLint("MissingPermission")
        suspend fun connect(device: DeviceInfo, scope: CoroutineScope, context: Context, progressListener: OnProgressListener): BLEDevice? {
            try {
                val btGatt = ClientBleGatt.connect(context, device.address, scope)
                Log.i("cycoffline1", "gatt ${btGatt.isConnected.toString()}")
                if (btGatt.isConnected) {
                    progressListener.onConnect(device)
                    val modem =
                        YModem(btGatt.discoverServices(), scope, progressListener).apply { start() }
                    return BLEDevice(device, btGatt, modem, scope, progressListener)
                }
                return null
            }
            catch (_: Exception) {
                return null
            }
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getFileList() : List<DeviceFile> {
        val list = mutableListOf<DeviceFile>()
///        val list = mutableListOf<FitFileRecord>(FitFileRecord("20220618070720.fit", 83081),
///            FitFileRecord("20240510133515.fit", 183081)  )
        modem.readFile("filelist.txt").decodeToString().split("\r\n").sortedByDescending { it }.forEach {
            val fields = it.split(" ")
            try {
                //Log.i("FitOpener3", "${fields[0]}")
                list.add(DeviceFile(fields[0], device, fields[1].toInt()))
            } catch (e: Exception) {
                //Log.i("FitOpener1", "exeption ${fields.toString()}")
            }
        }
        return list
    }

    @SuppressLint("MissingPermission")
    suspend fun getFile(file: String): ByteArray {
///        return byteArrayOf(0x22, 0x22,0x22,0x22,0x22)
        //Log.i("FitOpener3", "get file ${file}")
        return modem.readFile(file)
    }

    override fun close() {
        if(::btGatt.isInitialized && btGatt.isConnected) {
            btGatt.close()
            progressListener.onDisconnect(device)
        }
    }
}
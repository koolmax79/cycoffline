package ru.koolmax.cycoffline.data.ble

import android.annotation.SuppressLint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattServices
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import ru.koolmax.cycoffline.data.DeviceFileProgressListener
import java.util.UUID
import kotlin.experimental.xor

class YModem(private val services: ClientBleGattServices, private val scope: CoroutineScope, private val progressListener: DeviceFileProgressListener) {
    private var curXOSSFile: XOSSFile? = null

    private enum class TXCmd(val v: Byte) { ACK(0x06), NAK(0x15), C(0x43) }

    suspend fun start() {
        subscribeNotifications()
    }

    companion object {
        fun calcCRC(buf: List<Byte>): Byte {
            var crc: Byte = 0x00
            buf.forEach { crc = crc xor it }
            return crc
        }
    }

    suspend fun readFile(file: String): ByteArray {
        var data = byteArrayOf()
        curXOSSFile = XOSSFile(file, progressListener)
        curXOSSFile?.let { file ->
            //sendCmd(file.getStartCmdFF())
            //file.wait()
            sendCmd(file.getStartCmd()) //start
            file.wait()
            //if(file.isSilence) { //повторяем
            //    sendCmd(file.getStartCmd()) //start
            //    file.wait()
            //}
            sendTX(TXCmd.C)//get info file
            file.wait()
            sendTX(TXCmd.ACK)
            sendTX(TXCmd.C)//get body
            file.wait()
            data = file.getFileData()
            //Log.i("FitOpener3", "sendNAK()")
            sendTX(TXCmd.NAK)
            delay(100)
            sendTX(TXCmd.ACK)
            delay(100)
            //sendTX(TXCmd.ACK)
            //delay(100)
            //sendTX(TXCmd.ACK)
            //delay(100)
            //sendTX(TXCmd.ACK)
            //delay(100)
            //Log.i("FitOpener3", "end")
        }
        return data
    }

    fun getDataClear09(): ByteArray //0x09 0x00 0x09
    {
        val requestBuf = byteArrayOf(0x09.toByte(), 0x00.toByte()).toMutableList()
        requestBuf.add(calcCRC(requestBuf))
        return requestBuf.toByteArray()
    }

    fun getDataClearff(): ByteArray //0xff 0x00 0xff
    {
        val requestBuf = byteArrayOf(0xff.toByte(), 0x00.toByte()).toMutableList()
        requestBuf.add(calcCRC(requestBuf))
        return requestBuf.toByteArray()
    }

    @SuppressLint("MissingPermission")
    private suspend fun sendCmd(data: ByteArray) {
        val serviceUUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
        val charCTRLUUID = UUID.fromString("6e400004-b5a3-f393-e0a9-e50e24dcca9e")
        services.findService(serviceUUID)?.let {
            it.findCharacteristic(charCTRLUUID)?.splitWrite(DataByteArray(data))
        }
    }

    private suspend fun sendTX(cmd: TXCmd) {
        sendTX(byteArrayOf(cmd.v))
    }

    @SuppressLint("MissingPermission")
    private suspend fun sendTX(data: ByteArray) {
        val serviceUUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
        val charTXUUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
        services.findService(serviceUUID)?.let {
            val startData = DataByteArray(data)
            it.findCharacteristic(charTXUUID)?.splitWrite(startData, BleWriteType.NO_RESPONSE)
        }
    }

    private suspend fun subscribeNotifications() {
        //Log.i("FitOpener3", "start subscribeNotifications")
        val serviceUUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
        val charRXUUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")
        val charCTRLUUID = UUID.fromString("6e400004-b5a3-f393-e0a9-e50e24dcca9e")

        services.findService(serviceUUID)?.let {
            it.findCharacteristic(charRXUUID)?.getNotifications()?.onStart {
                //Log.i("FitOpener3", "on start RX notify")
            }?.onCompletion {
                //Log.i("FitOpener3", "on end RX notify")
            }?.onEach {
                //Log.i("FitOpener3", "on RX notify ${it.value.toHex()}")
                curXOSSFile?.let { file ->
                    if(file.receive(it.value) == XOSSFile.ReceiveStatus.ASK) {
                         sendTX(TXCmd.ACK)
                    }
                }
            }?.onEmpty {
                //Log.i("FitOpener3", "on empty notify")
            }?.launchIn(scope)

            it.findCharacteristic(charCTRLUUID)?.getNotifications()?.onEach {
                //Log.i("FitOpener3", "on CMD notify ${it.value.toHex()}")
                curXOSSFile?.receiveCtrl(it.value)
            }?.launchIn(scope)
        }
        //Log.i("FitOpener3", "end subscribeNotifications")
    }
}
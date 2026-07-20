package ru.koolmax.cycoffline.data.ble

import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.koolmax.cycoffline.data.DeviceFileProgressListener

class XOSSFile(private val file: String, private val progressListener: DeviceFileProgressListener) {
    private enum class Mode { NONE, INFO, BODY, READY }
    enum class ReceiveStatus { NONE, ASK, END }

    private val mutex = Mutex(true)
    private var fileSize = 0
    private val buffer = mutableListOf<Byte>()
    private val buffer1K = mutableListOf<Byte>()
    private var mode = Mode.NONE

    /*init {
        val timer = object : CountDownTimer(1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                if(mode == Mode.NONE) {
                    Log.i("FitOpener3", mode.toString())
                    mode = Mode.SILENCE
                    if(mutex.isLocked)
                        mutex.unlock()
                }
            }
        }
        timer.start()
    }*/

    //val isSilence get() = mode == Mode.SILENCE

    fun receiveCtrl(buf: ByteArray) {
        //Log.i("FitOpener3", "fileSize = ${buf.toHex()}")
        nextState()
        mutex.unlock()
    }

    fun receive(buf: ByteArray): ReceiveStatus {
        var status = ReceiveStatus.NONE
        when(mode) {
            Mode.INFO -> {
                decodeSize(buf)
                progressListener.onBegin(file, fileSize)
                nextState()
                mutex.unlock()
                //Log.i("FitOpener3", "fileSize = ${fileSize.toString()}")
            }
            Mode.BODY -> {
                status = receiveBody(buf)
                if(status == ReceiveStatus.END)
                    mutex.unlock()
                //Log.i("FitOpener3", "fileSize = ${receivedSize()}")
            }
            else -> { }
        }
        return status
    }

    fun getStartCmdFF(): ByteArray
    {
        val requestBuf = byteArrayOf(0x00).toMutableList()
        requestBuf.add(0, 0xff.toByte())
        requestBuf.add(YModem.calcCRC(requestBuf))
        return requestBuf.toByteArray()
    }

    fun getStartCmd(): ByteArray
    {
        val requestBuf = file.toByteArray().toMutableList()
        requestBuf.add(0, 0x05)
        requestBuf.add(YModem.calcCRC(requestBuf))
        Log.i("FitOpener3", requestBuf.toByteArray().toHex())
        return requestBuf.toByteArray()
    }

    private fun nextState() {
        mode = when(mode) {
            Mode.NONE -> Mode.INFO
            //Mode.INIT -> Mode.INFO
            Mode.INFO -> Mode.BODY
            Mode.BODY -> Mode.READY
            else -> { Mode.NONE }
        }
    }

    suspend fun wait() {
        mutex.withLock {  }
        mutex.tryLock()
        //Log.i("FitOpener3", mode.name)
    }

    fun getFileData(): ByteArray {
        //Log.i("FitOpener3", size.toString())
        moveFrom1KToBuff()
        return buffer.subList(0, fileSize).toByteArray()
    }

    private fun decodeSize(buf: ByteArray) {
        val lines = buf.decodeToString(3, buf.size-2).split(" ")
        fileSize = lines[1].trimEnd(0.toChar()).toInt()
    }

    private fun receiveBody(buf: ByteArray): ReceiveStatus {
        //Log.i("FitOpener3", "receive buf len=${buf.size}  ${buf.toHex()}")
        var status = ReceiveStatus.NONE
        if((buf.size == 1) && (buf[0] == 0x04.toByte())) {
            nextState()
            progressListener.onStep(fileSize)
            progressListener.onFinish(file)
            return ReceiveStatus.END
        }
        buffer1K.addAll(buf.toList())
        if(buffer1K.size >= 1024+5) {
            moveFrom1KToBuff()
            status = ReceiveStatus.ASK
            progressListener.onStep(receivedSize())
        }
        if(receivedSize() >= fileSize + 5) {
            status = ReceiveStatus.ASK
        }
        return status
    }

    private fun moveFrom1KToBuff() {
        if(buffer1K.size >= 5) {
            buffer.addAll(buffer1K.subList(3, buffer1K.size - 2))
            buffer1K.clear()
        }
    }

    private fun receivedSize() = buffer.size + buffer1K.size
}
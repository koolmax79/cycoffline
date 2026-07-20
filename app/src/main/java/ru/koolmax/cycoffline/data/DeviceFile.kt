package ru.koolmax.cycoffline.data

import android.net.Uri
import ru.koolmax.cycoffline.data.db.DeviceInfo

data class DeviceFile(val name: String = "", val size: Int = -1, val device: DeviceInfo = DeviceInfo())

open class FileStatus {
    open val loadedPart: Float = 0.toFloat()
}

//-1 - не загружен с устройства
//size == statusSize загружен полностью
data class DeviceFileStatus(val deviceFile: DeviceFile, var loadedSize: Int = -1): FileStatus() {
    val isNotSynchronized get() = loadedSize == -1
    val isSynchronized get() = loadedSize == deviceFile.size
    val isSynchronizing get() = (loadedSize < deviceFile.size) && (loadedSize > 0)
    val inQueue get() = loadedSize == 0
    override val loadedPart = when {
        (loadedSize == -1) -> 0.toFloat()
        else -> loadedSize.toFloat() / deviceFile.size.toFloat()
    }
}

data class UriFileStatus(val uri: Uri, var loadedStep: Int = -1): FileStatus() {
    override val loadedPart = when {
        (loadedStep == -1) -> 0.toFloat()
        (loadedStep == 0) -> (0.5).toFloat()
        else -> 1.toFloat()
    }
}
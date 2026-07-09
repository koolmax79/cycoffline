package ru.koolmax.cycoffline.data

import ru.koolmax.cycoffline.data.db.DeviceInfo

//-1 - не загружен с устройства
//size == statusSize загружен полностью
data class DeviceFile(val name: String, val device: DeviceInfo, val size: Int = -1, var loadedSize: Int = -1) {
    val isNotSynchronized get() = loadedSize == -1
    val isSynchronized get() = loadedSize == size
    val isSynchronizing get() = (loadedSize < size) && (loadedSize > 0)
    val inQueue get() = loadedSize == 0
    fun equalInfo(itm: DeviceFile) = this.name == itm.name && this.device == itm.device
}
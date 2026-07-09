package ru.koolmax.cycoffline.service

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.koolmax.cycoffline.data.DeviceFile
import ru.koolmax.cycoffline.data.DeviceStatus
import ru.koolmax.cycoffline.data.db.DeviceInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRepository @Inject constructor(private val context: Context) {
    private val lock = Any()
    private var serviceRun = false
    fun setServiceStop() {
        synchronized(lock) {
            serviceRun = false
        }
    }

    private val _deviceStatus = MutableStateFlow(DeviceStatus())
    val deviceStatus = _deviceStatus.asStateFlow()

    private val _loadingFileList = mutableStateListOf<DeviceFile>()

    private val _currentLoadingFile = MutableStateFlow(DeviceFile("", DeviceInfo()))
    val currentLoadingFile = _currentLoadingFile.asStateFlow()

    fun addToLoad(file: DeviceFile) {
        synchronized(lock) {
            _loadingFileList.add(file)
            //Log.i("cycoffline1", "add to queue ${_loadingFileList.size} ${serviceRun.toString()}")
            if(!serviceRun) {
                serviceRun = true
                val intent = Intent(context, DeviceFileService::class.java)
                context.startForegroundService(intent)
            }
        }
    }

    //fun printSize() {
    //    Log.i("cycoffline1", "printSize() ${_loadingFileList.size}")
    //}

    fun getFileForLoad(): DeviceFile? {
        //Log.i("cycoffline1", "try get file ${_loadingFileList.size}")
        synchronized(lock) {
            //Log.i("cycoffline1", "synchronized(lock) try get file ${_loadingFileList.size}")
            if (_loadingFileList.isNotEmpty()) {
                _currentLoadingFile.value.device.address.also { address ->
                    if (address.isEmpty())
                        _currentLoadingFile.value = _loadingFileList.removeAt(0)
                    else {
                        var idx = _loadingFileList.indexOfFirst { it.device.address == address }
                        if (idx == -1) idx = 0
                        _currentLoadingFile.value = _loadingFileList.removeAt(idx)
                    }
                }
                return _currentLoadingFile.value
            }
        }
        return null
    }

    fun update(value: DeviceStatus) {
        _deviceStatus.value = value
    }

    fun update(file: DeviceFile) {
        if (_currentLoadingFile.value.equalInfo(file))
            _currentLoadingFile.value = file
    }
}
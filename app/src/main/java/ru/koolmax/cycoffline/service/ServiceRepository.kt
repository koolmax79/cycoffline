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
import ru.koolmax.cycoffline.data.DeviceFileStatus
import ru.koolmax.cycoffline.data.DeviceStatus
import ru.koolmax.cycoffline.data.FileStatus
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

    private val _loadingFileList = mutableStateListOf<FileStatus>()

    private val _currentLoadingFile = MutableStateFlow(DeviceFileStatus(DeviceFile()))
    val currentLoadingFile = _currentLoadingFile.asStateFlow()

    fun addToLoad(file: FileStatus) {
        synchronized(lock) {
            _loadingFileList.add(file)
            Log.i("cycoffline1", "add to queue ${_loadingFileList.size} ${serviceRun.toString()}")
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

    fun getFileForLoad(): FileStatus? {
        //Log.i("cycoffline1", "try get file ${_loadingFileList.size}")
        synchronized(lock) {
            if (_loadingFileList.isNotEmpty()) {
                val itm = _loadingFileList.removeAt(0)
                if(itm is DeviceFileStatus)
                    _currentLoadingFile.value = itm as DeviceFileStatus
                return itm
            }
        }
        return null
    }

    fun update(value: DeviceStatus) {
        _deviceStatus.value = value
    }

    fun update(file: DeviceFileStatus) {
        if (_currentLoadingFile.value.deviceFile == file.deviceFile) {
            _currentLoadingFile.value = file
        }
    }
}
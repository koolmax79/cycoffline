package ru.koolmax.cycoffline.presentation.ui.deviceList

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResult
import ru.koolmax.cycoffline.data.DEVICE_STATUS
import ru.koolmax.cycoffline.data.DeviceFileStatus
import ru.koolmax.cycoffline.data.DeviceStatus
import ru.koolmax.cycoffline.data.db.DeviceInfo
import ru.koolmax.cycoffline.data.DeviceFileProgressListener
import ru.koolmax.cycoffline.data.ble.BLEDeviceRepository
import ru.koolmax.cycoffline.data.db.FitRepository
import ru.koolmax.cycoffline.data.db.SettingsDBRepository
import ru.koolmax.cycoffline.service.ServiceRepository
import javax.inject.Inject
import kotlin.collections.listOf

@HiltViewModel
class DeviceListViewModel @Inject constructor(
    private val repository: BLEDeviceRepository,
    private val settingsDBRepository: SettingsDBRepository,
    private val fitRepository: FitRepository,
    private val serviceRepository: ServiceRepository) : ViewModel() {

    private val savedDeviceList: StateFlow<List<DeviceInfo>> = settingsDBRepository.allDevices().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // Пауза 5 сек при сворачивании UI
        initialValue = emptyList() // Значение по умолчанию
    )

    val scanResultList = MutableStateFlow<List<BleScanResult>>(listOf())
    val deviceFileList = mutableStateListOf<DeviceFileStatus>()

    val deviceList: StateFlow<List<DeviceStatus>> = combine(savedDeviceList,
        serviceRepository.deviceStatus) { savedList, status ->
        savedList.map {
            if(it.address == status.device.address)
                DeviceStatus(it, status.status)
            else
                DeviceStatus(it)
        }
    }.stateIn(scope = viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        listOf())

    init {
        observeLoadFiles()
    }

    private fun observeLoadFiles() {
        viewModelScope.launch {
            serviceRepository.currentLoadingFile.collect { file ->
                val idx = deviceFileList.indexOfFirst { it.deviceFile == file.deviceFile }
                if(idx != -1)
                    deviceFileList[idx] = file
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        viewModelScope.launch {
            repository.startScan(viewModelScope) {
                scanResultList.emit(it)
            }
        }
    }

    fun addDevice(device: DeviceInfo) {
        viewModelScope.launch {
            settingsDBRepository.add(device)
        }
    }

    fun deleteDevice(device: DeviceInfo) {
        viewModelScope.launch {
            settingsDBRepository.delete(device)
        }
    }

    fun getInfoFromDevice(deviceInfo: DeviceInfo) {
        viewModelScope.launch {
            val listener = object : DeviceFileProgressListener() {
                override fun onConnect(device: DeviceInfo) {
                    serviceRepository.update(DeviceStatus(device.address, device.name, DEVICE_STATUS.CONNECTED))
                }

                override fun onDisconnect(device: DeviceInfo) {
                    serviceRepository.update(DeviceStatus(device.address, device.name, DEVICE_STATUS.OFFLINE))
                }
            }
            repository.connect(deviceInfo, this, listener)?.use {
                val newList = it.getFileList().map { file ->
                    if (fitRepository.contains(file.name))
                        DeviceFileStatus( file, file.size)
                    else {
                        //Log.i("cycoffline1", "not load ${file.name}")
                        DeviceFileStatus(file)
                    }
                }
                newList.forEach {status ->
                    val idx = deviceFileList.indexOfFirst { it.deviceFile == status.deviceFile }
                    if(idx == -1) {
                        deviceFileList.add(status)
                        //Log.i("cycoffline1", "add ${it.deviceFile.name}")
                    }
                    else {
                        deviceFileList[idx] = status
                        //Log.i("cycoffline1", it.deviceFile.name)
                    }
                }
            }
        }
    }

    fun saveDeviceFileToLib(file: DeviceFileStatus) {
        val idx = deviceFileList.indexOf(file)
        deviceFileList[idx] = file.copy(loadedSize = 0)
        serviceRepository.addToLoad(deviceFileList[idx])
    }
}
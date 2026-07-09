package ru.koolmax.cycoffline.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import ru.koolmax.cycoffline.data.DEVICE_STATUS
import ru.koolmax.cycoffline.data.DeviceFile
import ru.koolmax.cycoffline.data.DeviceStatus
import ru.koolmax.cycoffline.data.OnProgressListener
import ru.koolmax.cycoffline.data.ble.BLEDeviceRepository
import ru.koolmax.cycoffline.data.db.DeviceInfo
import ru.koolmax.cycoffline.data.db.FitRepository
import ru.koolmax.cycoffline.data.media.FileRepository
import ru.koolmax.cycoffline.presentation.MeasureUtil
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.toList

@AndroidEntryPoint
class DeviceFileService : Service() {

    companion object {
        private val TAG = DeviceFileService.javaClass.name
        private val LOCATION_UPDATES_INTERVAL_MS = 1.seconds.inWholeMilliseconds
        private val TICKER_PERIOD_SECONDS = 5.seconds
    }
    @Inject lateinit var deviceRepository: BLEDeviceRepository
    @Inject lateinit var fileRepository: FileRepository
    @Inject lateinit var fitRepository: FitRepository
    @Inject lateinit var serviceRepository: ServiceRepository

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Default)
    inner class LocalBinder: Binder() {
        fun getService(): DeviceFileService = this@DeviceFileService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        //setupLocationUpdates()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //val address = intent.getStringExtra("ADDRESS")
        //val name = intent.getStringExtra("NAME")

        val downloadNotifications = DownloadNotifications(this)
        startAsForegroundService(downloadNotifications.notification)
        startDownload( downloadNotifications)
        return START_NOT_STICKY//super.onStartCommand(intent, flags, startId)
    }

    /*private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Download Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Загрузка данных")
            .setContentText(file)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentIntent(pendingIntent)
            .build()
    }*/

    private fun startDownload(notification: DownloadNotifications) {
        //Log.i("FitOpener3", "start ${allFiles}")
        val listener = object : OnProgressListener() {
            private var max = 0
            private var file = ""
            private lateinit var device: DeviceInfo
            override fun onConnect(device: DeviceInfo) {
                this.device = device
                serviceRepository.update(
                    DeviceStatus(device, DEVICE_STATUS.CONNECTED)
                )
            }

            override fun onBegin(file: String, max: Int) {
                //serviceRepository.updateDeviceFileStatus(DeviceFile(file, device, max, 0))
                this.file = file
                this.max = max
            }

            override fun onStep(count: Int) {
                //Log.i("cycoffline1", "onStep = ${count}")
                serviceRepository.update(DeviceFile(file, device, max, count))
                val text = "${file} " + if(this.max > 0) MeasureUtil.getPercent(count.toFloat() / this.max.toFloat() * 100f).toList().joinToString(" ")
                    else MeasureUtil.getFileSize(count)
                notification.updateNotification(text)
            }

            override fun onFinish(file: String) {
                //Log.i("cycoffline1", "service max = ${this.max}")
                this.max = 0
            }

            override fun onDisconnect(device: DeviceInfo) {
                serviceRepository.update(DeviceStatus(device))
            }
        }
        serviceScope.launch {
                //val job = CoroutineScope(Dispatchers.IO).launch {
                //Log.i("cycoffline1", "serviceScope.launch")
            runBlocking {
                generateSequence { serviceRepository.getFileForLoad() }.forEach { file ->
                    //Log.i("cycoffline1", "get file ${file.name}")
                    //Log.i("cycoffline1", "end load ${it.name}")
                        deviceRepository.connect(file.device, this, listener)?.use { bleDevice ->
                            //Log.i("cycoffline1", "start ${file.name}")
                            val data = bleDevice.getFile(file.name)
                            //Log.i("cycoffline1", data.size.toString())
                            fileRepository.addFit(data, file.name)?.let {
                                fitRepository.add(it)
                            }
                            //Log.i("cycoffline1", "end ${file.name}")
                            delay(1000)
                        }
                        //serviceRepository.printSize()
                    }
                    //Log.i("cycoffline1", "end runBlocking")
                }
            serviceRepository.setServiceStop()
        }
            //}
            //job.join()
            //Log.i("cycoffline1", "end service")

            //val data = deviceRepository.getFile(file)
            //Log.i("FitOpener3", "start fileRepository.addFit(data, file)")
            //fileRepository.addFit(data, file)?.let {
            //    Log.i("FitOpener3", "start fitRepository.add(it)")
            //    fitRepository.add(it)
            //}
            //Log.i("FitOpener3", "finish ${file}")
            stopSelf()
        }


    private fun startAsForegroundService(notification: Notification) {
        ServiceCompat.startForeground(
            this,
            1,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
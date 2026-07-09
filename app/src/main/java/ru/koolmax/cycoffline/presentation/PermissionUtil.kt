package ru.koolmax.cycoffline.presentation

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class PermissionUtil {
    companion object {
        private val REQUEST_CODE = 1

        fun checkAndRequest(activity: Activity, permissions: Array<String>) {
            val list = permissions.filter {
                activity.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
            }.toTypedArray()
            if(!list.isEmpty())
                ActivityCompat.requestPermissions(activity, list, REQUEST_CODE)
        }
    }
}

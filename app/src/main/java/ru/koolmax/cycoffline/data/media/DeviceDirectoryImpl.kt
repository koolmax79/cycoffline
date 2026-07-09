package ru.koolmax.cycoffline.data.media

import android.net.Uri
import androidx.core.net.toUri
import java.io.File
import java.io.FilenameFilter
import java.util.Locale

object DeviceDirectoryImpl {
    fun getListUri(): List<Uri> {
        var result = mutableListOf<Uri>()
        val dir = File(directory)
        if (dir.isDirectory) {
            val filter = FilenameFilter { d: File?, s: String -> s.lowercase(Locale.getDefault()).endsWith(".fit") }
            return dir.listFiles(filter).map { it.toUri() }
        }
        return result
    }

    fun setDir(path: String) {
        directory = path
    }

    var directory: String = ""
}
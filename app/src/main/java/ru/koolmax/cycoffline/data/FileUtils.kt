package ru.koolmax.cycoffline.data

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.io.path.Path


class FileUtils(var context: Context) {
    fun getDirectory(uri: Uri): File? {
        getPath(uri)?.let {
            Log.i("TAG", it)
            return Path(it).parent.toFile()
        }
        return null
    }

    @SuppressLint("NewApi")
    fun getPath(uri: Uri): String? {
        if (isExternalStorageDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]
            var fullPath = getPathFromExtSD(split)
            if (fullPath == null || !fileExists(fullPath)) {
                Log.d(TAG, "Copy files as a fallback")
                fullPath = copyFileToInternalStorage(uri, FALLBACK_COPY_FOLDER)
            }
            return if (fullPath !== "") {
                fullPath
            } else {
                null
            }
        }
        val str = Regex("""[A-Za-z]*://[A-Za-z.]*(\S*)/""").find(uri.toString())?.groupValues?.get(1)!!
        return str.removePrefix("/root")
    }

    private fun getDriveFilePath(uri: Uri): String {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val size = java.lang.Long.toString(returnCursor.getLong(sizeIndex))
        val file = File(context.cacheDir, name)
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream: FileOutputStream = FileOutputStream(file)
            var read = 0
            val maxBufferSize = 1 * 1024 * 1024
            val bytesAvailable = inputStream!!.available()

            //int bufferSize = 1024;
            val bufferSize = Math.min(bytesAvailable, maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
            Log.e(TAG, "Size " + file.length())
            inputStream.close()
            outputStream.close()
            Log.e(TAG, "Path " + file.getPath())
            Log.e(TAG, "Size " + file.length())
        } catch (e: Exception) {
            Log.e(TAG, e.message!!)
        }
        return file.getPath()
    }

    /***
     * Used for Android Q+
     * @param uri
     * @param newDirName if you want to create a directory, you can set this variable
     * @return
     */
    private fun copyFileToInternalStorage(uri: Uri, newDirName: String): String {
        val returnCursor = context.contentResolver.query(
            uri, arrayOf(
                OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
            ), null, null, null
        )


        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val size = java.lang.Long.toString(returnCursor.getLong(sizeIndex))
        val output: File
        if (newDirName != "") {
            val random_collision_avoidance = UUID.randomUUID().toString()
            val dir =
                File(((context.filesDir.name + File.separator).toString() + newDirName + File.separator).toString() + random_collision_avoidance)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            output =
                File((((context.filesDir.name + File.separator).toString() + newDirName + File.separator).toString() + random_collision_avoidance + File.separator).toString() + name)
        } else {
            output = File((context.filesDir.name + File.separator).toString() + name)
        }
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream: FileOutputStream = FileOutputStream(output)
            var read = 0
            val bufferSize = 1024
            val buffers = ByteArray(bufferSize)
            while (inputStream!!.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
            inputStream.close()
            outputStream.close()
        } catch (e: Exception) {
            Log.e(TAG, e.message!!)
        }
        return output.getPath()
    }

    private fun getFilePathForWhatsApp(uri: Uri): String {
        return copyFileToInternalStorage(uri, "whatsapp")
    }

    private fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = "_display_name"//"_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection,
                selection, selectionArgs, null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    fun isWhatsAppFile(uri: Uri): Boolean {
        return "com.whatsapp.provider.media" == uri.authority
    }

    private fun isGoogleDriveUri(uri: Uri): Boolean {
        return "com.google.android.apps.docs.storage" == uri.authority!! || "com.google.android.apps.docs.storage.legacy" == uri.authority!!
    }

    companion object {
        var FALLBACK_COPY_FOLDER = "upload_part"
        private const val TAG = "FileUtils"
        private var contentUri: Uri? = null
        private fun fileExists(filePath: String): Boolean {
            val file = File(filePath)
            return file.exists()
        }

        private fun getPathFromExtSD(pathData: Array<String>): String? {
            val type = pathData[0]
            val relativePath: String = File.separator + pathData[1]
            var fullPath = ""
            Log.d(TAG, "MEDIA EXTSD TYPE: $type")
            Log.d(TAG, "Relative path: $relativePath")
            // on my Sony devices (4.4.4 & 5.1.1), `type` is a dynamic string
            // something like "71F8-2C0A", some kind of unique id per storage
            // don't know any API that can get the root path of that storage based on its id.
            //
            // so no "primary" type, but let the check here for other devices
            if ("primary".equals(type, ignoreCase = true)) {
                fullPath = Environment.getExternalStorageDirectory().toString() + relativePath
                if (fileExists(fullPath)) {
                    return fullPath
                }
            }
            if ("home".equals(type, ignoreCase = true)) {
                fullPath = "/storage/emulated/0/Documents$relativePath"
                if (fileExists(fullPath)) {
                    return fullPath
                }
            }

            // Environment.isExternalStorageRemovable() is `true` for external and internal storage
            // so we cannot relay on it.
            //
            // instead, for each possible path, check if file exists
            // we'll start with secondary storage as this could be our (physically) removable sd card
            fullPath = System.getenv("SECONDARY_STORAGE") + relativePath
            if (fileExists(fullPath)) {
                return fullPath
            }
            fullPath = System.getenv("EXTERNAL_STORAGE") + relativePath
            return if (fileExists(fullPath)) {
                fullPath
            } else null
        }

        private fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        private fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }
    }
}
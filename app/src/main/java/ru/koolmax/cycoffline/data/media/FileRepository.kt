package ru.koolmax.cycoffline.data.media

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.garmin.fit.Decode
import com.garmin.fit.FileIdMesgListener
import com.garmin.fit.FitRuntimeException
import com.garmin.fit.Gender
import com.garmin.fit.MesgBroadcaster
import com.garmin.fit.RecordMesgListener
import com.garmin.fit.Weight
import ru.koolmax.cycoffline.data.HeartZone
import ru.koolmax.cycoffline.data.db.FitSessionItem
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

//import javax.inject.Inject

fun Uri.getName(context: Context): String {
    val returnCursor = context.contentResolver.query(this, null, null, null, null)
    val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
    returnCursor.moveToFirst()
    val fileName = returnCursor.getString(nameIndex)
    returnCursor.close()
    return fileName
}

@Singleton
class FileRepository @Inject constructor(private val context: Context) {

    fun getRecords(name: String): FitFile? {
        val file = File(context.filesDir, name)

        val info = FitInfo()
        file.inputStream().use {
            val decode = Decode()
            val broadcast = MesgBroadcaster(decode)
            broadcast.addListeners(info)
            decode.read(it, broadcast)
        }

        file.inputStream().use {
            val records = FitFile(info)
            val decode = Decode()
            val broadcast = MesgBroadcaster(decode)
            broadcast.addListener(records)
            if (!decode.read(it, broadcast))
                return null
            records.endLoad()
            return records
        }
    }

    private fun getSession(stream: InputStream, name: String): FitSessionItem? {
        try {
            val session = FitSessionItem(name, 0)
            stream.use {
                val decode = Decode()
                val broadcast = MesgBroadcaster(decode)
                broadcast.addListener(session)
                if (!decode.read(it, broadcast))
                    return null
            }
            return session
        } catch (ex: FitRuntimeException) {
            return null
        }
    }

    private fun getFitFileId(stream: InputStream): FitFileId? {
        try {
            val fileId = FitFileId()
            stream.use {
                val decode = Decode()
                val broadcast = MesgBroadcaster(decode)
                broadcast.addListener(fileId)
                if (!decode.read(it, broadcast))
                    return null
            }
            return fileId
        } catch (ex: FitRuntimeException) {
            return null
        }
    }

    fun addFit(uri: Uri, maxHRUser: Int?, weight: Int?): FitSessionItem? {
        try {
            //for (fit in fitFileList) {
            //    if(fit.GetFile().name  == uri.getName(application!!)) return null
            //}
                context.contentResolver.openInputStream(uri)?.let {
                getFitFileId(it)?.let {
                    val outputFile = it.timeCreated.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".fit"
                    val session = context.contentResolver.openInputStream(uri)?.let {
                        getSession(it, outputFile)?.apply {
                            this.maxHRUser = maxHRUser
                            this.weightUser = weight
                        }
                    }

                    context.contentResolver.openInputStream(uri)?.let {
                        //val outputFile = File(context.filesDir, uri.getName(context))
                        val outputFile = File(context.filesDir, outputFile)
                            //if(outputFile.exists())
                        val outputStream = outputFile.outputStream()

                        it.use { input ->
                            outputStream.use { output ->
                                input.copyTo(output)
                            }
                        }
                        return session
                    }
                }
            }
            return null
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun addFit(data: ByteArray, fileName: String, birthday: LocalDate, gender: Gender, weightUser: Int?): FitSessionItem? {
        try {
            //for (fit in fitFileList) {
            //    if(fit.GetFile().name  == uri.getName(application!!)) return null
            //}
            val inputStream = ByteArrayInputStream(data)
            val session = getSession(inputStream, fileName)?.apply {
                if(this.startTime!=null)
                    this.maxHRUser = HeartZone.calcMaxHR(birthday, gender, this.startTime!!.toLocalDate())
                this.weightUser = weightUser
            }
            //if(session!=null) {
                val outputFile = File(context.filesDir, fileName)
                val outputStream = outputFile.outputStream()

                ByteArrayInputStream(data).use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
            //}
            return session
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun delFit(name: String) {
        val file = File(context.filesDir, name)
        val d = file.delete()
        //Log.i("FitOpener3", "${d} ${context.filesDir} ${name}")
    }
}
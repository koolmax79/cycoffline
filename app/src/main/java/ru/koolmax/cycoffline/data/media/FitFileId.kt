package ru.koolmax.cycoffline.data.media

import com.garmin.fit.FileIdMesg
import com.garmin.fit.FileIdMesgListener
import java.time.LocalDateTime
import java.time.ZoneId

class FitFileId: FileIdMesgListener {
    var serialNumber: Long = 0
    lateinit var timeCreated: LocalDateTime
    var productName: String = ""
    override fun onMesg(p0: FileIdMesg?) {

        p0?.let {
            serialNumber = p0.serialNumber
            timeCreated = LocalDateTime.ofInstant(p0.timeCreated.date.toInstant(), ZoneId.systemDefault())
            productName = p0.productName
        }
    }
}
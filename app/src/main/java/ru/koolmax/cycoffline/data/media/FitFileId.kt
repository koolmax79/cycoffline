package ru.koolmax.cycoffline.data.media

import android.util.Log
import com.garmin.fit.File
import com.garmin.fit.FileIdMesg
import com.garmin.fit.FileIdMesgListener
import java.time.LocalDateTime
import java.time.ZoneId

class FitFileId: FileIdMesgListener {
    var type: File = File.ACTIVITY
        private set
    var manufacturer: Int = 0
        private set
    var product: Int = 0
        private set
    var productName: String = ""
        private set
    var serialNumber: Long = 0
        private set
    var timeCreated: LocalDateTime = LocalDateTime.MIN
        private set
    var number: Int = 0
        private set
    override fun onMesg(p0: FileIdMesg?) {

        p0?.let {
            type = p0.type
            manufacturer = p0.manufacturer
            if(p0.hasField(FileIdMesg.ProductFieldNum))
                product = p0.product
            if(p0.hasField(FileIdMesg.ProductNameFieldNum))
                productName = p0.productName
            if(p0.hasField(FileIdMesg.SerialNumberFieldNum))
                serialNumber = p0.serialNumber
            timeCreated = LocalDateTime.ofInstant(p0.timeCreated.date.toInstant(), ZoneId.systemDefault())
            if(p0.hasField(FileIdMesg.NumberFieldNum))
                number = p0.number
        }
    }
}
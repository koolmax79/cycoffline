package ru.koolmax.cycoffline.data.db

import android.util.Log
import com.garmin.fit.Gender
import ru.koolmax.cycoffline.data.HeartZone
import ru.koolmax.cycoffline.data.db.DeviceInfo
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

const val BIRTHDAY = "birthday"
const val GENDER = "gender"
const val MAX_HR = "maxHR"
const val AUTO_HR = "autoHR"

@Singleton
class SettingsDBRepository @Inject constructor(private val db: FitDatabase) {
    suspend fun add(device: DeviceInfo) = db.devices().add(device)
    suspend fun delete(device: DeviceInfo) = db.devices().delete(device.address)
    fun allDevices() = db.devices().all()

    fun allSettings() = db.settings().all()
    fun getValue(key: String) = db.settings().getValue(key)
    fun setValue(itm: SettingsItem) = db.settings().setValue(itm)

    var birthday: LocalDate
        get() {
            val str = getValue(BIRTHDAY)
            try {
                return LocalDate.parse(str)
            } catch(_: Exception) {
                //setValue(SettingsItem(BIRTHDAY, "2000-01-01"))
                return LocalDate.of(2000, 1, 1)
            }
        }
        set(value) {
            setValue(SettingsItem(BIRTHDAY, value.format(DateTimeFormatter.ISO_LOCAL_DATE)))
        }

    var gender: Gender
        get() {
            val str = getValue(GENDER)
            try {
                //Log.i("cycoffline1", "get ${str}")
                return Gender.entries[Integer.parseInt(str)]
            } catch(_: Exception) {
                setValue(SettingsItem(GENDER, "1"))
                return Gender.MALE
            }
        }
        set(value) {
            setValue(SettingsItem(GENDER, value.value.toString()))
            //Log.i("cycoffline1", "set ${value.value}")
        }

    var autoHR: Boolean
        get() {
            val str = getValue(AUTO_HR)
            try {
                return str.toBoolean()
            } catch(_: Exception) {
                setValue(SettingsItem(AUTO_HR, true.toString()))
                return true
            }
        }
        set(value) {
            setValue(SettingsItem(AUTO_HR, value.toString()))
        }

    var heartZone: HeartZone
        get() {
            return if(autoHR)
                HeartZone.make(birthday, gender)
            else {
                val str = getValue(MAX_HR)
                return try {
                    HeartZone.make(birthday, gender, str!!.toInt())
                } catch(_: Exception) {
                    HeartZone.make(birthday, gender)
                }
            }
        }
        set(value) {
            setValue(SettingsItem(MAX_HR, value.maxHR.toString()))
        }
/*
    var maxHR: Int
        get() {
            val str = getValue(MAX_HR)
            try {
                return str!!.toInt()
            } catch(_: Exception) {
                setValue(SettingsItem(MAX_HR, "180"))
                return 180
            }
        }
        set(value) {
            setValue(SettingsItem(MAX_HR, value.toString()))
        }*/

    /*var minHR: Int
        get() {
            val str = getValue(MIN_HR)
            try {
                return str!!.toInt()
            } catch(_: Exception) {
                setValue(SettingsItem(MIN_HR, "80"))
                return 80
            }
        }
        set(value) {
            setValue(SettingsItem(MIN_HR, value.toString()))
        }
*/
}
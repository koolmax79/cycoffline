package ru.koolmax.cycoffline.presentation.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garmin.fit.Gender
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.koolmax.cycoffline.data.HeartZone
import ru.koolmax.cycoffline.data.db.DeviceInfo
import ru.koolmax.cycoffline.data.db.SettingsDBRepository
import ru.koolmax.cycoffline.data.db.SettingsItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repositoryDB: SettingsDBRepository
) : ViewModel() {
    val birthday = MutableStateFlow(repositoryDB.birthday)
    val gender = MutableStateFlow(repositoryDB.gender)
    val autoHR = MutableStateFlow(repositoryDB.autoHR)
    val heartZone = MutableStateFlow( repositoryDB.heartZone)

    fun setBirthday(birthday: LocalDate) {
        repositoryDB.birthday = birthday
        this.birthday.value = birthday
    }

    fun setGender(gender: Gender) {
        repositoryDB.gender = gender
        this.gender.value = gender
    }

    fun setAutoHR(value: Boolean) {
        this.autoHR.value = value
        repositoryDB.autoHR = value
        heartZone.value = repositoryDB.heartZone
    }

    fun setMaxHR(hr: Int) {
        heartZone.value = HeartZone.make(birthday.value, gender.value, hr)
        repositoryDB.heartZone = heartZone.value
    }
}
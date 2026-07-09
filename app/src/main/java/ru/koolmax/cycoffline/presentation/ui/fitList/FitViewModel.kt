package ru.koolmax.cycoffline.presentation.ui.fitList

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.koolmax.cycoffline.data.db.FitRepository
import ru.koolmax.cycoffline.data.db.FitSessionItem
import ru.koolmax.cycoffline.data.media.FileRepository
import javax.inject.Inject

@HiltViewModel
class FitViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    private val fitRepository: FitRepository,
) : ViewModel() {
    val fitSessionList: StateFlow<List<FitSessionItem>> = fitRepository.all()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Пауза 5 сек при сворачивании UI
            initialValue = emptyList() // Значение по умолчанию
        )

    fun addFitToLib(uri: Uri) {
        viewModelScope.launch {
            fileRepository.addFit(uri)?.let {
                fitRepository.add(it)
            }
        }
    }

    fun delFitSession(fit: String) {
        viewModelScope.launch {
            fileRepository.delFit(fit)
            fitRepository.delete(fit)
        }
    }
}
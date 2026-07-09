package ru.koolmax.cycoffline.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.koolmax.cycoffline.data.db.FitRepository
import javax.inject.Inject

@HiltViewModel
class AppScaffoldViewModel @Inject constructor(private val fitRepository: FitRepository,) : ViewModel() {
    val fitSesNotDisplayedCount: StateFlow<Int> = fitRepository.all().map { it.count { it.displayed==0 } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
}
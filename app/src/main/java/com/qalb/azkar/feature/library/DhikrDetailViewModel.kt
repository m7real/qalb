package com.qalb.azkar.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qalb.azkar.data.repository.LibraryRepository
import com.qalb.azkar.data.repository.OperationFailure
import com.qalb.azkar.domain.model.Dhikr
import com.qalb.azkar.domain.model.SourceReference
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DhikrDetailViewModel(
    private val repository: LibraryRepository,
    dhikrId: String,
) : ViewModel() {

    val dhikr: StateFlow<Dhikr?> = repository.observeDhikr(dhikrId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val sources: StateFlow<List<SourceReference>> = repository.observeSources(dhikrId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setFavourite(favourite: Boolean) {
        viewModelScope.launch {
            dhikr.value?.let { repository.setFavourite(it.id, favourite) }
        }
    }

    fun updateNote(note: String, onResult: (OperationFailure?) -> Unit) {
        viewModelScope.launch {
            val failure = dhikr.value?.let { repository.updatePersonalNote(it.id, note) }
            onResult(failure)
        }
    }

    fun delete(onResult: (OperationFailure?) -> Unit) {
        viewModelScope.launch {
            val failure = dhikr.value?.let { repository.deletePersonalEntry(it.id) }
            onResult(failure)
        }
    }
}

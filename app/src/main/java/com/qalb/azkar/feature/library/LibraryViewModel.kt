package com.qalb.azkar.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qalb.azkar.data.repository.LibraryFilter
import com.qalb.azkar.data.repository.LibraryRepository
import com.qalb.azkar.domain.model.Dhikr
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class LibraryListUiState(
    val query: String = "",
    val filter: LibraryFilter = LibraryFilter.ALL,
    val entries: List<Dhikr> = emptyList(),
)

/**
 * Search + content-class filtering. The library UI knows nothing about how
 * classification is derived (domain/EditorialRules + Room do that).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel(
    private val repository: LibraryRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(LibraryFilter.ALL)

    val filterFlow: StateFlow<LibraryFilter> = filter.asStateFlow()
    val queryFlow: StateFlow<String> = query.asStateFlow()

    val uiState: StateFlow<LibraryListUiState> =
        combine(query, filter) { q, f -> q to f }
            .flatMapLatest { (q, f) ->
                repository.observeLibrary(q, f)
                    .map { entries -> LibraryListUiState(query = q, filter = f, entries = entries) }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = LibraryListUiState(),
            )

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onFilterChange(value: LibraryFilter) {
        filter.value = value
    }

    /** For the collection add dialog: pick from everything, unfiltered. */
    fun observeAllEntriesForPicking(): StateFlow<List<Dhikr>> =
        repository.observeLibrary("", LibraryFilter.ALL).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )
}

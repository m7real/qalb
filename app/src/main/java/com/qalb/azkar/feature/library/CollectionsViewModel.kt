package com.qalb.azkar.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qalb.azkar.data.repository.LibraryRepository
import com.qalb.azkar.data.repository.MoveDirection
import com.qalb.azkar.data.repository.OperationFailure
import com.qalb.azkar.domain.model.Collection
import com.qalb.azkar.domain.model.CollectionDhikr
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CollectionsViewModel(
    private val repository: LibraryRepository,
) : ViewModel() {

    val collections: StateFlow<List<Collection>> = repository.observeCollections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun create(name: String, description: String?) {
        viewModelScope.launch { repository.createCollection(name, description) }
    }

    fun delete(collectionId: String) {
        viewModelScope.launch { repository.deleteCollection(collectionId) }
    }
}

class CollectionDetailViewModel(
    private val repository: LibraryRepository,
    collectionId: String,
) : ViewModel() {

    val collectionId: String = collectionId

    val items: StateFlow<List<CollectionDhikr>> = repository.observeCollectionContent(collectionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addDhikr(dhikrId: String, onResult: (OperationFailure?) -> Unit = {}) {
        viewModelScope.launch {
            onResult(repository.addToCollection(collectionId, dhikrId))
        }
    }

    fun removeItem(itemId: String) {
        viewModelScope.launch { repository.removeFromCollection(itemId) }
    }

    fun moveItem(itemId: String, direction: MoveDirection) {
        viewModelScope.launch { repository.moveCollectionItem(collectionId, itemId, direction) }
    }

    fun deleteCollection(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteCollection(collectionId)
            onDeleted()
        }
    }
}

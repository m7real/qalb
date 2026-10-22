package com.qalb.azkar.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qalb.azkar.data.repository.LibraryRepository
import com.qalb.azkar.data.repository.SaveResult
import com.qalb.azkar.domain.editor.DhikrDraft
import com.qalb.azkar.domain.model.ContentClass
import com.qalb.azkar.domain.model.OccasionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DhikrEditorUiState(
    val isLoading: Boolean = true,
    /** null => creating a new entry. */
    val existingDhikrId: String? = null,
    val arabicText: String = "",
    val transliteration: String = "",
    val bengaliMeaning: String = "",
    val notes: String = "",
    /** Raw field value; parse errors surface at save time, not while typing. */
    val repeatCount: String = "",
    val occasion: OccasionType? = null,
)

class DhikrEditorViewModel(
    private val repository: LibraryRepository,
    private val dhikrId: String?,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DhikrEditorUiState(isLoading = dhikrId != null))
    val uiState: StateFlow<DhikrEditorUiState> = _uiState.asStateFlow()

    init {
        dhikrId?.let { id ->
            viewModelScope.launch {
                repository.observeDhikr(id).collect { dhikr ->
                    if (dhikr == null) {
                        // Deleted underneath us: stay a blank create form.
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        return@collect
                    }
                    if (_uiState.value.isLoading) {
                        // Fill the form exactly once; afterwards the field values
                        // belong to the user's typing.
                        _uiState.value = DhikrEditorUiState(
                            isLoading = false,
                            existingDhikrId = if (dhikr.contentClass == ContentClass.USER_CREATED) id else null,
                            arabicText = dhikr.arabicText,
                            transliteration = dhikr.transliteration.orEmpty(),
                            bengaliMeaning = dhikr.bengaliMeaning.orEmpty(),
                            notes = dhikr.notes.orEmpty(),
                            repeatCount = dhikr.plannedRepeatCount?.toString().orEmpty(),
                            occasion = dhikr.occasionType,
                        )
                    }
                }
            }
        }
    }

    fun onArabicTextChange(value: String) = update { it.copy(arabicText = value) }
    fun onTransliterationChange(value: String) = update { it.copy(transliteration = value) }
    fun onMeaningChange(value: String) = update { it.copy(bengaliMeaning = value) }
    fun onNotesChange(value: String) = update { it.copy(notes = value) }
    fun onRepeatCountChange(value: String) = update { it.copy(repeatCount = value) }
    fun onOccasionChange(value: OccasionType?) = update { it.copy(occasion = value) }

    private fun update(transform: (DhikrEditorUiState) -> DhikrEditorUiState) {
        _uiState.value = transform(_uiState.value)
    }

    /** onSaved receives null on success, or the rejection reasons. */
    suspend fun save(onSaved: (List<String>?) -> Unit) {
        val state = _uiState.value
        val reasons = mutableListOf<String>()
        val repeat = state.repeatCount.trim().ifEmpty { null }?.let { text ->
            val parsed = text.toIntOrNull()
            if (parsed == null) {
                reasons += REPEAT_NOT_A_NUMBER
                null
            } else {
                parsed
            }
        }
        if (reasons.isNotEmpty()) {
            onSaved(reasons)
            return
        }
        val draft = DhikrDraft(
            arabicText = state.arabicText,
            transliteration = state.transliteration.ifBlank { null },
            bengaliMeaning = state.bengaliMeaning.ifBlank { null },
            notes = state.notes.ifBlank { null },
            plannedRepeatCount = repeat,
            occasionType = state.occasion,
        )
        val result = if (state.existingDhikrId != null) {
            repository.updatePersonalEntry(state.existingDhikrId, draft)
        } else {
            repository.createPersonalEntry(draft)
        }
        when (result) {
            SaveResult.Saved -> onSaved(null)
            is SaveResult.Rejected -> onSaved(result.reasons)
        }
    }

    companion object {
        /** Localizable on the screen side; kept out of domain on purpose. */
        const val REPEAT_NOT_A_NUMBER = "repeat_not_a_number"
    }
}

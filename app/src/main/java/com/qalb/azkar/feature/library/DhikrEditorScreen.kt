package com.qalb.azkar.feature.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qalb.azkar.QalbApplication
import com.qalb.azkar.R
import com.qalb.azkar.domain.model.OccasionType
import kotlinx.coroutines.launch

@Composable
private fun occasionDisplayName(occasionType: OccasionType?): String =
    when (occasionType) {
        null -> stringResource(R.string.editor_occasion_none)
        else -> occasionLabel(occasionType) ?: occasionType.name
    }

@Composable
fun DhikrEditorScreen(
    existingDhikrId: String?,
    onBack: () -> Unit,
) {
    val app = androidx.compose.ui.platform.LocalContext.current
        .applicationContext as QalbApplication
    val viewModel: DhikrEditorViewModel = viewModel(
        factory = viewModelFactory {
            initializer { DhikrEditorViewModel(app.libraryRepository, existingDhikrId) }
        },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var rejectionReasons by remember { mutableStateOf<List<String>?>(null) }
    var occasionMenuOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                )
            }
            Text(
                text = stringResource(
                    if (state.existingDhikrId != null) R.string.editor_title_edit
                    else R.string.editor_title_new,
                ),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        Text(
            text = stringResource(R.string.editor_user_note),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = state.arabicText,
            onValueChange = viewModel::onArabicTextChange,
            label = { Text(stringResource(R.string.editor_arabic_hint)) },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.transliteration,
            onValueChange = viewModel::onTransliterationChange,
            label = { Text(stringResource(R.string.editor_transliteration_hint)) },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.bengaliMeaning,
            onValueChange = viewModel::onMeaningChange,
            label = { Text(stringResource(R.string.editor_meaning_hint)) },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.notes,
            onValueChange = viewModel::onNotesChange,
            label = { Text(stringResource(R.string.editor_notes_hint)) },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.repeatCount,
            onValueChange = viewModel::onRepeatCountChange,
            label = { Text(stringResource(R.string.editor_repeat_hint)) },
            modifier = Modifier.fillMaxWidth(),
        )

        // Occasion selector — a closed vocabulary, never free text.
        OutlinedButton(onClick = { occasionMenuOpen = true }) {
            Text(
                text = stringResource(R.string.editor_occasion_label) + ": " +
                    occasionDisplayName(state.occasion),
            )
        }
        DropdownMenu(
            expanded = occasionMenuOpen,
            onDismissRequest = { occasionMenuOpen = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.editor_occasion_none)) },
                onClick = {
                    viewModel.onOccasionChange(null)
                    occasionMenuOpen = false
                },
            )
            OccasionType.entries.forEach { occasion ->
                DropdownMenuItem(
                    text = { Text(occasionDisplayName(occasion)) },
                    onClick = {
                        viewModel.onOccasionChange(occasion)
                        occasionMenuOpen = false
                    },
                )
            }
        }

        rejectionReasons?.let { reasons ->
            Column {
                reasons.forEach { reason ->
                    Text(
                        text = localReason(reason),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    scope.launch {
                        viewModel.save { reasons ->
                            if (reasons == null) {
                                onBack()
                            } else {
                                rejectionReasons = reasons
                            }
                        }
                    }
                },
            ) {
                Text(stringResource(R.string.editor_save))
            }
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.editor_cancel))
            }
        }
    }
}

/** Maps VM reason codes / messages to localized text; falls back to the raw reason. */
@Composable
private fun localReason(reason: String): String = when (reason) {
    DhikrEditorViewModel.REPEAT_NOT_A_NUMBER -> stringResource(R.string.editor_repeat_hint)
    "Arabic text must not be empty" -> stringResource(R.string.editor_arabic_hint)
    "Repetition must be at least 1 (or empty)" -> stringResource(R.string.editor_repeat_hint)
    "Repetition is unreasonably large" -> stringResource(R.string.editor_repeat_hint)
    "Curated wording is immutable; only your personal entries can be edited" ->
        stringResource(R.string.detail_curated_readonly_note)
    "Entry no longer exists" -> stringResource(R.string.library_empty_search)
    else -> reason
}

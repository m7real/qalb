package com.qalb.azkar.feature.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qalb.azkar.QalbApplication
import com.qalb.azkar.R
import com.qalb.azkar.domain.model.ContentClass
import com.qalb.azkar.domain.model.Dhikr
import com.qalb.azkar.domain.model.SourceReference

@Composable
fun DhikrDetailScreen(
    dhikrId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
) {
    val app = androidx.compose.ui.platform.LocalContext.current
        .applicationContext as QalbApplication
    val viewModel: DhikrDetailViewModel = viewModel(
        factory = viewModelFactory {
            initializer { DhikrDetailViewModel(app.libraryRepository, dhikrId) }
        },
    )
    val dhikr by viewModel.dhikr.collectAsStateWithLifecycle()
    val sources by viewModel.sources.collectAsStateWithLifecycle()

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showNoteEditor by remember { mutableStateOf(false) }

    val currentDhikr = dhikr
    if (currentDhikr == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            ContentClassBadge(currentDhikr.contentClass)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = { viewModel.setFavourite(!currentDhikr.isFavourite) },
            ) {
                Icon(
                    imageVector = if (currentDhikr.isFavourite) Icons.Filled.Favorite
                    else Icons.Filled.FavoriteBorder,
                    contentDescription = stringResource(
                        if (currentDhikr.isFavourite) R.string.detail_unfavourite
                        else R.string.detail_favourite,
                    ),
                )
            }
        }

        EntryBody(currentDhikr)

        Divider()

        Text(
            text = stringResource(R.string.detail_sources_title),
            style = MaterialTheme.typography.titleMedium,
        )
        if (sources.isEmpty()) {
            Text(
                text = stringResource(R.string.detail_no_sources),
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            sources.forEach { source -> SourceCard(source) }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = { showNoteEditor = true }) {
                Text(stringResource(R.string.detail_note_title))
            }
            if (currentDhikr.contentClass == ContentClass.USER_CREATED) {
                Button(onClick = { onEdit(currentDhikr.id) }) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.detail_edit))
                }
                Button(onClick = { showDeleteConfirm = true }) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.detail_delete))
                }
            }
        }

        if (currentDhikr.contentClass != ContentClass.USER_CREATED) {
            Text(
                text = stringResource(R.string.detail_curated_readonly_note),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.confirm_delete_dhikr)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.delete { onBack() }
                    },
                ) { Text(stringResource(R.string.action_yes)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.action_no))
                }
            },
        )
    }

    if (showNoteEditor) {
        NoteEditorDialog(
            initialNote = currentDhikr.notes.orEmpty(),
            onSave = { note ->
                showNoteEditor = false
                viewModel.updateNote(note) { }
            },
            onDismiss = { showNoteEditor = false },
        )
    }
}

@Composable
private fun EntryBody(dhikr: Dhikr) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = dhikr.arabicText,
            style = MaterialTheme.typography.displaySmall,
        )
        dhikr.transliteration?.let {
            Text(text = it, style = MaterialTheme.typography.bodyLarge)
        }
        dhikr.bengaliMeaning?.let {
            Text(text = it, style = MaterialTheme.typography.bodyLarge)
        }
        dhikr.notes?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        dhikr.plannedRepeatCount?.let {
            Text(
                text = stringResource(R.string.detail_repeat_count, it),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        dhikr.occasionType?.let { occasion ->
            Text(
                text = stringResource(
                    R.string.detail_occasion,
                    occasionLabel(occasion) ?: occasion.name,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun SourceCard(source: SourceReference) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = source.sourceName, style = MaterialTheme.typography.titleSmall)
            source.referenceText?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
            source.gradingText?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = stringResource(
                    if (source.reviewStatus == com.qalb.azkar.domain.model.ReviewStatus.VERIFIED) {
                        R.string.detail_source_verified
                    } else {
                        R.string.detail_source_needs_review
                    },
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            source.reviewNote?.let {
                Text(text = it, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun NoteEditorDialog(
    initialNote: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var note by remember { mutableStateOf(initialNote) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.detail_note_title)) },
        text = {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(R.string.detail_note_hint)) },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(onClick = { onSave(note) }) {
                Text(stringResource(R.string.detail_note_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.editor_cancel)) }
        },
    )
}

@Composable
private fun Divider() {
    Spacer(modifier = Modifier.height(1.dp))
    androidx.compose.material3.HorizontalDivider(modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(1.dp))
}

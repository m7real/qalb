package com.qalb.azkar.feature.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.qalb.azkar.data.repository.MoveDirection
import com.qalb.azkar.domain.model.CollectionDhikr

@Composable
fun CollectionDetailScreen(
    collectionId: String,
    onBack: () -> Unit,
    onOpenDhikr: (String) -> Unit,
) {
    val app = androidx.compose.ui.platform.LocalContext.current
        .applicationContext as QalbApplication
    val viewModel: CollectionDetailViewModel = viewModel(
        factory = viewModelFactory {
            initializer { CollectionDetailViewModel(app.libraryRepository, collectionId) }
        },
    )
    val items by viewModel.items.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteCollectionConfirm by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            androidx.compose.material3.ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.collection_add_dhikr)) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                }
                Text(
                    text = stringResource(R.string.collections_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { showDeleteCollectionConfirm = true }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.collection_delete),
                    )
                }
            }

            if (items.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(stringResource(R.string.collection_empty))
                }
            } else {
                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(items, key = { it.item.id }) { entry ->
                        CollectionEntryRow(
                            entry = entry,
                            onOpen = { onOpenDhikr(entry.dhikr.id) },
                            onMoveUp = { viewModel.moveItem(entry.item.id, MoveDirection.UP) },
                            onMoveDown = { viewModel.moveItem(entry.item.id, MoveDirection.DOWN) },
                            onRemove = { viewModel.removeItem(entry.item.id) },
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddDhikrToCollectionDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false },
        )
    }

    if (showDeleteCollectionConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteCollectionConfirm = false },
            title = { Text(stringResource(R.string.confirm_delete_collection)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteCollectionConfirm = false
                        viewModel.deleteCollection(onDeleted = onBack)
                    },
                ) { Text(stringResource(R.string.action_yes)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCollectionConfirm = false }) {
                    Text(stringResource(R.string.action_no))
                }
            },
        )
    }
}

@Composable
private fun CollectionEntryRow(
    entry: CollectionDhikr,
    onOpen: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clickable(onClick = onOpen)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.dhikr.arabicText,
                    style = MaterialTheme.typography.bodyLarge,
                )
                entry.dhikr.bengaliMeaning?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                ContentClassBadge(entry.dhikr.contentClass)
            }
            IconButton(onClick = onMoveUp) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = stringResource(R.string.collection_move_up),
                )
            }
            IconButton(onClick = onMoveDown) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.collection_move_down),
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.collection_remove_item),
                )
            }
        }
    }
}

@Composable
private fun AddDhikrToCollectionDialog(
    viewModel: CollectionDetailViewModel,
    onDismiss: () -> Unit,
) {
    val app = androidx.compose.ui.platform.LocalContext.current
        .applicationContext as QalbApplication
    val libraryViewModel: LibraryViewModel = viewModel(
        key = "library-pick-all",
        factory = viewModelFactory {
            initializer { LibraryViewModel(app.libraryRepository) }
        },
    )
    val allEntries by libraryViewModel.observeAllEntriesForPicking()
        .collectAsStateWithLifecycle()
    val current by viewModel.items.collectAsStateWithLifecycle()
    val alreadyMember = current.map { it.dhikr.id }.toSet()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.collection_pick_dhikr)) },
        text = {
            if (allEntries.isEmpty()) {
                Text(stringResource(R.string.library_empty))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(allEntries, key = { it.id }) { dhikr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (dhikr.id !in alreadyMember) {
                                        viewModel.addDhikr(dhikr.id)
                                        onDismiss()
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dhikr.arabicText,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                ContentClassBadge(dhikr.contentClass)
                            }
                            if (dhikr.id in alreadyMember) {
                                Text(
                                    text = "✓",
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.editor_cancel)) }
        },
    )
}

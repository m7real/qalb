package com.qalb.azkar.feature.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.qalb.azkar.data.repository.LibraryFilter
import com.qalb.azkar.domain.model.Dhikr

private data class FilterChoice(val filter: LibraryFilter, val labelRes: Int)

private val filterChoices = listOf(
    FilterChoice(LibraryFilter.ALL, R.string.filter_all),
    FilterChoice(LibraryFilter.VERIFIED_CURATED, R.string.filter_verified),
    FilterChoice(LibraryFilter.NEEDS_REVIEW, R.string.filter_needs_review),
    FilterChoice(LibraryFilter.USER_CREATED, R.string.filter_user),
    FilterChoice(LibraryFilter.FAVOURITES, R.string.filter_favourites),
)

@Composable
fun LibraryScreen(
    onOpenDetail: (String) -> Unit,
    onAddNew: () -> Unit,
    onOpenCollections: () -> Unit,
) {
    val app = androidx.compose.ui.platform.LocalContext.current
        .applicationContext as QalbApplication
    val viewModel: LibraryViewModel = viewModel(
        factory = viewModelFactory {
            initializer { LibraryViewModel(app.libraryRepository) }
        },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentFilter by viewModel.filterFlow.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddNew,
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                    )
                },
                text = { Text(stringResource(R.string.library_add_entry)) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.nav_library),
                    style = MaterialTheme.typography.titleLarge,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onOpenCollections),
                ) {
                    Icon(
                        imageVector = Icons.Filled.List,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(R.string.library_open_collections))
                }
            }

            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text(stringResource(R.string.library_search_hint)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                filterChoices.forEach { choice ->
                    FilterChip(
                        selected = currentFilter == choice.filter,
                        onClick = { viewModel.onFilterChange(choice.filter) },
                        label = { Text(stringResource(choice.labelRes)) },
                    )
                }
            }

            if (state.entries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(
                            if (state.query.isBlank()) R.string.library_empty
                            else R.string.library_empty_search,
                        ),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.entries, key = { it.id }) { dhikr ->
                        DhikrCard(
                            dhikr = dhikr,
                            onClick = { onOpenDetail(dhikr.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DhikrCard(dhikr: Dhikr, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(16.dp)) {
            ContentClassBadge(dhikr.contentClass)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = dhikr.arabicText,
                style = MaterialTheme.typography.titleMedium,
            )
            dhikr.bengaliMeaning?.let { meaning ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = meaning,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            dhikr.occasionType?.let { occasion ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.detail_occasion,
                        occasionLabel(occasion) ?: occasion.name,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

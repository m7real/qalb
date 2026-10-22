package com.qalb.azkar.feature.today

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.qalb.azkar.R

/** Placeholder — the occasion-grouped schedule arrives in Milestones 3/4. */
@Composable
fun TodayScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.nav_today),
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

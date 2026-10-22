package com.qalb.azkar.feature.library

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.qalb.azkar.R
import com.qalb.azkar.domain.model.ContentClass
import com.qalb.azkar.domain.model.OccasionType

/**
 * The single visible marker distinguishing curated-reviewed content,
 * unreviewed content and personal entries (docs/AZKAR_CONTENT_POLICY.md).
 * Text always accompanies the colour — colour alone is never the signal.
 */
@Composable
fun ContentClassBadge(contentClass: ContentClass) {
    data class BadgeStyle(val labelRes: Int, val container: Color, val content: Color)
    val style = when (contentClass) {
        ContentClass.VERIFIED_CURATED -> BadgeStyle(
            R.string.badge_verified_curated,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
        )
        ContentClass.NEEDS_REVIEW -> BadgeStyle(
            R.string.badge_needs_review,
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
        )
        ContentClass.USER_CREATED -> BadgeStyle(
            R.string.badge_user_created,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = style.container,
        contentColor = style.content,
    ) {
        Text(
            text = stringResource(style.labelRes),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun occasionLabel(occasionType: OccasionType?): String? = when (occasionType) {
    null -> null
    OccasionType.ANYTIME -> stringResource(R.string.occasion_anytime)
    OccasionType.MORNING_WINDOW -> stringResource(R.string.occasion_morning_window)
    OccasionType.EVENING_WINDOW -> stringResource(R.string.occasion_evening_window)
    OccasionType.AFTER_EACH_FARD -> stringResource(R.string.occasion_after_each_fard)
    OccasionType.AFTER_FAJR -> stringResource(R.string.occasion_after_fajr)
    OccasionType.AFTER_MAGHRIB -> stringResource(R.string.occasion_after_maghrib)
    OccasionType.BEFORE_SLEEP -> stringResource(R.string.occasion_before_sleep)
    OccasionType.CUSTOM -> stringResource(R.string.occasion_custom)
}

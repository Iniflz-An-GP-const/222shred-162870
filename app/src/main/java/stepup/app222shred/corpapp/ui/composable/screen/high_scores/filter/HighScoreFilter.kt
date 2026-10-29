package stepup.app222shred.corpapp.ui.composable.screen.high_scores.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import stepup.app222shred.corpapp.ui.composable.screen.high_scores.filter.HighScoreFilterItem
import stepup.app222shred.corpapp.ui.composable.screen.high_scores.filter.chip.HighScoreFilterChip
import stepup.app222shred.corpapp.ui.theme.LocalDimens

@Composable
fun HighScoreFilter(
    items: List<HighScoreFilterItem>,
    onSelect: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = LocalDimens.current

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = dimens.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(dimens.itemSpacing),
    ) {
        itemsIndexed(items) { index, item ->
            HighScoreFilterChip(item = item, onClick = { onSelect(index) })
        }
    }
}

package stepup.app222shred.corpapp.ui.composable.approot.background.points

import androidx.compose.ui.unit.dp
import stepup.app222shred.corpapp.ui.composable.approot.background.BackgroundPattern
import stepup.app222shred.corpapp.ui.composable.approot.background.gridAnchors

/** Many small icons in a tight 6 x 12 grid — a fine texture. */
val AppBackgroundPattern = BackgroundPattern(
    anchors = gridAnchors(columns = 6, rows = 12),
    iconSize = 24.dp,
)

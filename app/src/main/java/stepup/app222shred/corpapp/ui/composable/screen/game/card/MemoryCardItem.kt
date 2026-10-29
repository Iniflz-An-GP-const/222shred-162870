package stepup.app222shred.corpapp.ui.composable.screen.game.card

import androidx.compose.ui.graphics.vector.ImageVector
import stepup.app222shred.corpapp.data.model.CardState

data class MemoryCardItem(
    val symbol: ImageVector,
    val state: CardState,
)

package stepup.app222shred.corpapp.ui.composable.screen.game.card.back

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import stepup.app222shred.corpapp.ui.composable.screen.game.card.MemoryCardDefaults

@Composable
fun MemoryCardBack(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(MemoryCardDefaults.shape)
            .background(Brush.verticalGradient(listOf(colors.primary, colors.primaryContainer))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Extension,
            contentDescription = null,
            tint = colors.onPrimary,
            modifier = Modifier.fillMaxSize(MemoryCardDefaults.SYMBOL_SIZE_FRACTION),
        )
    }
}

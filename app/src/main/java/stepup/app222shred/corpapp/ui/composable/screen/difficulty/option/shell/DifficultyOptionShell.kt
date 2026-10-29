package stepup.app222shred.corpapp.ui.composable.screen.difficulty.option.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import stepup.app222shred.corpapp.ui.composable.screen.difficulty.option.DifficultyOptionDefaults
import stepup.app222shred.corpapp.ui.composable.screen.difficulty.option.DifficultyOptionItem
import stepup.app222shred.corpapp.ui.composable.screen.difficulty.option.difficultyOptionSurface

@Composable
fun DifficultyOptionShell(
    item: DifficultyOptionItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (DifficultyOptionItem) -> Unit,
) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .difficultyOptionSurface(
                    onClick = onClick,
                    border = DifficultyOptionDefaults.border(item.selected),
                ),
            contentAlignment = Alignment.CenterStart,
        ) {
            content(item)
        }
    }
}

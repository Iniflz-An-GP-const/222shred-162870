package stepup.app222shred.corpapp.ui.composable.screen.game.hud.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import stepup.app222shred.corpapp.ui.composable.screen.game.hud.gameHudSurface
import stepup.app222shred.corpapp.ui.theme.cardBorderStroke

@Composable
fun GameHudShell(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
        Box(
            modifier = modifier.gameHudSurface(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                border = cardBorderStroke(),
            ),
        ) {
            content()
        }
    }
}

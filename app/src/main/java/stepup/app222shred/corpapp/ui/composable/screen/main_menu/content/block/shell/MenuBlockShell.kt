package stepup.app222shred.corpapp.ui.composable.screen.main_menu.content.block.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import stepup.app222shred.corpapp.ui.composable.screen.main_menu.content.block.MenuBlockDefaults
import stepup.app222shred.corpapp.ui.composable.screen.main_menu.content.block.MenuBlockEmphasis
import stepup.app222shred.corpapp.ui.composable.screen.main_menu.content.block.menuBlockMinHeight
import stepup.app222shred.corpapp.ui.composable.screen.main_menu.content.block.menuBlockSurface

@Composable
fun MenuBlockShell(
    onClick: () -> Unit,
    emphasis: MenuBlockEmphasis,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalContentColor provides MenuBlockDefaults.emphasisContentColor(
            emphasis
        )
    ) {
        Box(
            modifier = modifier
                .menuBlockMinHeight()
                .menuBlockSurface(
                    onClick = onClick,
                    containerColor = MenuBlockDefaults.emphasisContainerColor(emphasis),
                ),
            propagateMinConstraints = true,
        ) {
            content()
        }
    }
}

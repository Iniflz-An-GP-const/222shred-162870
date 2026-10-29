package stepup.app222shred.corpapp.ui.composable.screen.main_menu.content.block.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import stepup.app222shred.corpapp.ui.composable.screen.main_menu.content.block.MenuBlockDefaults
import stepup.app222shred.corpapp.ui.composable.screen.main_menu.content.block.MenuBlockItem
import stepup.app222shred.corpapp.ui.theme.LocalDimens

@Composable
fun MenuBlockContent(
    item: MenuBlockItem,
    modifier: Modifier = Modifier,
) {
    val dimens = LocalDimens.current

    Row(
        modifier = modifier.fillMaxHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.itemSpacing),
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            modifier = Modifier.size(MenuBlockDefaults.IconSize),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(dimens.tightSpacing),
        ) {
            Text(text = item.title, style = MaterialTheme.typography.titleMedium)
            item.subtitle?.let { subtitle ->
                Text(
                    text = subtitle,
                    color = LocalContentColor.current.copy(alpha = MenuBlockDefaults.SecondaryAlpha),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

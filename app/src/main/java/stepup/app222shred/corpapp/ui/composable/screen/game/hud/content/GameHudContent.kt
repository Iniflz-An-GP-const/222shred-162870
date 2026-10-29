package stepup.app222shred.corpapp.ui.composable.screen.game.hud.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import stepup.app222shred.corpapp.R
import stepup.app222shred.corpapp.ui.composable.screen.game.hud.GameHudDefaults
import stepup.app222shred.corpapp.ui.composable.screen.game.hud.GameHudItem
import stepup.app222shred.corpapp.ui.theme.LocalDimens

private val ChipIconSize = 18.dp
private const val ChipFillAlpha = 0.12f

@Composable
fun GameHudContent(
    item: GameHudItem,
    modifier: Modifier = Modifier,
) {
    val dimens = LocalDimens.current

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimens.itemSpacing),
        ) {
            Chip(
                icon = Icons.Filled.TouchApp,
                label = stringResource(R.string.game_hud_moves_label),
                value = item.moves.toString(),
                modifier = Modifier.weight(1f),
            )
            Chip(
                icon = Icons.Filled.Timer,
                label = stringResource(R.string.game_hud_time_label),
                value = item.elapsedText,
                modifier = Modifier.weight(1f),
            )
            Chip(
                icon = Icons.Filled.Extension,
                label = stringResource(R.string.game_hud_pairs_label),
                value = GameHudDefaults.pairsText(item),
                modifier = Modifier.weight(1f),
            )
        }

        Text(
            text = GameHudDefaults.phaseText(item),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = GameHudDefaults.bestText(item),
            color = GameHudDefaults.secondaryColor(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Chip(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val dimens = LocalDimens.current

    Row(
        modifier = modifier
            .background(
                color = LocalContentColor.current.copy(alpha = ChipFillAlpha),
                shape = RoundedCornerShape(dimens.controlCornerRadius),
            )
            .padding(horizontal = dimens.itemSpacing, vertical = dimens.itemSpacing),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            dimens.tightSpacing,
            Alignment.CenterHorizontally
        ),
    ) {
        Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(ChipIconSize))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
        )
    }
}

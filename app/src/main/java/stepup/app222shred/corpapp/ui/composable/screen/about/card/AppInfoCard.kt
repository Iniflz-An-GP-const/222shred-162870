package stepup.app222shred.corpapp.ui.composable.screen.about.card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import stepup.app222shred.corpapp.BuildConfig
import stepup.app222shred.corpapp.R
import stepup.app222shred.corpapp.ui.theme.ControlDivider
import stepup.app222shred.corpapp.ui.theme.LocalDimens
import stepup.app222shred.corpapp.ui.theme.cardBorderStroke

private val InfoRowIconSize = 16.dp
private val IconBadgeSize = 32.dp
private val TrailingIconSize = 18.dp

@Composable
fun AppInfoCard(modifier: Modifier = Modifier) {
    val dimens = LocalDimens.current
    val companyName = stringResource(R.string.sphop_company_name)
    val supportLink = stringResource(R.string.sphop_customer_support_link)
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimens.cardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = cardBorderStroke(),
    ) {
        Column {
            InfoRow(
                label = stringResource(R.string.app_name),
                value = stringResource(R.string.sphop_main_menu_tagline),
            )

            if (companyName.isNotBlank()) {
                ControlDivider()

                InfoRow(
                    label = stringResource(R.string.about_developer_label),
                    value = companyName,
                )
            }

            ControlDivider()

            InfoRow(
                label = stringResource(R.string.sphop_about_title),
                value = stringResource(R.string.about_version_format, BuildConfig.VERSION_NAME),
            )

            if (supportLink.isNotBlank()) {
                ControlDivider()

                InfoRow(
                    modifier = Modifier.clickable { uriHandler.openUri(supportLink) },
                    label = stringResource(R.string.about_contact_label),
                    value = supportLink,
                    valueColor = MaterialTheme.colorScheme.primary,
                    leadingIcon = Icons.Outlined.Mail,
                    trailingIcon = Icons.AutoMirrored.Outlined.OpenInNew,
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    val dimens = LocalDimens.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.cardPadding, vertical = dimens.contentSpacing),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.contentSpacing),
    ) {
        if (leadingIcon != null) {
            Box(
                modifier = Modifier
                    .size(IconBadgeSize)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(InfoRowIconSize),
                )
            }
        }

        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )

        if (value != null) {
            Text(
                text = value,
                modifier = Modifier.weight(1f),
                color = valueColor,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (trailingIcon != null) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(TrailingIconSize),
            )
        }
    }
}

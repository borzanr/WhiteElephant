package com.whiteelephant.app.ui.setup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.whiteelephant.app.R
import com.whiteelephant.app.share.InviteChannel

/**
 * Por onde mandar o convite. Os canais diretos vêm primeiro e o seletor do
 * sistema por último: ele é o único que pode falhar sem o app ficar sabendo (ver
 * [InviteChannel]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteSheet(hasWhatsApp: Boolean, onDismiss: () -> Unit, onPick: (InviteChannel) -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 16.dp)) {
            Text(
                stringResource(R.string.invite_chooser),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            if (hasWhatsApp) {
                Option(Icons.AutoMirrored.Outlined.Chat, stringResource(R.string.invite_whatsapp)) {
                    onPick(InviteChannel.WHATSAPP)
                }
            }
            Option(Icons.Outlined.Sms, stringResource(R.string.invite_sms)) { onPick(InviteChannel.SMS) }
            Option(Icons.Outlined.ContentCopy, stringResource(R.string.invite_copy)) { onPick(InviteChannel.COPY) }
            Option(Icons.AutoMirrored.Outlined.Send, stringResource(R.string.invite_other)) { onPick(InviteChannel.OTHER) }
        }
    }
}

@Composable
private fun Option(icon: ImageVector, label: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(label, style = MaterialTheme.typography.titleMedium) },
        leadingContent = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 8.dp),
    )
}

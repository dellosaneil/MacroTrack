package org.thelazybattley.macrotrack.features.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.backup_export
import macrotrack.composeapp.generated.resources.backup_profile_backed_up
import macrotrack.composeapp.generated.resources.backup_profile_locked
import macrotrack.composeapp.generated.resources.backup_profile_not_yet
import macrotrack.composeapp.generated.resources.backup_profile_trial
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.ic_cloud_check
import macrotrack.composeapp.generated.resources.your_data
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.LastBackup
import org.thelazybattley.macrotrack.features.backup.ui.IconTile
import org.thelazybattley.macrotrack.features.backup.ui.formatBackupTime
import org.thelazybattley.macrotrack.features.pro.LocalProAccess
import org.thelazybattley.macrotrack.features.pro.ui.ProBadge
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import org.jetbrains.compose.resources.painterResource

/** The way into Backup & export, with when the last backup was saved. */
@Composable
fun ProfileYourData(
    modifier: Modifier = Modifier,
    lastBackup: LastBackup?,
    onClick: () -> Unit
) {
    val status = LocalProAccess.current.state.status
    val backupText = if (lastBackup != null) {
        stringResource(resource = Res.string.backup_profile_backed_up, formatBackupTime(millis = lastBackup.atMillis))
    } else {
        stringResource(resource = Res.string.backup_profile_not_yet)
    }
    val detail = when {
        !status.isActive -> stringResource(resource = Res.string.backup_profile_locked)
        status.isTrialActive -> stringResource(resource = Res.string.backup_profile_trial, backupText.replaceFirstChar(Char::lowercase))
        else -> backupText
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Text(text = stringResource(resource = Res.string.your_data), style = typography.bold12, color = colors.muted)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 20.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
                .clickable(role = Role.Button, onClick = onClick)
                .heightIn(min = 64.dp)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            IconTile(icon = Res.drawable.ic_cloud_check, tint = colors.accentInk, background = colors.accentSoft, size = 36)
            Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
                ) {
                    Text(text = stringResource(resource = Res.string.backup_export), style = typography.medium13, color = colors.ink)
                    if (!status.isPurchased) ProBadge()
                }
                Text(text = detail, style = typography.regular11, color = colors.muted)
            }
            Icon(
                painter = painterResource(resource = Res.drawable.ic_chevron_right),
                contentDescription = null,
                tint = colors.faint,
                modifier = Modifier.size(size = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewProfileYourData() {
    MacroTrackTheme {
        ProfileYourData(modifier = Modifier.padding(all = 16.dp), lastBackup = null, onClick = {})
    }
}

package com.drivelock.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.drivelock.app.R

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    historyCleared: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
    onOpenPermissions: () -> Unit,
    onReviewOnboarding: () -> Unit,
    onClearHistory: () -> Unit,
    onHistoryClearedConsumed: () -> Unit,
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 36.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(R.string.settings_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
            SettingsSection(R.string.settings_experience) {
                SettingsItem(R.string.settings_theme, themeMode.labelResource()) { showThemeDialog = true }
                HorizontalDivider()
                SettingsItem(R.string.settings_review_onboarding, R.string.settings_review_onboarding_description, onReviewOnboarding)
            }
            SettingsSection(R.string.settings_privacy_security) {
                SettingsItem(R.string.settings_permissions, R.string.settings_permissions_description, onOpenPermissions)
                HorizontalDivider()
                SettingsItem(R.string.settings_local_data, R.string.settings_local_data_description)
            }
            SettingsSection(R.string.settings_data) {
                SettingsItem(R.string.settings_clear_history, R.string.settings_clear_history_description) { showClearDialog = true }
            }
            SettingsSection(R.string.settings_about) {
                SettingsItem(R.string.app_name, R.string.settings_version)
                HorizontalDivider()
                SettingsItem(R.string.settings_privacy_title, R.string.settings_privacy_description)
            }
        }
    }

    if (showThemeDialog) ThemeDialog(themeMode, onThemeModeChange) { showThemeDialog = false }
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.settings_clear_confirm_title)) },
            text = { Text(stringResource(R.string.settings_clear_confirm_description)) },
            confirmButton = {
                TextButton(onClick = { showClearDialog = false; onClearHistory() }) {
                    Text(stringResource(R.string.clear), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
    if (historyCleared) {
        AlertDialog(
            onDismissRequest = onHistoryClearedConsumed,
            title = { Text(stringResource(R.string.settings_history_cleared)) },
            text = { Text(stringResource(R.string.settings_history_cleared_description)) },
            confirmButton = { TextButton(onClick = onHistoryClearedConsumed) { Text(stringResource(R.string.done)) } },
        )
    }
}

@Composable
private fun SettingsSection(title: Int, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(title), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column { content() }
        }
    }
}

@Composable
private fun SettingsItem(title: Int, description: Int, onClick: (() -> Unit)? = null) {
    Column(
        Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(description), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ThemeDialog(selected: ThemeMode, onSelect: (ThemeMode) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_theme)) },
        text = {
            Column {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onSelect(mode); onDismiss() }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = selected == mode, onClick = { onSelect(mode); onDismiss() })
                        Text(stringResource(mode.labelResource()), modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

private fun ThemeMode.labelResource(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
}

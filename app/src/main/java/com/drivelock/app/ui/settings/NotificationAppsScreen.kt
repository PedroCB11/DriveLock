package com.drivelock.app.ui.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.OutlinedTextField
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.drivelock.app.R
import com.drivelock.app.ui.components.BackButton

data class SelectableApp(val packageName: String, val label: String)

@Composable
fun NotificationAppsScreen(
    context: Context,
    state: NotificationAppsUiState,
    onQueryChange: (String) -> Unit,
    onToggle: (String) -> Unit,
    onRefreshPermission: () -> Unit,
    onBack: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) onRefreshPermission()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    Surface(color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 36.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Column(Modifier.padding(end = 52.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.notification_apps_title), style = MaterialTheme.typography.headlineLarge)
                        Text(stringResource(R.string.notification_apps_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(stringResource(R.string.notification_privacy_title), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.notification_privacy_description))
                            if (!state.accessEnabled) {
                                Button(onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }) {
                                    Text(stringResource(R.string.enable_notification_access))
                                }
                            }
                        }
                    }
                }
                item { Text(stringResource(R.string.choose_apps), style = MaterialTheme.typography.titleLarge) }
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it; onQueryChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.search_apps)) },
                        singleLine = true,
                    )
                }
                items(state.apps, key = { it.packageName }) { app ->
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(app.label, style = MaterialTheme.typography.titleMedium)
                                Text(app.packageName, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(checked = app.packageName in state.selectedPackages, onCheckedChange = { onToggle(app.packageName) })
                        }
                    }
                }
            }
            BackButton(onBack, Modifier.align(Alignment.TopEnd).padding(top = 28.dp, end = 20.dp))
        }
    }
}

package com.drivelock.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.drivelock.app.R
import com.drivelock.app.detection.MonitoringState
import com.drivelock.app.domain.model.DriveState
import java.util.Locale

@Composable
fun HomeScreen(
    state: HomeUiState,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onNotificationApps: () -> Unit,
    onRequestBackgroundLocation: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onStartMonitoring: () -> Unit,
    onReset: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Surface(color = MaterialTheme.colorScheme.background) {
      Box(Modifier.fillMaxSize()) {
      Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 36.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
        Text(stringResource(R.string.home_tagline), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if (state.driveState == DriveState.IDLE) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(10.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                    Text(stringResource(R.string.current_status), style = MaterialTheme.typography.labelLarge)
                }
                Text(stringResource(state.driveState.titleResource()), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(state.driveState.descriptionResource()), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (state.monitoringState == MonitoringState.BACKGROUND_LOCATION_PERMISSION_REQUIRED) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.background_location_title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.background_location_rationale))
                    Button(onClick = onRequestBackgroundLocation) { Text(stringResource(R.string.allow_access)) }
                }
            }
        } else if (state.monitoringState == MonitoringState.LOCATION_PERMISSION_REQUIRED) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.location_permission_title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.location_permission_rationale))
                    Button(onClick = onRequestLocationPermission) { Text(stringResource(R.string.allow_access)) }
                }
            }
        } else if (state.monitoringState == MonitoringState.LOCATION_PERMISSION_DENIED) {
            RecoveryCard(R.string.location_denied_title, R.string.location_denied_description, R.string.try_again, onRequestLocationPermission)
        } else if (state.monitoringState == MonitoringState.LOCATION_PERMISSION_PERMANENTLY_DENIED) {
            RecoveryCard(R.string.location_blocked_title, R.string.location_blocked_description, R.string.open_settings, onOpenAppSettings)
        } else if (state.monitoringState == MonitoringState.UNAVAILABLE) {
            RecoveryCard(R.string.monitoring_unavailable_title, R.string.monitoring_unavailable_description, R.string.try_again, onStartMonitoring)
        } else if (state.monitoringState == MonitoringState.STOPPED && state.driveState == DriveState.IDLE) {
            RecoveryCard(R.string.monitoring_stopped_title, R.string.monitoring_stopped_description, R.string.activate_monitoring, onStartMonitoring)
        }
        Text(stringResource(R.string.last_trip), style = MaterialTheme.typography.titleLarge)
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (state.lastTrip == null) {
                    Text(stringResource(R.string.no_completed_trips), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.first_trip_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    val trip = state.lastTrip
                    val minutes = ((trip.durationMillis ?: 0L) / 60_000L).coerceAtLeast(1L)
                    Text(stringResource(R.string.latest_trip_complete), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.trip_compact_summary, minutes, String.format(Locale.getDefault(), "%.1f", trip.distanceMeters / 1_000.0)), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
      }
      Box(Modifier.align(Alignment.TopEnd).padding(top = 28.dp, end = 20.dp)) {
          FilledTonalIconButton(onClick = { menuExpanded = true }) { Text("⚙") }
          DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
              DropdownMenuItem(text = { Text(stringResource(R.string.history)) }, onClick = { menuExpanded = false; onHistory() })
              DropdownMenuItem(text = { Text(stringResource(R.string.settings)) }, onClick = { menuExpanded = false; onSettings() })
              DropdownMenuItem(
                  text = {
                      Column {
                          Text(stringResource(R.string.notification_apps_menu))
                          Text(stringResource(R.string.notification_apps_menu_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                      }
                  },
                  onClick = { menuExpanded = false; onNotificationApps() },
              )
          }
      }
      }
    }
}

@Composable
private fun RecoveryCard(title: Int, description: Int, action: Int, onAction: () -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(description), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onAction) { Text(stringResource(action)) }
        }
    }
}

private fun DriveState.titleResource(): Int = when (this) {
    DriveState.IDLE -> R.string.status_ready
    DriveState.MOVEMENT_DETECTED -> R.string.status_movement
    DriveState.POSSIBLE_VEHICLE -> R.string.status_verifying
    DriveState.CONFIRMING_DRIVER -> R.string.status_confirmation
    DriveState.DRIVING -> R.string.status_active
    DriveState.POSSIBLE_TRIP_END -> R.string.status_finishing
}

private fun DriveState.descriptionResource(): Int = when (this) {
    DriveState.IDLE -> R.string.status_ready_description
    DriveState.MOVEMENT_DETECTED -> R.string.status_movement_description
    DriveState.POSSIBLE_VEHICLE -> R.string.status_verifying_description
    DriveState.CONFIRMING_DRIVER -> R.string.status_confirmation_description
    DriveState.DRIVING -> R.string.status_active_description
    DriveState.POSSIBLE_TRIP_END -> R.string.status_finishing_description
}

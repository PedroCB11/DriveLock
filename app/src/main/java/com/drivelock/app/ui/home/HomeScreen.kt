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
    onRequestActivityPermission: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    onReset: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background) {
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
        if (state.monitoringState == MonitoringState.ACTIVITY_PERMISSION_REQUIRED) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.activity_permission_title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.activity_permission_rationale))
                    Button(onClick = onRequestActivityPermission) { Text(stringResource(R.string.allow_access)) }
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
        } else if (state.monitoringState == MonitoringState.UNAVAILABLE) {
            Text(stringResource(R.string.activity_recognition_unavailable), color = MaterialTheme.colorScheme.error)
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
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onHistory, modifier = Modifier.weight(1f).height(54.dp)) { Text(stringResource(R.string.history)) }
            OutlinedButton(onClick = onSettings, modifier = Modifier.weight(1f).height(54.dp)) { Text(stringResource(R.string.settings)) }
        }
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

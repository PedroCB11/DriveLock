package com.drivelock.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.drivelock.app.R
import com.drivelock.app.detection.MonitoringState

@Composable
fun HomeScreen(
    state: HomeUiState,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onRequestActivityPermission: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    onReset: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
        Text(stringResource(R.string.current_status), style = MaterialTheme.typography.labelLarge)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(state.driveState.name.replace('_', ' '), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.you_are_stopped))
                Text(stringResource(R.string.automatic_activation_message))
            }
        }
        Text(stringResource(R.string.last_trip), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.no_completed_trips))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onHistory) { Text(stringResource(R.string.history)) }
            OutlinedButton(onClick = onSettings) { Text(stringResource(R.string.settings)) }
        }
        Spacer(Modifier.height(8.dp))
        if (state.monitoringState == MonitoringState.ACTIVITY_PERMISSION_REQUIRED) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.activity_permission_title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.activity_permission_rationale))
                    Button(onClick = onRequestActivityPermission) { Text(stringResource(R.string.allow_activity_access)) }
                }
            }
        } else if (state.monitoringState == MonitoringState.LOCATION_PERMISSION_REQUIRED) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.location_permission_title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.location_permission_rationale))
                    Button(onClick = onRequestLocationPermission) { Text(stringResource(R.string.allow_location_access)) }
                }
            }
        } else if (state.monitoringState == MonitoringState.UNAVAILABLE) {
            Text(stringResource(R.string.activity_recognition_unavailable), color = MaterialTheme.colorScheme.error)
        }
    }
}

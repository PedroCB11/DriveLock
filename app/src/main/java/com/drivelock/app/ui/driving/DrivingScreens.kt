package com.drivelock.app.ui.driving

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.drivelock.app.R

@Composable
fun DrivingConfirmationScreen(onDriver: () -> Unit, onPassenger: () -> Unit) {
    BackHandler(onBack = onPassenger)
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(stringResource(R.string.vehicle_detected), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Text(stringResource(R.string.are_you_driving), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(24.dp))
        Text(
            stringResource(R.string.notification_permission_explanation),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        Button(onClick = onDriver, Modifier.fillMaxWidth()) { Text(stringResource(R.string.yes_driving)) }
        OutlinedButton(onClick = onPassenger, Modifier.fillMaxWidth().padding(top = 12.dp)) { Text(stringResource(R.string.passenger)) }
    }
}

@Composable
fun ActiveDriveScreen(state: DrivingUiState, onEndTrip: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.app_name).uppercase(), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(R.string.active), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
        }
        Text("${state.speedKph.toInt()} km/h", style = MaterialTheme.typography.displayLarge)
        Text(stringResource(R.string.drive_safely), textAlign = TextAlign.Center)
        Text("%02d min     %.1f km".format(state.elapsedMinutes, state.distanceKm), style = MaterialTheme.typography.titleLarge)
        OutlinedButton(onClick = onEndTrip) { Text(stringResource(R.string.end_trip)) }
    }
}

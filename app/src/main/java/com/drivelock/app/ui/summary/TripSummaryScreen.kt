package com.drivelock.app.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.drivelock.app.R
import com.drivelock.app.ui.driving.DrivingUiState
import com.drivelock.app.ui.components.BackButton

@Composable
fun TripSummaryScreen(state: DrivingUiState, onDone: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
    ) {
        Text(stringResource(R.string.trip_completed), style = MaterialTheme.typography.headlineLarge)
        Text("${stringResource(R.string.duration)}: ${state.elapsedMinutes} min")
        Text("${stringResource(R.string.distance)}: %.1f km".format(state.distanceKm))
        Text("${stringResource(R.string.average_speed)}: %.0f km/h".format(state.averageSpeedKph))
        Text("${stringResource(R.string.maximum_speed)}: %.0f km/h".format(state.maximumSpeedKph))
        Button(onClick = onDone) { Text(stringResource(R.string.done)) }
    }
    BackButton(onDone, Modifier.align(Alignment.TopEnd).padding(top = 28.dp, end = 20.dp))
    }
}

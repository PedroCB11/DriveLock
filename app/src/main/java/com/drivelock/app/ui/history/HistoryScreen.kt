package com.drivelock.app.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.drivelock.app.R
import com.drivelock.app.domain.model.Trip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(trips: List<Trip>) {
    if (trips.isEmpty()) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(stringResource(R.string.no_trips), style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(R.string.empty_history), modifier = Modifier.padding(top = 8.dp))
        }
    } else {
        LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text(stringResource(R.string.history), style = MaterialTheme.typography.headlineLarge) }
            items(trips, key = { it.id }) { trip ->
                Column {
                    Text(formatStartTime(trip.startTime), style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${formatDuration(trip.durationMillis)} · %.1f km · %.0f km/h".format(
                            trip.distanceMeters / 1_000,
                            trip.averageSpeedKph ?: 0.0,
                        ),
                    )
                }
            }
        }
    }
}

private fun formatStartTime(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))

private fun formatDuration(durationMillis: Long?): String = "${(durationMillis ?: 0) / 60_000} min"

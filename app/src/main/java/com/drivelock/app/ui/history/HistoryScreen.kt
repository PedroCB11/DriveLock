package com.drivelock.app.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.drivelock.app.R
import com.drivelock.app.domain.model.Trip
import com.drivelock.app.ui.components.BackButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(trips: List<Trip>, onBack: () -> Unit) {
    val context = LocalContext.current
    val stats = trips.toHistoryStats()
    Surface(color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize()) {
            if (trips.isEmpty()) {
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(stringResource(R.string.no_trips), style = MaterialTheme.typography.headlineSmall)
                    Text(stringResource(R.string.empty_history), modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        Column(Modifier.padding(end = 52.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(stringResource(R.string.history), style = MaterialTheme.typography.headlineLarge)
                            Text(stringResource(R.string.history_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    item { ImpactSummary(stats) }
                    item { Text(stringResource(R.string.completed_trips), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 4.dp)) }
                    items(trips, key = { it.id }) { trip ->
                        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(formatStartTime(trip.startTime), style = MaterialTheme.typography.titleMedium)
                                Text(
                                    stringResource(
                                        R.string.trip_history_summary,
                                        formatDuration(trip.durationMillis),
                                        String.format(Locale.getDefault(), "%.1f", trip.distanceMeters / 1_000),
                                        String.format(Locale.getDefault(), "%.0f", trip.averageSpeedKph ?: 0.0),
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (trip.blockedNotifications.isNotEmpty()) {
                                    Text(
                                        stringResource(R.string.notifications_avoided, trip.blockedNotifications.values.sum()),
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.padding(top = 6.dp),
                                    )
                                    trip.blockedNotifications.entries.sortedByDescending { it.value }.forEach { (packageName, count) ->
                                        val label = runCatching {
                                            context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(packageName, 0)).toString()
                                        }.getOrDefault(packageName)
                                        Text(stringResource(R.string.notification_app_count, label, count), style = MaterialTheme.typography.bodySmall)
                                    }
                                } else {
                                    Text(stringResource(R.string.no_notifications_avoided), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
            BackButton(onBack, Modifier.align(Alignment.TopEnd).padding(top = 28.dp, end = 20.dp))
        }
    }
}

@Composable
private fun ImpactSummary(stats: HistoryStats) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(R.string.your_impact), style = MaterialTheme.typography.titleLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ImpactMetric(stats.tripCount.toString(), stringResource(R.string.trips_label))
                ImpactMetric(stats.totalDurationMinutes.toString(), stringResource(R.string.minutes_label))
                ImpactMetric(String.format(Locale.getDefault(), "%.1f", stats.totalDistanceKm), stringResource(R.string.kilometers_label))
            }
            Text(
                stringResource(R.string.total_notifications_avoided, stats.avoidedNotifications),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun ImpactMetric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatStartTime(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))

private fun formatDuration(durationMillis: Long?): String = "${(durationMillis ?: 0L) / 60_000L} min"

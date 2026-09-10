package com.drivelock.app.ui.settings

import android.os.SystemClock
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.drivelock.app.R
import com.drivelock.app.detection.DetectionDiagnostics
import com.drivelock.app.detection.MonitoringState
import com.drivelock.app.domain.model.DriveState
import com.drivelock.app.ui.components.BackButton
import java.util.Locale

@Composable
fun DetectionDiagnosticsScreen(
    diagnostics: DetectionDiagnostics,
    monitoringState: MonitoringState,
    driveState: DriveState,
    onBack: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 36.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(stringResource(R.string.diagnostics_title), style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(end = 52.dp))
                Text(stringResource(R.string.diagnostics_privacy), color = MaterialTheme.colorScheme.onSurfaceVariant)
                DiagnosticCard(R.string.diagnostics_live_data) {
                    MetricRow(R.string.diagnostics_speed, diagnostics.speedKph?.let { String.format(Locale.getDefault(), "%.1f km/h", it) } ?: "—")
                    MetricRow(R.string.diagnostics_accuracy, diagnostics.accuracyMeters?.let { String.format(Locale.getDefault(), "%.0f m", it) } ?: "—")
                    MetricRow(R.string.diagnostics_last_sample, diagnostics.sampleAgeText())
                }
                DiagnosticCard(R.string.diagnostics_state) {
                    MetricRow(R.string.diagnostics_monitor, monitoringState.name)
                    MetricRow(R.string.diagnostics_trip_state, driveState.name)
                    MetricRow(
                        R.string.diagnostics_stop_window,
                        stringResource(if (diagnostics.lowSpeedCountdownActive) R.string.active else R.string.diagnostics_inactive),
                    )
                }
                DiagnosticCard(R.string.diagnostics_configuration) {
                    MetricRow(R.string.diagnostics_threshold, String.format(Locale.getDefault(), "%.0f km/h", diagnostics.speedThresholdKph))
                    MetricRow(R.string.diagnostics_tolerance, stringResource(R.string.diagnostics_seconds, diagnostics.stopDelaySeconds))
                    MetricRow(R.string.diagnostics_accepted, diagnostics.acceptedSamples.toString())
                    MetricRow(R.string.diagnostics_rejected, diagnostics.rejectedSamples.toString())
                }
            }
            BackButton(onBack, Modifier.align(Alignment.TopEnd).padding(top = 28.dp, end = 20.dp))
        }
    }
}

@Composable
private fun DiagnosticCard(title: Int, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

@Composable
private fun MetricRow(label: Int, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(stringResource(label), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall)
    }
}

private fun DetectionDiagnostics.sampleAgeText(): String {
    val sampleTime = lastSampleElapsedRealtimeMillis ?: return "—"
    val seconds = ((SystemClock.elapsedRealtime() - sampleTime).coerceAtLeast(0)) / 1_000
    return "${seconds}s"
}

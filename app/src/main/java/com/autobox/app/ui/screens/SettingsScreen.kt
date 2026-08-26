package com.autobox.app.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autobox.app.ui.components.BatteryOptimizationCard
import com.autobox.app.ui.components.SectionHeader
import com.autobox.app.ui.theme.DarkBorder
import com.autobox.app.ui.theme.DarkSurface
import com.autobox.app.ui.theme.DarkSurfaceVariant
import com.autobox.app.ui.theme.OrangePrimary
import com.autobox.app.ui.theme.StatusGreen
import com.autobox.app.ui.theme.TextMuted
import com.autobox.app.ui.theme.TextPrimary
import com.autobox.app.ui.theme.TextSecondary
import com.autobox.app.ui.viewmodels.SettingsViewModel
import com.autobox.app.util.BatteryOptimizationHelper
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshStatus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Settings & Calibration",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Configure engine performance, exact alarm permissions, and battery policies.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Battery Optimization Warning Banner
        BatteryOptimizationCard(
            isWhitelisted = state.isBatteryWhitelisted,
            onRequestWhitelist = {
                try {
                    context.startActivity(BatteryOptimizationHelper.createIgnoreBatteryOptimizationsIntent(context))
                } catch (e: Exception) {
                    context.startActivity(BatteryOptimizationHelper.createBatteryOptimizationSettingsIntent())
                }
            }
        )

        // System Permissions Status Card
        SectionHeader(title = "System Health & Permissions")

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                PermissionStatusRow(
                    title = "Battery Optimization Exemption",
                    subtitle = if (state.isBatteryWhitelisted) "Whitelisted (High precision guaranteed)" else "Restricted (May cause Doze delay)",
                    isGranted = state.isBatteryWhitelisted,
                    actionText = if (!state.isBatteryWhitelisted) "Whitelist" else null,
                    onAction = {
                        try {
                            context.startActivity(BatteryOptimizationHelper.createIgnoreBatteryOptimizationsIntent(context))
                        } catch (_: Exception) {
                            context.startActivity(BatteryOptimizationHelper.createBatteryOptimizationSettingsIntent())
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                PermissionStatusRow(
                    title = "Exact Alarms (AlarmManager)",
                    subtitle = if (state.canScheduleExactAlarms) "Granted (T-5s precise trigger enabled)" else "Permission required on Android 12+",
                    isGranted = state.canScheduleExactAlarms,
                    actionText = if (!state.canScheduleExactAlarms) "Enable" else null,
                    onAction = {
                        context.startActivity(BatteryOptimizationHelper.createExactAlarmSettingsIntent(context))
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Snipe Strategy & Performance Calibration Card
        SectionHeader(title = "Snipe Performance Engine")

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Parallel Request Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Parallel HTTP Burst Count", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(text = "Concurrent requests dispatched at T-0s", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                    Text(
                        text = "${state.settings.burstParallelRequests}x",
                        style = MaterialTheme.typography.titleLarge,
                        color = OrangePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = state.settings.burstParallelRequests.toFloat(),
                    onValueChange = { viewModel.setBurstCount(it.roundToInt()) },
                    valueRange = 1f..8f,
                    steps = 6,
                    colors = SliderDefaults.colors(
                        thumbColor = OrangePrimary,
                        activeTrackColor = OrangePrimary,
                        inactiveTrackColor = DarkSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Latency Calibration Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Network Offset Calibration", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(text = "Compensate for RTT (Fire before T0)", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                    Text(
                        text = "${state.settings.calibrationOffsetMs} ms",
                        style = MaterialTheme.typography.titleLarge,
                        color = OrangePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = state.settings.calibrationOffsetMs.toFloat(),
                    onValueChange = { viewModel.setCalibrationOffset(it.toLong()) },
                    valueRange = -300f..300f,
                    steps = 11,
                    colors = SliderDefaults.colors(
                        thumbColor = OrangePrimary,
                        activeTrackColor = OrangePrimary,
                        inactiveTrackColor = DarkSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Background WorkManager Sync Trigger
        SectionHeader(title = "Background Sync Engine")

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Automated WorkManager Sync",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Runs every 12 hours to discover newly published classes and arm exact alarms.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.triggerManualSync() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = TextPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = OrangePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Trigger Immediate Sync Worker")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PermissionStatusRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Info,
                    contentDescription = null,
                    tint = if (isGranted) StatusGreen else OrangePrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            }
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }

        if (actionText != null && onAction != null) {
            OutlinedButton(
                onClick = onAction,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(actionText, color = OrangePrimary, fontSize = 11.sp)
            }
        }
    }
}

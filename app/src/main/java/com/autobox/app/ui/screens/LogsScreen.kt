package com.autobox.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autobox.app.data.models.SnipeLog
import com.autobox.app.ui.components.SnipeStatusBadge
import com.autobox.app.ui.theme.DarkBorder
import com.autobox.app.ui.theme.DarkSurface
import com.autobox.app.ui.theme.TextMuted
import com.autobox.app.ui.theme.TextPrimary
import com.autobox.app.ui.theme.TextSecondary
import com.autobox.app.ui.viewmodels.SettingsViewModel
import com.autobox.app.util.DateTimeUtils

@Composable
fun LogsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.logs.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Snipe Execution Logs",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${logs.size} recorded attempts",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                if (logs.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Logs", tint = TextMuted)
                    }
                }
            }
        }

        if (logs.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Snipe Records Yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextMuted
                    )
                    Text(
                        text = "When an automated or manual snipe executes, performance and latency logs will appear here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        } else {
            items(logs, key = { it.id }) { log ->
                LogItemCard(log = log)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LogItemCard(log: SnipeLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.className,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                SnipeStatusBadge(status = log.status)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = log.message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = DateTimeUtils.formatDateTime(log.timestampMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )

                Text(
                    text = "Burst Latency: ${log.durationMs}ms ${log.httpCode?.let { "• HTTP $it" } ?: ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

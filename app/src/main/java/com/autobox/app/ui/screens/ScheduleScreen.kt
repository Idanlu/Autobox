package com.autobox.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autobox.app.data.models.ScheduledSnipe
import com.autobox.app.data.models.SessionDto
import com.autobox.app.data.models.SnipeStatus
import com.autobox.app.ui.components.SnipeStatusBadge
import com.autobox.app.ui.theme.DarkBorder
import com.autobox.app.ui.theme.DarkSurface
import com.autobox.app.ui.theme.DarkSurfaceVariant
import com.autobox.app.ui.theme.OrangePrimary
import com.autobox.app.ui.theme.StatusAmber
import com.autobox.app.ui.theme.StatusGreen
import com.autobox.app.ui.theme.StatusRed
import com.autobox.app.ui.theme.TextMuted
import com.autobox.app.ui.theme.TextPrimary
import com.autobox.app.ui.theme.TextSecondary
import com.autobox.app.ui.viewmodels.ScheduleViewModel
import com.autobox.app.util.DateTimeUtils

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scheduledSnipes by viewModel.scheduledSnipes.collectAsState()

    val armedSessionIds = remember(scheduledSnipes) {
        scheduledSnipes.map { it.sessionId }.toSet()
    }

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
                        text = "Live Gym Schedule",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${state.sessions.size} classes • ${scheduledSnipes.size} armed snipers",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                IconButton(
                    onClick = { viewModel.loadSchedule() },
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color = OrangePrimary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = OrangePrimary)
                    }
                }
            }
        }

        if (state.manualSnipeMessage != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.manualSnipeMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OrangePrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Dismiss",
                            color = TextMuted,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.clickable { viewModel.clearManualMessage() }
                        )
                    }
                }
            }
        }

        if (state.sessions.isEmpty() && !state.isLoading) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Schedule Available",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextMuted
                    )
                    Text(
                        text = "Tap refresh above to fetch the upcoming weekly schedule.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        } else {
            items(state.sessions, key = { it.id }) { session ->
                val isArmed = armedSessionIds.contains(session.id)
                val isSnipingThis = state.isSnipingManualId == session.id

                SessionCard(
                    session = session,
                    isArmed = isArmed,
                    isSniping = isSnipingThis,
                    onInstantSnipe = { viewModel.manualSnipeNow(session) },
                    onArmSnipe = { viewModel.scheduleSnipeManually(session) },
                    onCancelSnipe = { viewModel.cancelScheduledSnipe(session.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SessionCard(
    session: SessionDto,
    isArmed: Boolean,
    isSniping: Boolean,
    onInstantSnipe: () -> Unit,
    onArmSnipe: () -> Unit,
    onCancelSnipe: () -> Unit
) {
    val sessionLdt = DateTimeUtils.parseSessionDateTime(session)
    val openEpochMs = DateTimeUtils.calculateBookingOpenEpochMs(session)
    val countdownStr = openEpochMs?.let { DateTimeUtils.formatCountdown(it) } ?: "Schedule TBA"

    val isAlreadyBooked = session.isBooked == true
    val isFull = (session.maxParticipants ?: 0) > 0 &&
            (session.bookedParticipants ?: 0) >= (session.maxParticipants ?: 0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isArmed) OrangePrimary.copy(alpha = 0.5f) else DarkBorder
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Time & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sessionLdt?.let { DateTimeUtils.formatLocalDateTime(it) } ?: (session.time ?: "Class"),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                if (isAlreadyBooked) {
                    SnipeStatusBadge(status = SnipeStatus.SUCCESS)
                } else if (isArmed) {
                    SnipeStatusBadge(status = SnipeStatus.SCHEDULED)
                } else {
                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = countdownStr,
                            color = if (countdownStr == "OPEN NOW") StatusGreen else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Class Name & Category
            Text(
                text = session.name ?: session.category?.name ?: "Gym Workout",
                style = MaterialTheme.typography.bodyLarge,
                color = OrangePrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Details Row: Coach & Capacity
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                session.coach?.name?.let { coach ->
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = coach,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                if (session.maxParticipants != null) {
                    Text(
                        text = "${session.bookedParticipants ?: 0}/${session.maxParticipants} booked",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isFull) StatusAmber else TextMuted
                    )
                }
            }

            if (!isAlreadyBooked) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isArmed) {
                        OutlinedButton(
                            onClick = onCancelSnipe,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.AlarmOff, contentDescription = null, modifier = Modifier.size(16.dp), tint = StatusRed)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Disarm Alarm", color = StatusRed, fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = onArmSnipe,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = TextPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp), tint = OrangePrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Arm Sniper", fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = onInstantSnipe,
                        enabled = !isSniping,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isSniping) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Snipe Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

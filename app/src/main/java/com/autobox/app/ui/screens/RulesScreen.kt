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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autobox.app.data.models.BookingRule
import com.autobox.app.ui.theme.DarkBorder
import com.autobox.app.ui.theme.DarkSurface
import com.autobox.app.ui.theme.DarkSurfaceVariant
import com.autobox.app.ui.theme.OrangePrimary
import com.autobox.app.ui.theme.StatusGreen
import com.autobox.app.ui.theme.StatusRed
import com.autobox.app.ui.theme.TextMuted
import com.autobox.app.ui.theme.TextPrimary
import com.autobox.app.ui.theme.TextSecondary
import com.autobox.app.ui.viewmodels.RulesViewModel
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun RulesScreen(
    viewModel: RulesViewModel,
    modifier: Modifier = Modifier
) {
    val rules by viewModel.rules.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Weekly Auto-Snipe Rules",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Autobox polls the gym schedule daily and sets exact alarms at (T-5s) before registration opens for all classes matching these rules.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            if (rules.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Active Booking Rules",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextMuted
                        )
                        Text(
                            text = "Tap + below to add target weekly gym times.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            } else {
                items(rules, key = { it.id }) { rule ->
                    RuleItemCard(
                        rule = rule,
                        onToggle = { enabled -> viewModel.toggleRule(rule.id, enabled) },
                        onDelete = { viewModel.deleteRule(rule.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Bottom spacing for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = OrangePrimary,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Rule")
        }
    }

    if (showAddDialog) {
        AddRuleDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { newRule ->
                viewModel.addRule(newRule)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun RuleItemCard(
    rule: BookingRule,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (rule.enabled) DarkBorder else DarkBorder.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (rule.enabled) OrangePrimary.copy(alpha = 0.2f) else DarkSurfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = rule.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US).uppercase(),
                            color = if (rule.enabled) OrangePrimary else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = String.format("%02d:%02d", rule.targetTime.hour, rule.targetTime.minute),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (rule.enabled) TextPrimary else TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                val filterDesc = if (rule.classNamePattern.isNotBlank()) {
                    "Filter: \"${rule.classNamePattern}\""
                } else {
                    "All Class Categories"
                }

                Text(
                    text = filterDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (rule.enabled) TextSecondary else TextMuted
                )

                Text(
                    text = "Opens ${rule.leadDaysBefore}d before • ${if (rule.allowWaitlist) "Auto-Standby" else "Direct Only"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = rule.enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = OrangePrimary,
                        checkedTrackColor = OrangePrimary.copy(alpha = 0.3f),
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = DarkSurfaceVariant
                    )
                )

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Rule",
                        tint = StatusRed.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddRuleDialog(
    onDismiss: () -> Unit,
    onAdd: (BookingRule) -> Unit
) {
    var selectedDay by remember { mutableStateOf(DayOfWeek.MONDAY) }
    var hour by remember { mutableIntStateOf(18) }
    var minute by remember { mutableIntStateOf(0) }
    var pattern by remember { mutableStateOf("CrossFit") }
    var leadDays by remember { mutableIntStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "Add Booking Rule",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Target Day of Week", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DayOfWeek.values().forEach { day ->
                        val isSelected = day == selectedDay
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(
                                    if (isSelected) OrangePrimary else DarkSurfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedDay = day },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.getDisplayName(TextStyle.NARROW, Locale.US),
                                color = if (isSelected) Color.Black else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Target Class Time (HH:MM)", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = String.format("%02d", hour),
                        onValueChange = { hour = it.toIntOrNull()?.coerceIn(0, 23) ?: hour },
                        label = { Text("Hour") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = String.format("%02d", minute),
                        onValueChange = { minute = it.toIntOrNull()?.coerceIn(0, 59) ?: minute },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = pattern,
                    onValueChange = { pattern = it },
                    label = { Text("Class Name Filter (Optional)") },
                    placeholder = { Text("e.g. CrossFit, WOD, Yoga") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = OrangePrimary,
                        unfocusedLabelColor = TextMuted
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Registration Window: Opens $leadDays day(s) before class",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rule = BookingRule(
                        dayOfWeek = selectedDay,
                        targetTime = LocalTime.of(hour, minute),
                        classNamePattern = pattern.trim(),
                        enabled = true,
                        leadDaysBefore = leadDays,
                        leadHoursBefore = leadDays * 24
                    )
                    onAdd(rule)
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Rule", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

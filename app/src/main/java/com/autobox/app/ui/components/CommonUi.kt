package com.autobox.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autobox.app.data.models.SnipeStatus
import com.autobox.app.ui.theme.DarkBorder
import com.autobox.app.ui.theme.DarkSurface
import com.autobox.app.ui.theme.DarkSurfaceVariant
import com.autobox.app.ui.theme.OrangePrimary
import com.autobox.app.ui.theme.StatusAmber
import com.autobox.app.ui.theme.StatusBlue
import com.autobox.app.ui.theme.StatusGreen
import com.autobox.app.ui.theme.StatusRed
import com.autobox.app.ui.theme.TextMuted
import com.autobox.app.ui.theme.TextPrimary
import com.autobox.app.ui.theme.TextSecondary

@Composable
fun BatteryOptimizationCard(
    isWhitelisted: Boolean,
    onRequestWhitelist: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isWhitelisted) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1E14)),
        border = androidx.compose.foundation.BorderStroke(1.dp, StatusAmber.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BatteryAlert,
                    contentDescription = null,
                    tint = StatusAmber,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Battery Optimization Active",
                    style = MaterialTheme.typography.titleMedium,
                    color = StatusAmber,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Android Doze mode may delay precision alarms. Please whitelist Autobox to guarantee sub-millisecond snipes.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onRequestWhitelist,
                colors = ButtonDefaults.buttonColors(containerColor = StatusAmber, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Disable Optimization", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun SnipeStatusBadge(
    status: SnipeStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label, icon) = when (status) {
        SnipeStatus.SCHEDULED -> Quadruple(
            StatusBlue.copy(alpha = 0.15f),
            StatusBlue,
            "ARMED",
            Icons.Default.Schedule
        )
        SnipeStatus.WAITING_COUNTDOWN -> Quadruple(
            StatusAmber.copy(alpha = 0.15f),
            StatusAmber,
            "SNIPING (T-5s)",
            Icons.Default.HourglassTop
        )
        SnipeStatus.EXECUTING -> Quadruple(
            OrangePrimary.copy(alpha = 0.2f),
            OrangePrimary,
            "BURSTING",
            Icons.Default.Bolt
        )
        SnipeStatus.SUCCESS -> Quadruple(
            StatusGreen.copy(alpha = 0.15f),
            StatusGreen,
            "BOOKED",
            Icons.Default.CheckCircle
        )
        SnipeStatus.WAITLISTED -> Quadruple(
            StatusAmber.copy(alpha = 0.15f),
            StatusAmber,
            "WAITLISTED",
            Icons.Default.HourglassTop
        )
        SnipeStatus.FAILED -> Quadruple(
            StatusRed.copy(alpha = 0.15f),
            StatusRed,
            "FAILED",
            Icons.Default.ErrorOutline
        )
        SnipeStatus.CANCELLED -> Quadruple(
            DarkSurfaceVariant,
            TextMuted,
            "CANCELLED",
            Icons.Default.ErrorOutline
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelLarge,
                color = OrangePrimary,
                modifier = Modifier.clickable(onClick = onActionClick)
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

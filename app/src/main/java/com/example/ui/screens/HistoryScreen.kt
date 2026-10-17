package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReminderHistoryEntity
import com.example.ui.theme.CardBorderGray
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.MutedTextGray
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftGraySurface
import com.example.ui.theme.SolidBlack
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    historyList: List<ReminderHistoryEntity>,
    onClearHistory: () -> Unit
) {
    var showConfirmClear by remember { mutableStateOf(false) }

    val completedCount = historyList.count { it.status == "COMPLETED" }
    val snoozedCount = historyList.count { it.status == "SNOOZED" }
    val dismissedCount = historyList.count { it.status == "DISMISSED" }
    val totalCount = historyList.size
    val completionRate = if (totalCount > 0) (completedCount * 100) / totalCount else 100

    val timeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // Stats Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SoftGraySurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderGray))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Weekly Activity & Reliability",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SolidBlack
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatMetric(label = "Completion", value = "$completionRate%")
                    StatMetric(label = "Completed", value = "$completedCount")
                    StatMetric(label = "Snoozed", value = "$snoozedCount")
                    StatMetric(label = "Dismissed", value = "$dismissedCount")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // History Log Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reminder Logs",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SolidBlack
            )

            if (historyList.isNotEmpty()) {
                OutlinedButton(
                    onClick = { showConfirmClear = true },
                    shape = RoundedCornerShape(50),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderGray)),
                    modifier = Modifier.testTag("clear_history_button")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear", tint = SolidBlack, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Clear", color = SolidBlack, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "No history",
                        tint = MutedTextGray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No Reminder History",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SolidBlack
                    )
                    Text(
                        text = "Completed, snoozed, and answered calls will appear here.",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = MutedTextGray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyList, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftGraySurface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderGray))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(PureWhite)
                                        .border(1.dp, CardBorderGray, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = when (item.status) {
                                            "COMPLETED" -> Icons.Default.CheckCircle
                                            "SNOOZED" -> Icons.Default.Snooze
                                            else -> Icons.Default.Close
                                        },
                                        contentDescription = item.status,
                                        tint = SolidBlack,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = item.title,
                                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SolidBlack
                                    )
                                    Text(
                                        text = timeFormat.format(Date(item.actionTimestamp)),
                                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                        color = MutedTextGray
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PureWhite)
                                    .border(1.dp, CardBorderGray, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = item.status.lowercase().capitalize(Locale.getDefault()),
                                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SolidBlack
                                )
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }

    if (showConfirmClear) {
        AlertDialog(
            onDismissRequest = { showConfirmClear = false },
            title = { Text("Clear History Logs?", fontWeight = FontWeight.Bold, color = SolidBlack) },
            text = { Text("This will delete past completion logs. Your active scheduled reminders will remain intact.", color = CharcoalBlack) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearHistory()
                        showConfirmClear = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SolidBlack, contentColor = PureWhite)
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmClear = false }) {
                    Text("Cancel", color = SolidBlack)
                }
            },
            containerColor = PureWhite
        )
    }
}

@Composable
fun StatMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = SolidBlack
        )
        Text(
            text = label,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            color = MutedTextGray
        )
    }
}

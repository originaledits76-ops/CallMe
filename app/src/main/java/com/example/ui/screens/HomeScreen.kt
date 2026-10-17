package com.example.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReminderEntity
import com.example.ui.call.IncomingCallActivity
import com.example.ui.components.NextReminderHeroCard
import com.example.ui.components.ReminderListItemCard
import com.example.ui.theme.CardBorderGray
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.MutedTextGray
import com.example.ui.theme.OffWhite
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftGraySurface
import com.example.ui.theme.SolidBlack
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    allReminders: List<ReminderEntity>,
    nextDueReminder: ReminderEntity?,
    onToggleReminder: (Long, Boolean) -> Unit,
    onCompleteReminder: (Long) -> Unit,
    onEditReminder: (ReminderEntity) -> Unit,
    onDeleteReminder: (Long) -> Unit,
    onAddNewReminder: () -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("Today") }

    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
    val dateDisplay = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())

    // Filtering reminders
    val filteredReminders = remember(allReminders, selectedFilter) {
        val now = System.currentTimeMillis()
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfToday = startOfToday + 24 * 60 * 60 * 1000L

        when (selectedFilter) {
            "Today" -> allReminders.filter { it.targetTimestamp in startOfToday..endOfToday }
            "Upcoming" -> allReminders.filter { it.targetTimestamp > endOfToday }
            else -> allReminders
        }
    }

    Scaffold(
        containerColor = PureWhite,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewReminder,
                containerColor = com.example.ui.theme.SophisticatedBlue,
                contentColor = PureWhite,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("add_reminder_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add new reminder",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Greeting & Date
            item {
                Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                    Text(
                        text = dateDisplay.uppercase(Locale.getDefault()),
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                        color = MutedTextGray,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = greeting,
                        style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = SolidBlack
                    )
                }
            }

            // Next Due Reminder Card (Hero)
            item {
                NextReminderHeroCard(
                    reminder = nextDueReminder,
                    onSimulateCall = { reminder ->
                        val intent = Intent(context, IncomingCallActivity::class.java).apply {
                            putExtra(IncomingCallActivity.EXTRA_REMINDER_ID, reminder.id)
                        }
                        context.startActivity(intent)
                    },
                    onComplete = { reminder ->
                        onCompleteReminder(reminder.id)
                    },
                    onEdit = { reminder ->
                        onEditReminder(reminder)
                    }
                )
            }

            // Filter Tabs (Today / Upcoming / All)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Today", "Upcoming", "All (${allReminders.size})").forEach { label ->
                        val rawLabel = if (label.startsWith("All")) "All" else label
                        val isSelected = selectedFilter == rawLabel
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = rawLabel },
                            label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = com.example.ui.theme.SophisticatedBlue,
                                selectedLabelColor = PureWhite,
                                containerColor = SoftGraySurface,
                                labelColor = SolidBlack
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) com.example.ui.theme.SophisticatedBlue else CardBorderGray
                            )
                        )
                    }
                }
            }

            // List of Reminders
            if (filteredReminders.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftGraySurface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderGray))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "No tasks",
                                tint = MutedTextGray,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No Reminders for $selectedFilter",
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SolidBlack
                            )
                            Text(
                                text = "Tap the + button to schedule a reminder call.",
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                color = MutedTextGray
                            )
                        }
                    }
                }
            } else {
                items(filteredReminders, key = { it.id }) { reminder ->
                    ReminderListItemCard(
                        reminder = reminder,
                        onToggle = { isEnabled -> onToggleReminder(reminder.id, isEnabled) },
                        onEdit = { onEditReminder(reminder) },
                        onDelete = { onDeleteReminder(reminder.id) },
                        onSimulateCall = {
                            val intent = Intent(context, IncomingCallActivity::class.java).apply {
                                putExtra(IncomingCallActivity.EXTRA_REMINDER_ID, reminder.id)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            // Bottom space for floating bottom bar & FAB
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

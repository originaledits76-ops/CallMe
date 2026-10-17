package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReminderEntity
import com.example.data.model.RepeatType
import com.example.ui.theme.CardBorderGray
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.LiquidGlassBackground
import com.example.ui.theme.LiquidGlassBorder
import com.example.ui.theme.LiquidGlassHighlight
import com.example.ui.theme.LiquidGlassSurface
import com.example.ui.theme.MutedTextGray
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftGraySurface
import com.example.ui.theme.SolidBlack
import com.example.ui.theme.SophisticatedBlue
import com.example.ui.theme.SophisticatedBlueBorder
import com.example.ui.theme.SophisticatedBlueGlow
import com.example.ui.theme.SophisticatedBlueSoft
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NextReminderHeroCard(
    reminder: ReminderEntity?,
    onSimulateCall: (ReminderEntity) -> Unit,
    onComplete: (ReminderEntity) -> Unit,
    onEdit: (ReminderEntity) -> Unit
) {
    if (reminder == null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0x10000000)),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = LiquidGlassSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(
                    listOf(LiquidGlassHighlight, LiquidGlassBorder)
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(PureWhite)
                        .border(1.dp, CardBorderGray, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "No upcoming reminders",
                        tint = SophisticatedBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "No Upcoming Calls",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SolidBlack
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap the + button to schedule a reminder call.",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = MutedTextGray
                )
            }
        }
        return
    }

    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTime = System.currentTimeMillis()
        }
    }

    val diffMillis = reminder.targetTimestamp - currentTime
    val countdownText = if (diffMillis <= 0) {
        "Calling Soon"
    } else {
        val totalSec = diffMillis / 1000
        val hours = totalSec / 3600
        val mins = (totalSec % 3600) / 60
        val secs = totalSec % 60
        if (hours > 0) {
            "In ${hours}h ${mins}m"
        } else if (mins > 0) {
            "In ${mins}m ${secs}s"
        } else {
            "In ${secs}s"
        }
    }

    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

    // Liquid Glass Container with subtle specular gradient & blue glow
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color(0x0C000000),
                spotColor = Color(0x18000000)
            )
            .testTag("next_reminder_hero_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = LiquidGlassSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(
                    LiquidGlassHighlight,
                    SophisticatedBlueBorder.copy(alpha = 0.25f),
                    LiquidGlassBorder
                )
            )
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Countdown Badge in Sophisticated Blue
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(SophisticatedBlue)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "NEXT CALL • $countdownText",
                        color = PureWhite,
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = timeFormat.format(Date(reminder.targetTimestamp)),
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SolidBlack
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = reminder.title,
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = SolidBlack,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (reminder.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = reminder.description,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = MutedTextGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Caller: ${reminder.callerName}",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = CharcoalBlack,
                    fontWeight = FontWeight.SemiBold
                )

                if (reminder.repeatType != RepeatType.NONE.name) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SophisticatedBlueSoft)
                            .border(1.dp, SophisticatedBlueBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = reminder.repeatType.lowercase().capitalize(Locale.getDefault()),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SophisticatedBlue
                        )
                    }
                }

                if (!reminder.voiceRecordingPath.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PureWhite)
                            .border(1.dp, CardBorderGray, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice", tint = SophisticatedBlue, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "Voice Note", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SolidBlack)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Action Buttons with smooth tactile feedback
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Test Call Button in Sophisticated Blue
                Button(
                    onClick = { onSimulateCall(reminder) },
                    modifier = Modifier
                        .weight(1.1f)
                        .height(48.dp)
                        .testTag("simulate_call_now_button"),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SophisticatedBlue,
                        contentColor = PureWhite
                    )
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Test Call", fontWeight = FontWeight.Bold)
                }

                // Complete Button
                OutlinedButton(
                    onClick = { onComplete(reminder) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("complete_hero_button"),
                    shape = RoundedCornerShape(24.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CardBorderGray)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = PureWhite)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Complete", tint = SolidBlack, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Complete", color = SolidBlack, fontWeight = FontWeight.SemiBold)
                }

                // Edit Button
                IconButton(
                    onClick = { onEdit(reminder) },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PureWhite)
                        .border(1.dp, CardBorderGray, CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit reminder", tint = SolidBlack, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun ReminderListItemCard(
    reminder: ReminderEntity,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSimulateCall: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "card_press_scale"
    )

    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = Color(0x0C000000)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onEdit() }
            .testTag("reminder_card_${reminder.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = LiquidGlassSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(LiquidGlassHighlight, LiquidGlassBorder)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = timeFormat.format(Date(reminder.targetTimestamp)),
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (reminder.isEnabled) SolidBlack else MutedTextGray
                    )

                    if (reminder.isSnoozed) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PureWhite)
                                .border(1.dp, CardBorderGray, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Snoozed", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SolidBlack)
                        }
                    }

                    if (reminder.repeatType != RepeatType.NONE.name) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SophisticatedBlueSoft)
                                .border(1.dp, SophisticatedBlueBorder.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                reminder.repeatType.lowercase().capitalize(Locale.getDefault()),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SophisticatedBlue
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = reminder.title,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (reminder.isEnabled) SolidBlack else MutedTextGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${reminder.callerName} • ${dateFormat.format(Date(reminder.targetTimestamp))}",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = MutedTextGray
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Switch with Sophisticated Blue active tint
                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PureWhite,
                        checkedTrackColor = SophisticatedBlue,
                        uncheckedThumbColor = SolidBlack,
                        uncheckedTrackColor = PureWhite,
                        uncheckedBorderColor = CardBorderGray
                    ),
                    modifier = Modifier.testTag("toggle_reminder_${reminder.id}")
                )

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = CharcoalBlack
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(PureWhite)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Simulate Call Now", color = SolidBlack) },
                            onClick = {
                                showMenu = false
                                onSimulateCall()
                            },
                            leadingIcon = { Icon(Icons.Default.Call, contentDescription = null, tint = SophisticatedBlue) }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Reminder", color = SolidBlack) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = SolidBlack) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = SolidBlack) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = SolidBlack) }
                        )
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.InterpreterMode
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.TtsPlaybackManager
import com.example.data.model.ReminderEntity
import com.example.data.model.RepeatType
import com.example.scheduling.ScheduleCalculator
import com.example.ui.theme.BackgroundColor
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandNavy
import com.example.ui.theme.DarkNeutral
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.InputBackground
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SubtleText
import com.example.ui.theme.SurfaceCard
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class CallingPersona(
    val id: String,
    val name: String,
    val initial: String,
    val badge: String,
    val description: String,
    val isWarning: Boolean = false
)

@Composable
fun ScheduleCallScreen(
    initialReminder: ReminderEntity? = null,
    defaultCallerName: String = "Personal Assistant",
    onSaveCall: (ReminderEntity) -> Unit,
    onTriggerTestCall: (ReminderEntity) -> Unit
) {
    val context = LocalContext.current
    val ttsManager = remember { TtsPlaybackManager(context) }

    var callPurpose by remember {
        mutableStateOf(initialReminder?.title ?: "Take Blood Pressure Medication")
    }
    var callerName by remember {
        mutableStateOf(initialReminder?.callerName ?: defaultCallerName)
    }
    var messageScript by remember {
        mutableStateOf(
            initialReminder?.description?.ifBlank { null }
                ?: "Hi David, this is your CallMinder alert to take your 20mg Lisinopril with a full glass of water. Press 1 to acknowledge you've taken it, or press 2 to snooze for 15 minutes."
        )
    }

    // Numpad Key Actions
    var numpad1Action by remember { mutableStateOf("Acknowledge & Complete") }
    var numpad2Action by remember { mutableStateOf("Snooze 15 Mins") }
    var numpad3Action by remember { mutableStateOf("Repeat Message Script") }

    // Time states
    var selectedHour by remember { mutableIntStateOf(8) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var isAm by remember { mutableStateOf(true) }

    // Repeat day states (0 = Sunday, 1 = Monday, etc.)
    var selectedDayIndex by remember { mutableIntStateOf(1) } // Default to Monday (19)

    // Persona selection
    val personas = remember {
        listOf(
            CallingPersona("maya", "Maya", "M", "Gentle & Calming", "Warm reminder tone for meds and morning routine."),
            CallingPersona("marcus", "Marcus", "M", "Assertive Wake-up", "Deep resonant voice that won't let you stay asleep."),
            CallingPersona("alex", "Alex", "A", "Executive Crisp", "Paced, concise briefing style for high-stakes tasks."),
            CallingPersona("urgent", "Urgent Siren", "!", "Critical Alarm", "High-pitch pulsing intro with repeated prompts.", isWarning = true)
        )
    }
    var selectedPersona by remember { mutableStateOf(personas[0]) }

    // Delivery & Urgency toggles
    var breakthroughSilent by remember { mutableStateOf(true) }
    var requireKeypad by remember { mutableStateOf(true) }
    var retryPolicy by remember { mutableStateOf("Retry every 2 mins until answered (Max 3)") }
    var isRetryDropdownOpen by remember { mutableStateOf(false) }

    // Audio preview state
    var isPlayingPreview by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            ttsManager.shutdown()
        }
    }

    // Compute display time string
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
    val amPmString = if (isAm) "AM" else "PM"
    val timeZoneString = TimeZone.getDefault().displayName

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Title Section
        Column(modifier = Modifier.padding(top = 4.dp)) {
            Text(
                text = "Schedule Call",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Synthesizes natural voice audio and rings your carrier line at the scheduled instant.",
                fontSize = 12.sp,
                color = SecondaryText,
                lineHeight = 17.sp
            )
        }

        // Card 1: Call Purpose Input
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderLight))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Call Purpose",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryText
                    )
                    Text(
                        text = "Max 40 chars",
                        fontSize = 11.sp,
                        color = SubtleText,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = callPurpose,
                    onValueChange = { if (it.length <= 40) callPurpose = it },
                    placeholder = {
                        Text(
                            text = "e.g. Morning Medication or Flight Check-in",
                            fontSize = 13.sp,
                            color = SubtleText
                        )
                    },
                    trailingIcon = {
                        if (callPurpose.isNotBlank()) {
                            IconButton(onClick = { callPurpose = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = SubtleText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("call_purpose_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkNeutral,
                        unfocusedBorderColor = BorderSubtle,
                        focusedContainerColor = InputBackground,
                        unfocusedContainerColor = InputBackground,
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText
                    )
                )
            }
        }

        // Card: Caller Name Input
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderLight))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CALLER NAME",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Shown on incoming screen",
                        fontSize = 11.sp,
                        color = SecondaryText
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = callerName,
                    onValueChange = { if (it.length <= 30) callerName = it },
                    placeholder = {
                        Text(
                            text = "e.g. Dr. Roberts or Personal Assistant",
                            fontSize = 13.sp,
                            color = SubtleText
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("caller_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkNeutral,
                        unfocusedBorderColor = BorderSubtle,
                        focusedContainerColor = InputBackground,
                        unfocusedContainerColor = InputBackground,
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText
                    )
                )
            }
        }

        // Card: Numpad Actions Configurator
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderLight))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NUMPAD KEY ACTIONS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "What pressing 1, 2, 3 does",
                        fontSize = 11.sp,
                        color = SecondaryText
                    )
                }

                NumpadActionRow(keyNum = "1", action = numpad1Action) { newAction -> numpad1Action = newAction }
                NumpadActionRow(keyNum = "2", action = numpad2Action) { newAction -> numpad2Action = newAction }
                NumpadActionRow(keyNum = "3", action = numpad3Action) { newAction -> numpad3Action = newAction }
            }
        }

        // Card 2: Time & Schedule Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderLight))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = SecondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "DISPATCH TIME",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryText,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Text(
                        text = "In 9h 12m",
                        fontSize = 11.sp,
                        color = SecondaryText,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Clean Minimalist Clock
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(InputBackground)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .clickable {
                            val hour24 = if (isAm) {
                                if (selectedHour == 12) 0 else selectedHour
                            } else {
                                if (selectedHour == 12) 12 else selectedHour + 12
                            }
                            android.app.TimePickerDialog(
                                context,
                                { _, h, m ->
                                    isAm = h < 12
                                    selectedHour = when {
                                        h == 0 -> 12
                                        h > 12 -> h - 12
                                        else -> h
                                    }
                                    selectedMinute = m
                                },
                                hour24,
                                selectedMinute,
                                false
                            ).show()
                        }
                        .padding(vertical = 18.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = timeFormatted,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryText,
                                letterSpacing = (-1).sp
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isAm) DarkNeutral else Color.Transparent)
                                        .clickable { isAm = true }
                                        .padding(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "AM",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAm) PureWhite else SubtleText
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (!isAm) DarkNeutral else Color.Transparent)
                                        .clickable { isAm = false }
                                        .padding(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "PM",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isAm) PureWhite else SubtleText
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = SubtleText,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = timeZoneString,
                                fontSize = 11.sp,
                                color = SubtleText
                            )
                        }
                    }
                }

                // Repeat Day Minimal Horizon
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = "REPEAT DAY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = SecondaryText,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val daysData = listOf(
                        Triple("S", "18", 0),
                        Triple("M", "19", 1),
                        Triple("T", "20", 2),
                        Triple("W", "21", 3),
                        Triple("T", "22", 4),
                        Triple("F", "23", 5),
                        Triple("S", "24", 6)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        daysData.forEach { (dayLetter, dateNum, index) ->
                            val isSelected = selectedDayIndex == index
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) DarkNeutral else InputBackground)
                                    .border(
                                        1.dp,
                                        if (isSelected) DarkNeutral else BorderSubtle,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedDayIndex = index }
                                    .padding(vertical = 8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayLetter,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        color = if (isSelected) PureWhite else SecondaryText
                                    )
                                    Text(
                                        text = dateNum,
                                        fontSize = 10.sp,
                                        color = if (isSelected) PureWhite.copy(alpha = 0.7f) else SubtleText,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Card 3: Spoken Message Script Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderLight))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = DarkNeutral,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "MESSAGE SCRIPT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryText,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Text(
                        text = "~18s duration",
                        fontSize = 11.sp,
                        color = SecondaryText,
                        fontFamily = FontFamily.Monospace
                    )
                }

                OutlinedTextField(
                    value = messageScript,
                    onValueChange = { if (it.length <= 300) messageScript = it },
                    maxLines = 4,
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("message_script_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkNeutral,
                        unfocusedBorderColor = BorderSubtle,
                        focusedContainerColor = InputBackground,
                        unfocusedContainerColor = InputBackground,
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Natural pauses detected",
                            fontSize = 11.sp,
                            color = EmeraldDark
                        )
                    }

                    Text(
                        text = "${messageScript.length} / 300",
                        fontSize = 11.sp,
                        color = SubtleText,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Audio Waveform Delight Player
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(InputBackground)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Play button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DarkNeutral)
                                .clickable {
                                    if (isPlayingPreview) {
                                        ttsManager.stop()
                                        isPlayingPreview = false
                                    } else {
                                        isPlayingPreview = true
                                        ttsManager.speak(messageScript) {
                                            isPlayingPreview = false
                                        }
                                    }
                                }
                                .testTag("preview_play_button")
                        ) {
                            Icon(
                                imageVector = if (isPlayingPreview) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play voice preview",
                                tint = PureWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Voice Preview",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = PrimaryText
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(BorderLight)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "HD Neural",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = DarkNeutral
                                        )
                                    }
                                }

                                Text(
                                    text = if (isPlayingPreview) "Playing..." else "0:00 / 0:18",
                                    fontSize = 11.sp,
                                    color = SubtleText,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Minimal Geometric Waveform Bars
                            WaveformBarVisualizer(isPlaying = isPlayingPreview)
                        }
                    }
                }
            }
        }

        // Card 4: Calling Persona Selector (2x2 Grid)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderLight))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.InterpreterMode,
                            contentDescription = null,
                            tint = DarkNeutral,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "CALLING PERSONA",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryText,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Text(
                        text = "4 options",
                        fontSize = 11.sp,
                        color = SubtleText
                    )
                }

                // 2x2 Grid of Persona Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PersonaCard(
                        persona = personas[0],
                        isSelected = selectedPersona == personas[0],
                        modifier = Modifier.weight(1f)
                    ) { selectedPersona = personas[0] }

                    PersonaCard(
                        persona = personas[1],
                        isSelected = selectedPersona == personas[1],
                        modifier = Modifier.weight(1f)
                    ) { selectedPersona = personas[1] }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PersonaCard(
                        persona = personas[2],
                        isSelected = selectedPersona == personas[2],
                        modifier = Modifier.weight(1f)
                    ) { selectedPersona = personas[2] }

                    PersonaCard(
                        persona = personas[3],
                        isSelected = selectedPersona == personas[3],
                        modifier = Modifier.weight(1f)
                    ) { selectedPersona = personas[3] }
                }
            }
        }

        // Card 5: Telephony Breakthrough & Urgency Rules
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderLight))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = DarkNeutral,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "DELIVERY & URGENCY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText,
                        letterSpacing = 0.5.sp
                    )
                }

                // Toggle 1: Breakthrough Silent Mode
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(InputBackground)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoNotDisturbOn,
                                contentDescription = null,
                                tint = DarkNeutral,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Breakthrough Silent Mode",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PrimaryText
                                )
                                Text(
                                    text = "Overrides iOS Focus & Android DND priority",
                                    fontSize = 11.sp,
                                    color = SecondaryText
                                )
                            }
                        }

                        Switch(
                            checked = breakthroughSilent,
                            onCheckedChange = { breakthroughSilent = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureWhite,
                                checkedTrackColor = DarkNeutral,
                                uncheckedThumbColor = DarkNeutral,
                                uncheckedTrackColor = InputBackground
                            )
                        )
                    }
                }

                // Retry Dropdown Configuration
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = null,
                            tint = SubtleText,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Retry If Unanswered",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = SecondaryText
                        )
                    }

                    Box {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(InputBackground)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                .clickable { isRetryDropdownOpen = true }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = retryPolicy,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PrimaryText
                                )
                                Icon(
                                    imageVector = Icons.Default.ExpandMore,
                                    contentDescription = "Expand",
                                    tint = SubtleText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isRetryDropdownOpen,
                            onDismissRequest = { isRetryDropdownOpen = false },
                            modifier = Modifier.background(PureWhite)
                        ) {
                            listOf(
                                "Retry every 2 mins until answered (Max 3)",
                                "Retry once after 5 mins",
                                "Escalate to emergency contact on 2nd miss",
                                "Do not retry (Single call attempt)"
                            ).forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, fontSize = 12.sp, color = PrimaryText) },
                                    onClick = {
                                        retryPolicy = option
                                        isRetryDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Toggle 2: Keypad Confirmation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(InputBackground)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dialpad,
                                contentDescription = null,
                                tint = DarkNeutral,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Require Keypad \"Press 1\"",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PrimaryText
                                )
                                Text(
                                    text = "Ensures voicemail is not logged as completed",
                                    fontSize = 11.sp,
                                    color = SecondaryText
                                )
                            }
                        }

                        Switch(
                            checked = requireKeypad,
                            onCheckedChange = { requireKeypad = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureWhite,
                                checkedTrackColor = DarkNeutral,
                                uncheckedThumbColor = DarkNeutral,
                                uncheckedTrackColor = InputBackground
                            )
                        )
                    }
                }
            }
        }

        // Card 6: Trust Note
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(BorderLight.copy(alpha = 0.4f))
                .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = SecondaryText,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = "Carrier Verified Caller ID",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryText
                    )
                    Text(
                        text = "Originates via verified toll-free route (+1-888-CALLMND) with 99.98% reachability.",
                        fontSize = 10.sp,
                        color = SecondaryText
                    )
                }
            }
        }

        // Action Buttons: Save & Trigger Immediate Test Call
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Primary Save Button
            Button(
                onClick = {
                    if (callPurpose.isBlank()) {
                        Toast.makeText(context, "Please enter a call purpose", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val cal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, if (isAm) (selectedHour % 12) else (selectedHour % 12) + 12)
                        set(Calendar.MINUTE, selectedMinute)
                        set(Calendar.SECOND, 0)
                        if (timeInMillis <= System.currentTimeMillis()) {
                            add(Calendar.DAY_OF_YEAR, 1)
                        }
                    }

                    val reminder = ReminderEntity(
                        id = initialReminder?.id ?: 0L,
                        title = callPurpose.trim(),
                        description = messageScript.trim(),
                        targetTimestamp = cal.timeInMillis,
                        callerName = callerName.trim().ifBlank { "Personal Assistant" },
                        repeatType = RepeatType.DAILY.name,
                        repeatDaysMask = 1 shl selectedDayIndex,
                        isEnabled = true
                    )
                    onSaveCall(reminder)
                    Toast.makeText(context, "Call reminder scheduled for ${timeFormatted} ${amPmString}", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                    .testTag("set_call_reminder_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkNeutral,
                    contentColor = PureWhite
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneInTalk,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Set Call Reminder",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "| ${timeFormatted} ${amPmString} Mon",
                    fontSize = 12.sp,
                    color = SubtleText,
                    fontWeight = FontWeight.Normal
                )
            }

            // Secondary Immediate Test Call
            Text(
                text = "Trigger Immediate Test Call",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = SubtleText,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable {
                        val testReminder = ReminderEntity(
                            id = -99L,
                            title = callPurpose.ifBlank { "Take Blood Pressure Medication" },
                            description = messageScript,
                            targetTimestamp = System.currentTimeMillis(),
                            callerName = selectedPersona.name
                        )
                        onTriggerTestCall(testReminder)
                    }
                    .padding(vertical = 4.dp)
                    .testTag("trigger_test_call_text")
            )
        }
    }
}

@Composable
fun QuickPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) DarkNeutral else PureWhite)
            .border(
                1.dp,
                if (isSelected) DarkNeutral else BorderLight,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) PureWhite else SecondaryText
        )
    }
}

@Composable
fun PersonaCard(
    persona: CallingPersona,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) DarkNeutral else InputBackground)
            .border(
                1.dp,
                if (isSelected) DarkNeutral else BorderSubtle,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) PureWhite.copy(alpha = 0.2f) else BorderLight)
                ) {
                    if (persona.isWarning) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isSelected) PureWhite else DarkNeutral,
                            modifier = Modifier.size(13.dp)
                        )
                    } else {
                        Text(
                            text = persona.initial,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) PureWhite else DarkNeutral
                        )
                    }
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = PureWhite,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = persona.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) PureWhite else PrimaryText
            )
            Text(
                text = persona.badge,
                fontSize = 10.sp,
                color = if (isSelected) SubtleText else SecondaryText
            )
            Text(
                text = persona.description,
                fontSize = 10.sp,
                color = if (isSelected) SubtleText else SubtleText,
                lineHeight = 13.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
fun WaveformBarVisualizer(isPlaying: Boolean) {
    val heights = listOf(8, 12, 6, 16, 10, 8, 14, 16, 10, 6, 12, 16, 8, 4, 12, 6)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.height(16.dp)
    ) {
        heights.forEachIndexed { index, baseHeight ->
            val barHeight = if (isPlaying) {
                val animatedHeight by rememberInfiniteTransition(label = "bar_$index").animateFloat(
                    initialValue = 4f,
                    targetValue = baseHeight.toFloat(),
                    animationSpec = infiniteRepeatable(
                        animation = tween(300 + (index * 40), easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "h_$index"
                )
                animatedHeight.dp
            } else {
                baseHeight.dp
            }

            val isFilled = index < 5 || isPlaying
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(barHeight)
                    .clip(CircleShape)
                    .background(if (isFilled) DarkNeutral else BorderLight)
            )
        }
    }
}

@Composable
fun NumpadActionRow(keyNum: String, action: String, onActionChanged: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val actions = listOf("Acknowledge & Complete", "Snooze 15 Mins", "Repeat Message Script", "Call Back Later", "Dismiss & Mute")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(InputBackground)
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .clickable { expanded = true }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(DarkNeutral)
            ) {
                Text(text = keyNum, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PureWhite)
            }
            Text(text = "Press $keyNum Action", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
        }

        Text(text = action, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkNeutral)

        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(PureWhite)
        ) {
            actions.forEach { act ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(act, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryText) },
                    onClick = {
                        onActionChanged(act)
                        expanded = false
                    }
                )
            }
        }
    }
}

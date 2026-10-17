package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.core.content.ContextCompat
import com.example.audio.VoiceRecordingManager
import com.example.data.model.ReminderEntity
import com.example.data.model.RepeatType
import com.example.scheduling.ScheduleCalculator
import com.example.ui.call.IncomingCallActivity
import com.example.ui.theme.CardBorderGray
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.MutedTextGray
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftGraySurface
import com.example.ui.theme.SolidBlack
import com.example.ui.theme.SophisticatedBlue
import com.example.ui.theme.SophisticatedBlueSoft
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderEditSheet(
    initialReminder: ReminderEntity? = null,
    defaultCallerName: String = "Personal Assistant",
    defaultSnoozeMinutes: Int = 10,
    onDismissRequest: () -> Unit,
    onSave: (ReminderEntity) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf(initialReminder?.title ?: "") }
    var description by remember { mutableStateOf(initialReminder?.description ?: "") }
    var callerName by remember { mutableStateOf(initialReminder?.callerName ?: defaultCallerName) }
    var repeatType by remember {
        mutableStateOf(
            try {
                RepeatType.valueOf(initialReminder?.repeatType ?: RepeatType.NONE.name)
            } catch (e: Exception) {
                RepeatType.NONE
            }
        )
    }
    var repeatDaysMask by remember { mutableIntStateOf(initialReminder?.repeatDaysMask ?: 0) }
    var snoozeMinutes by remember { mutableIntStateOf(initialReminder?.snoozeDurationMinutes ?: defaultSnoozeMinutes) }
    var targetTimestamp by remember {
        mutableLongStateOf(
            initialReminder?.targetTimestamp ?: (System.currentTimeMillis() + 15 * 60 * 1000L)
        )
    }
    var voiceRecordingPath by remember { mutableStateOf(initialReminder?.voiceRecordingPath) }

    // Audio recording state
    val voiceManager = remember { VoiceRecordingManager(context) }
    var isRecording by remember { mutableStateOf(false) }
    var isPlayingPreview by remember { mutableStateOf(false) }
    var recordDurationSeconds by remember { mutableIntStateOf(0) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val path = voiceManager.startRecording()
            if (path != null) {
                voiceRecordingPath = path
                isRecording = true
            }
        } else {
            Toast.makeText(context, "Microphone permission required for voice recording", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordDurationSeconds = 0
            while (isRecording) {
                delay(1000L)
                recordDurationSeconds++
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceManager.release()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = PureWhite,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(CardBorderGray)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialReminder == null) "New Reminder Call" else "Edit Reminder Call",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = SolidBlack
                )
                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close sheet",
                        tint = SolidBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Reminder Title
            Text(
                text = "Reminder Task Title *",
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                color = SolidBlack,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("e.g. Complete Physics lecture", color = MutedTextGray) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reminder_title_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SolidBlack,
                    unfocusedBorderColor = CardBorderGray,
                    focusedTextColor = SolidBlack,
                    unfocusedTextColor = SolidBlack,
                    focusedContainerColor = SoftGraySurface,
                    unfocusedContainerColor = SoftGraySurface
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Optional Description / Notes
            Text(
                text = "Notes or Context (Optional)",
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                color = SolidBlack,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("What specifically needs to be done?", color = MutedTextGray) },
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reminder_description_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SolidBlack,
                    unfocusedBorderColor = CardBorderGray,
                    focusedTextColor = SolidBlack,
                    unfocusedTextColor = SolidBlack,
                    focusedContainerColor = SoftGraySurface,
                    unfocusedContainerColor = SoftGraySurface
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Caller Identity
            Text(
                text = "Simulated Caller Identity",
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                color = SolidBlack,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = callerName,
                onValueChange = { callerName = it },
                placeholder = { Text("e.g. Personal Assistant, Future Self", color = MutedTextGray) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("caller_name_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SolidBlack,
                    unfocusedBorderColor = CardBorderGray,
                    focusedTextColor = SolidBlack,
                    unfocusedTextColor = SolidBlack,
                    focusedContainerColor = SoftGraySurface,
                    unfocusedContainerColor = SoftGraySurface
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Time & Date Controls
            TimeDateSelectorCard(
                targetTimestamp = targetTimestamp,
                onTimestampChange = { targetTimestamp = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Repeat Schedule
            Text(
                text = "Repeat Schedule",
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                color = SolidBlack,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RepeatType.values().forEach { type ->
                    val isSelected = repeatType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { repeatType = type },
                        label = {
                            Text(
                                text = when (type) {
                                    RepeatType.NONE -> "Once"
                                    RepeatType.DAILY -> "Daily"
                                    RepeatType.WEEKDAYS -> "Weekdays"
                                    RepeatType.WEEKENDS -> "Weekends"
                                    RepeatType.CUSTOM -> "Custom"
                                },
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SophisticatedBlue,
                            selectedLabelColor = PureWhite,
                            containerColor = SoftGraySurface,
                            labelColor = SolidBlack
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) SophisticatedBlue else CardBorderGray
                        )
                    )
                }
            }

            // Custom days selection if RepeatType.CUSTOM
            if (repeatType == RepeatType.CUSTOM) {
                Spacer(modifier = Modifier.height(10.dp))
                CustomDaysSelector(
                    mask = repeatDaysMask,
                    onMaskChange = { repeatDaysMask = it }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Snooze Duration Selector
            Text(
                text = "Snooze Interval",
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                color = SolidBlack,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(5, 10, 15, 30).forEach { mins ->
                    val isSelected = snoozeMinutes == mins
                    FilterChip(
                        selected = isSelected,
                        onClick = { snoozeMinutes = mins },
                        label = { Text("${mins} min", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SophisticatedBlue,
                            selectedLabelColor = PureWhite,
                            containerColor = SoftGraySurface,
                            labelColor = SolidBlack
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) SophisticatedBlue else CardBorderGray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Voice Message Attachment Card
            VoiceMessageSection(
                voiceRecordingPath = voiceRecordingPath,
                isRecording = isRecording,
                isPlayingPreview = isPlayingPreview,
                recordDurationSeconds = recordDurationSeconds,
                onStartRecord = {
                    val hasPerm = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPerm) {
                        val path = voiceManager.startRecording()
                        if (path != null) {
                            voiceRecordingPath = path
                            isRecording = true
                        }
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onStopRecord = {
                    val path = voiceManager.stopRecording()
                    isRecording = false
                    if (path != null) {
                        voiceRecordingPath = path
                    }
                },
                onPlayPreview = {
                    voiceRecordingPath?.let { path ->
                        isPlayingPreview = true
                        voiceManager.playAudio(
                            filePath = path,
                            onCompletion = { isPlayingPreview = false },
                            onError = { isPlayingPreview = false }
                        )
                    }
                },
                onStopPreview = {
                    voiceManager.stopPlayback()
                    isPlayingPreview = false
                },
                onDeleteRecording = {
                    voiceManager.deleteRecording(voiceRecordingPath)
                    voiceRecordingPath = null
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Live Preview of Incoming Call Screen
            OutlinedButton(
                onClick = {
                    val intent = Intent(context, IncomingCallActivity::class.java).apply {
                        putExtra(IncomingCallActivity.EXTRA_REMINDER_ID, -99L)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("preview_call_screen_button"),
                shape = RoundedCornerShape(25.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SolidBlack))
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Preview Call",
                    tint = SolidBlack,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Live Preview Incoming Call",
                    color = SolidBlack,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Save Reminder Button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, "Please enter a reminder title", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Compute clean initial target timestamp
                    val finalTarget = if (targetTimestamp <= System.currentTimeMillis()) {
                        // If selected time is in the past, calculate next recurrence or push ahead
                        val next = ScheduleCalculator.calculateNextOccurrence(
                            originalScheduledTime = targetTimestamp,
                            repeatType = repeatType,
                            customDaysMask = repeatDaysMask,
                            referenceTimeMillis = System.currentTimeMillis()
                        )
                        next ?: (System.currentTimeMillis() + 60 * 1000L)
                    } else {
                        targetTimestamp
                    }

                    val reminder = ReminderEntity(
                        id = initialReminder?.id ?: 0,
                        title = title.trim(),
                        description = description.trim(),
                        targetTimestamp = finalTarget,
                        callerName = callerName.trim().ifBlank { defaultCallerName },
                        voiceRecordingPath = voiceRecordingPath,
                        repeatType = repeatType.name,
                        repeatDaysMask = repeatDaysMask,
                        snoozeDurationMinutes = snoozeMinutes,
                        isEnabled = true,
                        isSnoozed = false
                    )

                    onSave(reminder)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_reminder_button"),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SophisticatedBlue,
                    contentColor = PureWhite
                )
            ) {
                Text(
                    text = if (initialReminder == null) "Schedule Reminder Call" else "Update Reminder Call",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TimeDateSelectorCard(
    targetTimestamp: Long,
    onTimestampChange: (Long) -> Unit
) {
    val cal = remember(targetTimestamp) {
        Calendar.getInstance().apply { timeInMillis = targetTimestamp }
    }

    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SoftGraySurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderGray))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Scheduled Time",
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                color = CharcoalBlack,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = timeFormat.format(Date(targetTimestamp)),
                        style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                        color = SolidBlack,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateFormat.format(Date(targetTimestamp)),
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = MutedTextGray
                    )
                }

                // Quick Adjust Pill Buttons (+15m, +1h, +Tomorrow)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        QuickTimePill(label = "+15m") {
                            onTimestampChange(targetTimestamp + 15 * 60 * 1000L)
                        }
                        QuickTimePill(label = "+1h") {
                            onTimestampChange(targetTimestamp + 60 * 60 * 1000L)
                        }
                    }
                    QuickTimePill(label = "Tomorrow") {
                        val tomorrow = Calendar.getInstance().apply {
                            timeInMillis = targetTimestamp
                            add(Calendar.DAY_OF_YEAR, 1)
                        }
                        onTimestampChange(tomorrow.timeInMillis)
                    }
                }
            }
        }
    }
}

@Composable
fun QuickTimePill(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(PureWhite)
            .border(1.dp, CardBorderGray, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
            color = SolidBlack,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun CustomDaysSelector(
    mask: Int,
    onMaskChange: (Int) -> Unit
) {
    val days = listOf(
        "M" to ScheduleCalculator.DAY_MONDAY,
        "T" to ScheduleCalculator.DAY_TUESDAY,
        "W" to ScheduleCalculator.DAY_WEDNESDAY,
        "T" to ScheduleCalculator.DAY_THURSDAY,
        "F" to ScheduleCalculator.DAY_FRIDAY,
        "S" to ScheduleCalculator.DAY_SATURDAY,
        "S" to ScheduleCalculator.DAY_SUNDAY
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { (label, bit) ->
            val isChecked = (mask and bit) != 0
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isChecked) SophisticatedBlue else SoftGraySurface)
                    .border(1.dp, if (isChecked) SophisticatedBlue else CardBorderGray, CircleShape)
                    .clickable {
                        val newMask = if (isChecked) mask and bit.inv() else mask or bit
                        onMaskChange(newMask)
                    }
            ) {
                Text(
                    text = label,
                    color = if (isChecked) PureWhite else SolidBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun VoiceMessageSection(
    voiceRecordingPath: String?,
    isRecording: Boolean,
    isPlayingPreview: Boolean,
    recordDurationSeconds: Int,
    onStartRecord: () -> Unit,
    onStopRecord: () -> Unit,
    onPlayPreview: () -> Unit,
    onStopPreview: () -> Unit,
    onDeleteRecording: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SoftGraySurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderGray))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Voice Message Attachment",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SolidBlack
                    )
                    Text(
                        text = if (voiceRecordingPath.isNullOrBlank()) "Optional spoken note from yourself" else "Audio recorded for call playback",
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = MutedTextGray
                    )
                }

                if (voiceRecordingPath != null && !isRecording) {
                    IconButton(onClick = onDeleteRecording) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete recording",
                            tint = CharcoalBlack
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isRecording) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(SolidBlack)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recording... ${recordDurationSeconds}s",
                            color = SolidBlack,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onStopRecord,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = SolidBlack, contentColor = PureWhite)
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stop")
                    }
                }
            } else if (voiceRecordingPath.isNullOrBlank()) {
                Button(
                    onClick = onStartRecord,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SolidBlack, contentColor = PureWhite)
                ) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Record", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Record Voice Note", fontWeight = FontWeight.SemiBold)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = if (isPlayingPreview) onStopPreview else onPlayPreview,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SolidBlack))
                    ) {
                        Icon(
                            imageVector = if (isPlayingPreview) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isPlayingPreview) "Stop" else "Play",
                            tint = SolidBlack,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPlayingPreview) "Stop Preview" else "Play Recording",
                            color = SolidBlack,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = onStartRecord,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderGray))
                    ) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = "Re-record", tint = SolidBlack, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Re-record", color = SolidBlack, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

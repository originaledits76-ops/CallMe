package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.audio.TtsPlaybackManager
import com.example.data.preferences.UserSettings
import com.example.scheduling.ReminderScheduler
import com.example.ui.call.IncomingCallActivity
import com.example.ui.theme.CardBorderGray
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.MutedTextGray
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftGraySurface
import com.example.ui.theme.SolidBlack
import com.example.ui.theme.SophisticatedBlue
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    userSettings: UserSettings,
    onUpdateCallerName: (String) -> Unit,
    onUpdateSnoozeMinutes: (Int) -> Unit,
    onExportData: suspend () -> String,
    onImportData: suspend (String) -> Boolean,
    onClearAllData: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val scheduler = remember { ReminderScheduler(context) }
    val ttsManager = remember { TtsPlaybackManager(context) }

    var showExportDialog by remember { mutableStateOf(false) }
    var exportedJson by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }

    // Check system permission statuses
    val hasExactAlarm = scheduler.canScheduleExactAlarms()
    val hasNotification = NotificationManagerCompat.from(context).areNotificationsEnabled()
    val hasMicrophone = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        // App Identity & Persona
        Text(
            text = "Default Caller Identity",
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SolidBlack
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = userSettings.defaultCallerName,
            onValueChange = onUpdateCallerName,
            placeholder = { Text("Personal Assistant", color = MutedTextGray) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("default_caller_name_input"),
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

        // Default Snooze Interval
        Text(
            text = "Default Snooze Interval",
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SolidBlack
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(5, 10, 15, 30).forEach { mins ->
                val isSelected = userSettings.defaultSnoozeMinutes == mins
                FilterChip(
                    selected = isSelected,
                    onClick = { onUpdateSnoozeMinutes(mins) },
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

        // System Permissions & Delivery Diagnostic Card
        Text(
            text = "Reliability & Permissions",
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SolidBlack
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SoftGraySurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderGray))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                PermissionStatusRow(
                    icon = Icons.Default.Alarm,
                    title = "Exact Alarm Scheduling",
                    subtitle = if (hasExactAlarm) "Permitted for precise second-level calls" else "Restricted by system. Tap to enable.",
                    isGranted = hasExactAlarm,
                    onFix = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            try {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // fallback
                            }
                        }
                    }
                )

                PermissionStatusRow(
                    icon = Icons.Default.Notifications,
                    title = "Notifications & Full-Screen",
                    subtitle = if (hasNotification) "Allowed to display incoming call alerts" else "Notifications disabled in system settings.",
                    isGranted = hasNotification,
                    onFix = {
                        try {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // fallback
                        }
                    }
                )

                PermissionStatusRow(
                    icon = Icons.Default.Mic,
                    title = "Microphone Access",
                    subtitle = if (hasMicrophone) "Granted for recording voice messages" else "Only needed when recording voice notes.",
                    isGranted = hasMicrophone,
                    onFix = {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // fallback
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sound & Speech Verification
        Text(
            text = "Testing & Diagnostics",
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SolidBlack
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    val intent = Intent(context, IncomingCallActivity::class.java).apply {
                        putExtra(IncomingCallActivity.EXTRA_REMINDER_ID, -99L)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("test_call_now_button"),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SolidBlack, contentColor = PureWhite)
            ) {
                Icon(imageVector = Icons.Default.Call, contentDescription = "Test Call", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Simulate Call")
            }

            OutlinedButton(
                onClick = {
                    ttsManager.speak("Hello! This is your ${userSettings.defaultCallerName}. Your scheduled reminder is working perfectly.")
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("test_tts_button"),
                shape = RoundedCornerShape(24.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderGray)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = PureWhite)
            ) {
                Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Test TTS", tint = SolidBlack, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Test Voice", color = SolidBlack)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Data Backup & Privacy
        Text(
            text = "Data & Privacy",
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SolidBlack
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SoftGraySurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderGray))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = "Privacy", tint = SolidBlack, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "100% Offline & Private",
                        fontWeight = FontWeight.Bold,
                        color = SolidBlack
                    )
                }
                Text(
                    text = "No accounts, no telemetry, no cloud servers. All reminder entries and voice messages remain exclusively on your device.",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = MutedTextGray
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                exportedJson = onExportData()
                                showExportDialog = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderGray)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = PureWhite)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = "Export", tint = SolidBlack, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export JSON", color = SolidBlack)
                    }

                    OutlinedButton(
                        onClick = {
                            importJsonText = ""
                            showImportDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderGray)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = PureWhite)
                    ) {
                        Icon(imageVector = Icons.Default.Upload, contentDescription = "Import", tint = SolidBlack, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import JSON", color = SolidBlack)
                    }
                }

                OutlinedButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SolidBlack)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = PureWhite)
                ) {
                    Text("Delete All Reminders & Data", color = SolidBlack, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(110.dp))
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Exported Reminders JSON", fontWeight = FontWeight.Bold, color = SolidBlack) },
            text = {
                Column {
                    Text("Your reminders have been formatted in valid JSON:", color = CharcoalBlack)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(SoftGraySurface, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(text = exportedJson, fontSize = 12.sp, color = SolidBlack)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(exportedJson))
                        Toast.makeText(context, "Copied JSON to clipboard", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SolidBlack, contentColor = PureWhite)
                ) {
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showExportDialog = false }) {
                    Text("Close", color = SolidBlack)
                }
            },
            containerColor = PureWhite
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Reminders JSON", fontWeight = FontWeight.Bold, color = SolidBlack) },
            text = {
                Column {
                    Text("Paste valid CallMe JSON backup below:", color = CharcoalBlack)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("Paste JSON here...", color = MutedTextGray) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val success = onImportData(importJsonText)
                            if (success) {
                                Toast.makeText(context, "Reminders imported successfully!", Toast.LENGTH_SHORT).show()
                                showImportDialog = false
                            } else {
                                Toast.makeText(context, "Invalid JSON format. Please verify.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SolidBlack, contentColor = PureWhite)
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showImportDialog = false }) {
                    Text("Cancel", color = SolidBlack)
                }
            },
            containerColor = PureWhite
        )
    }

    // Clear All Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Delete All Application Data?", fontWeight = FontWeight.Bold, color = SolidBlack) },
            text = { Text("This will permanently delete all scheduled reminders, history logs, and voice recordings.", color = CharcoalBlack) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showResetDialog = false
                        Toast.makeText(context, "All data wiped.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SolidBlack, contentColor = PureWhite)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = SolidBlack)
                }
            },
            containerColor = PureWhite
        )
    }
}

@Composable
fun PermissionStatusRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onFix: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PureWhite)
                    .border(1.dp, CardBorderGray, CircleShape)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = SolidBlack, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, color = SolidBlack, fontSize = 14.sp)
                Text(text = subtitle, style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = MutedTextGray)
            }
        }

        if (isGranted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Granted",
                tint = SolidBlack,
                modifier = Modifier.size(22.dp)
            )
        } else {
            OutlinedButton(
                onClick = onFix,
                shape = RoundedCornerShape(50),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SolidBlack))
            ) {
                Text("Enable", color = SolidBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

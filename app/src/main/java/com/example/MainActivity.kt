package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ReminderEntity
import com.example.ui.call.IncomingCallActivity
import com.example.ui.components.CallMinderBottomNav
import com.example.ui.components.CallMinderHeader
import com.example.ui.components.ReminderEditSheet
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ScheduleCallScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CallMeTheme
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.ReminderViewModel

enum class MainNavigationTab {
    HOME,
    NEW_CALL,
    LIVE_CALL,
    HISTORY
}

class MainActivity : ComponentActivity() {

    private var lastVolumePressTime = 0L
    private var volumePressCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CallMeTheme(darkTheme = false) {
                CallMeApp()
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP || event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (event.action == KeyEvent.ACTION_DOWN) {
                val now = System.currentTimeMillis()
                if (now - lastVolumePressTime < 800) {
                    volumePressCount++
                    if (volumePressCount >= 2) {
                        volumePressCount = 0
                        Toast.makeText(this, "Volume double-press detected! Launching fake call...", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, IncomingCallActivity::class.java).apply {
                            putExtra(IncomingCallActivity.EXTRA_REMINDER_ID, -99L)
                        }
                        startActivity(intent)
                        return true
                    }
                } else {
                    volumePressCount = 1
                }
                lastVolumePressTime = now
            }
        }
        return super.dispatchKeyEvent(event)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallMeApp(
    viewModel: ReminderViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(MainNavigationTab.NEW_CALL) }
    var isEditSheetOpen by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<ReminderEntity?>(null) }

    val allReminders by viewModel.allReminders.collectAsStateWithLifecycle()
    val nextDueReminder by viewModel.nextDueReminder.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PureWhite
    ) {
        Scaffold(
            containerColor = PureWhite,
            topBar = {
                CallMinderHeader(
                    onProfileClick = { isSettingsOpen = true }
                )
            },
            bottomBar = {
                CallMinderBottomNav(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        val isForward = targetState.ordinal > initialState.ordinal
                        (fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                         slideInHorizontally(
                             animationSpec = spring(
                                 dampingRatio = Spring.DampingRatioLowBouncy,
                                 stiffness = Spring.StiffnessMediumLow
                             )
                         ) { if (isForward) it / 6 else -it / 6 }) togetherWith
                        (fadeOut(animationSpec = tween(160)) +
                         slideOutHorizontally(
                             animationSpec = tween(160)
                         ) { if (isForward) -it / 6 else it / 6 })
                    },
                    label = "tab_animated_content",
                    modifier = Modifier.fillMaxSize()
                ) { tab ->
                    when (tab) {
                        MainNavigationTab.HOME -> {
                            HomeScreen(
                                allReminders = allReminders,
                                nextDueReminder = nextDueReminder,
                                onToggleReminder = { id, enabled ->
                                    viewModel.toggleReminder(id, enabled)
                                },
                                onCompleteReminder = { id ->
                                    viewModel.completeReminder(id)
                                },
                                onEditReminder = { reminder ->
                                    editingReminder = reminder
                                    isEditSheetOpen = true
                                },
                                onDeleteReminder = { id ->
                                    viewModel.deleteReminder(id)
                                },
                                onAddNewReminder = {
                                    selectedTab = MainNavigationTab.NEW_CALL
                                }
                            )
                        }

                        MainNavigationTab.NEW_CALL -> {
                            ScheduleCallScreen(
                                initialReminder = editingReminder,
                                defaultCallerName = userSettings.defaultCallerName,
                                onSaveCall = { reminder ->
                                    viewModel.saveReminder(reminder)
                                    Toast.makeText(context, "Call reminder saved successfully!", Toast.LENGTH_SHORT).show()
                                    selectedTab = MainNavigationTab.HOME
                                },
                                onTriggerTestCall = { testReminder ->
                                    val intent = Intent(context, IncomingCallActivity::class.java).apply {
                                        putExtra(IncomingCallActivity.EXTRA_REMINDER_ID, testReminder.id)
                                    }
                                    context.startActivity(intent)
                                }
                            )
                        }

                        MainNavigationTab.LIVE_CALL -> {
                            HomeScreen(
                                allReminders = allReminders,
                                nextDueReminder = nextDueReminder,
                                onToggleReminder = { id, enabled ->
                                    viewModel.toggleReminder(id, enabled)
                                },
                                onCompleteReminder = { id ->
                                    viewModel.completeReminder(id)
                                },
                                onEditReminder = { reminder ->
                                    editingReminder = reminder
                                    isEditSheetOpen = true
                                },
                                onDeleteReminder = { id ->
                                    viewModel.deleteReminder(id)
                                },
                                onAddNewReminder = {
                                    selectedTab = MainNavigationTab.NEW_CALL
                                }
                            )
                        }

                        MainNavigationTab.HISTORY -> {
                            HistoryScreen(
                                historyList = historyList,
                                onClearHistory = {
                                    viewModel.clearHistory()
                                }
                            )
                        }
                    }
                }
            }
        }

        if (isSettingsOpen) {
            Dialog(onDismissRequest = { isSettingsOpen = false }) {
                Surface(
                    modifier = Modifier.fillMaxSize(0.95f),
                    shape = RoundedCornerShape(24.dp),
                    color = PureWhite
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TopAppBar(
                            title = { Text("Settings & Profile") },
                            navigationIcon = {
                                IconButton(onClick = { isSettingsOpen = false }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close"
                                    )
                                }
                            }
                        )
                        SettingsScreen(
                            userSettings = userSettings,
                            onUpdateCallerName = { viewModel.updateDefaultCallerName(it) },
                            onUpdateSnoozeMinutes = { viewModel.updateDefaultSnoozeMinutes(it) },
                            onExportData = { viewModel.exportDataJson() },
                            onImportData = { viewModel.importDataJson(it) },
                            onClearAllData = { viewModel.clearAllData() }
                        )
                    }
                }
            }
        }

        if (isEditSheetOpen) {
            ReminderEditSheet(
                initialReminder = editingReminder,
                defaultCallerName = userSettings.defaultCallerName,
                defaultSnoozeMinutes = userSettings.defaultSnoozeMinutes,
                onDismissRequest = {
                    isEditSheetOpen = false
                    editingReminder = null
                },
                onSave = { reminder ->
                    if (reminder.id == 0L) {
                        viewModel.saveReminder(reminder)
                    } else {
                        viewModel.updateReminder(reminder)
                    }
                    isEditSheetOpen = false
                    editingReminder = null
                }
            )
        }
    }
}

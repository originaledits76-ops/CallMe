package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ReminderEntity
import com.example.ui.components.LiquidGlassBottomBar
import com.example.ui.components.ReminderEditSheet
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CallMeTheme
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.ReminderViewModel

enum class MainNavigationTab {
    CALLS,
    HISTORY,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CallMeTheme(darkTheme = false) {
                CallMeApp()
            }
        }
    }
}

@Composable
fun CallMeApp(
    viewModel: ReminderViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(MainNavigationTab.CALLS) }
    var isEditSheetOpen by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<ReminderEntity?>(null) }

    val allReminders by viewModel.allReminders.collectAsStateWithLifecycle()
    val nextDueReminder by viewModel.nextDueReminder.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()

    // Request notification permission on Android 13+
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
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content area with smooth animated transitions between tabs
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
                    MainNavigationTab.CALLS -> {
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
                                editingReminder = null
                                isEditSheetOpen = true
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

                    MainNavigationTab.SETTINGS -> {
                        SettingsScreen(
                            userSettings = userSettings,
                            onUpdateCallerName = { name ->
                                viewModel.updateDefaultCallerName(name)
                            },
                            onUpdateSnoozeMinutes = { mins ->
                                viewModel.updateDefaultSnoozeMinutes(mins)
                            },
                            onExportData = {
                                viewModel.exportDataJson()
                            },
                            onImportData = { json ->
                                viewModel.importDataJson(json)
                            },
                            onClearAllData = {
                                viewModel.clearAllData()
                            }
                        )
                    }
                }
            }

            // Floating Liquid Glass Navigation Bar: Wrapped around icons, rounded corners, icons only
            LiquidGlassBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 18.dp)
            )
        }

        // Add / Edit Reminder Sheet
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

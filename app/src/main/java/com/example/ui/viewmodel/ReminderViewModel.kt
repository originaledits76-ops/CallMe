package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ReminderEntity
import com.example.data.model.ReminderHistoryEntity
import com.example.data.preferences.UserSettings
import com.example.data.repository.ReminderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ReminderRepository(application)

    val allReminders: StateFlow<List<ReminderEntity>> = repository.allReminders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeReminders: StateFlow<List<ReminderEntity>> = repository.activeReminders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val historyList: StateFlow<List<ReminderHistoryEntity>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userSettings: StateFlow<UserSettings> = repository.settingsRepository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    val nextDueReminder: StateFlow<ReminderEntity?> = repository.activeReminders.map { list ->
        val now = System.currentTimeMillis()
        list.filter { it.isEnabled && it.targetTimestamp > (now - 60_000L) }
            .minByOrNull { it.targetTimestamp }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun saveReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.saveReminder(reminder)
        }
    }

    fun updateReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.updateReminder(reminder)
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            repository.deleteReminder(id)
        }
    }

    fun toggleReminder(id: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleReminder(id, isEnabled)
        }
    }

    fun snoozeReminder(id: Long, customMinutes: Int? = null) {
        viewModelScope.launch {
            repository.snoozeReminder(id, customMinutes)
        }
    }

    fun completeReminder(id: Long) {
        viewModelScope.launch {
            repository.completeReminder(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    fun updateDefaultCallerName(name: String) {
        viewModelScope.launch {
            repository.settingsRepository.updateDefaultCallerName(name)
        }
    }

    fun updateDefaultSnoozeMinutes(minutes: Int) {
        viewModelScope.launch {
            repository.settingsRepository.updateDefaultSnoozeMinutes(minutes)
        }
    }

    suspend fun exportDataJson(): String = repository.exportDataJson()

    suspend fun importDataJson(jsonString: String): Boolean = repository.importDataJson(jsonString)
}

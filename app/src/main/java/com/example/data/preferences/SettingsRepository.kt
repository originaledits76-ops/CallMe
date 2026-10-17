package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "callme_settings")

data class UserSettings(
    val defaultSnoozeMinutes: Int = 10,
    val defaultCallerName: String = "Personal Assistant",
    val isVibrationEnabled: Boolean = true,
    val isTtsEnabled: Boolean = true,
    val isDarkMode: Boolean = false,
    val autoSpeakOnAnswer: Boolean = true
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val DEFAULT_SNOOZE_MINUTES = intPreferencesKey("default_snooze_minutes")
        val DEFAULT_CALLER_NAME = stringPreferencesKey("default_caller_name")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val TTS_ENABLED = booleanPreferencesKey("tts_enabled")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val AUTO_SPEAK_ON_ANSWER = booleanPreferencesKey("auto_speak_on_answer")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            defaultSnoozeMinutes = prefs[Keys.DEFAULT_SNOOZE_MINUTES] ?: 10,
            defaultCallerName = prefs[Keys.DEFAULT_CALLER_NAME] ?: "Personal Assistant",
            isVibrationEnabled = prefs[Keys.VIBRATION_ENABLED] ?: true,
            isTtsEnabled = prefs[Keys.TTS_ENABLED] ?: true,
            isDarkMode = prefs[Keys.DARK_MODE] ?: false,
            autoSpeakOnAnswer = prefs[Keys.AUTO_SPEAK_ON_ANSWER] ?: true
        )
    }

    suspend fun updateDefaultSnoozeMinutes(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_SNOOZE_MINUTES] = minutes
        }
    }

    suspend fun updateDefaultCallerName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_CALLER_NAME] = name
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun setTtsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TTS_ENABLED] = enabled
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DARK_MODE] = enabled
        }
    }

    suspend fun setAutoSpeakOnAnswer(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTO_SPEAK_ON_ANSWER] = enabled
        }
    }
}

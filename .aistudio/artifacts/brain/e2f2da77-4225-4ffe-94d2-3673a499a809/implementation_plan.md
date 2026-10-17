# CallMe — Native Android Reminder App That Calls You

CallMe transforms critical reminders into simulated incoming-call experiences that demand attention without initiating actual phone calls. Engineered with offline-first local architecture, exact alarm scheduling, voice recording with on-device Text-to-Speech fallback, and a high-craft modern minimalist monochrome visual aesthetic.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and design decisions have been confirmed from your answers and will guide implementation:

- **Confirmed Decision 1 (Visual Theme)**: Modern minimalist two-tone theme featuring pure white backgrounds (`#FFFFFF`), crisp black text and tactile iconography (`#000000`), subtle boundary strokes (`#E5E5EA` / `#F2F2F7`), and rounded sans-serif typography with generous whitespace.
- **Confirmed Decision 2 (Default Caller Identity)**: Set to **"Personal Assistant"** as the default caller persona, with full customization per reminder.
- **Confirmed Decision 3 (Audio Fallback)**: Built-in Android **Text-to-Speech (TTS)** engine that articulates the reminder title and description upon answering when no custom voice message was recorded.
- **Core Reliability Commitment**: 100% offline-first. No accounts, external analytics, or cloud servers. Local Room database, DataStore preferences, exact AlarmManager alerts, and device reboot recovery (`BOOT_COMPLETED`).

---

## 1. Overview & Core Concept

- **What It Does**: When a scheduled reminder fires, CallMe presents an immersive, simulated incoming-call experience on screen (via full-screen intent and high-priority call-style notification). Users can answer the reminder to hear the audio message or TTS readout, snooze it (5, 10, or 15 mins), mark it complete, or dismiss it.
- **Target Audience / Persona**: Professionals, students, and busy individuals who suffer from notification fatigue and swipe away standard notification banners without acting.
- **Key Value**: Guarantees that critical reminders—medications, lectures, critical meetings, high-priority tasks—cut through notification noise while preserving battery life and absolute privacy.

---

## 2. User Experience & Visual Design

### Visual Identity & Theme
- **Monochrome Minimalist Palette**:
  - Background: Crisp Pure White (`#FFFFFF`) / Soft Off-White Layering (`#F8F9FA`)
  - Typography & High-Priority Actions: Deep Obsidian Black (`#111111`) / Solid Black (`#000000`)
  - Secondary Accents & Dividers: Minimalist Muted Slate (`#71717A`) & Light Gray Borders (`#E4E4E7`)
  - Call Accents: High-contrast tactile action buttons (Tactile Black Pill with white icons for Accept, Stark Outlined Pill for Decline/Dismiss)
- **Typography**: Clean, friendly rounded typography hierarchy (large rounded header titles, tight tracking, legible numerals for countdown timers and clock faces).
- **Tactility & Micro-Interactions**: Smooth scale feedback on press (`active:scale-[0.98]`), spring animations for call pulses, audio waveform playback bars, and tactile haptic vibration patterns.

### Key User Flows

```
┌──────────────────────────────────────────────────────────────────────────┐
│                             CallMe UX Flow                               │
└──────────────────────────────────────────────────────────────────────────┘
  [ Home Screen ] ─────────────► [ Create / Edit Reminder ]
         │                               │
         │ (Scheduled Alarm Fires)        │ (Live Call Preview & Voice Record)
         ▼                               ▼
  [ Incoming Call Screen ] ◄───── [ Full-Screen Intent / Heads-up Alert ]
         │
    ┌────┴──────────────────────────┐
    ▼                               ▼
 [ Snooze (5/10/15m) ]         [ Answer Call ]
                                    │
                                    ▼
                         [ Active Reminder View ]
                         - Voice Message / TTS
                         - Task Title & Notes
                         - [ Mark Completed ]
                         - [ Dismiss / Replay ]
```

1. **Home Screen**:
   - Dynamic greeting, current date, and an Apple-inspired grouped card for the **Next Due Reminder** featuring a live countdown clock.
   - Filter tabs: **Today's Schedule**, **Upcoming**, and **History**.
   - Floating minimalist "Add Reminder" button (`+`).
   - Quick action to trigger a "Simulate Call Now" test to immediately experience the feature.

2. **Reminder Creation & Editing**:
   - Title input, optional description, date & time wheel pickers.
   - Caller display name (defaults to "Personal Assistant").
   - Repeat schedule chips: *Once*, *Every Day*, *Weekdays*, *Weekends*, *Custom Days*.
   - Voice message recorder: In-app microphone recorder with waveform display, record, stop, playback preview, and delete.
   - Live interactive **Preview Incoming Call** button.

3. **Simulated Incoming-Call Screen (`IncomingCallActivity`)**:
   - Shows caller identity ("Personal Assistant"), avatar/call glyph, pulsating ring animation, reminder title, and scheduled time.
   - Answer slider or prominent tap button, accompanied by customizable snooze pills (5m, 10m, 15m) and dismiss.
   - Works over lock screen (`setShowWhenLocked(true)`, `setTurnScreenOn(true)`).

4. **Active Answered Call Experience**:
   - Displays task details in an inset card.
   - Voice audio playback controls (Play, Pause, Progress scrubber) or automatic TTS speech reading the task.
   - Prominent **"Mark as Completed"** button which logs to history and clears the alarm, or **"Snooze"** if needed later.

5. **History & Settings**:
   - History: Logs of completed, snoozed, and dismissed reminders with weekly completion stats and a "Clear History" option.
   - Settings: Default snooze duration, default caller persona, permission diagnostics (Exact Alarm, Notification, Microphone), data JSON export/import.

---

## 3. Key Product Decisions & Trade-Offs

- **Exact Alarms via AlarmManager (`setExactAndAllowWhileIdle`)**:
  - *Chosen Approach*: Uses Android's `AlarmManager` with `setExactAndAllowWhileIdle()` coupled with `POST_NOTIFICATIONS` and `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`.
  - *Why*: Doze mode and battery optimizations delay standard timers. Exact alarms guarantee delivery at the designated second.
  - *Fallback*: If exact alarm permissions are restricted by Android 12+, gracefully falls back to `setAndAllowWhileIdle()` and informs the user in settings.
- **Simulated Incoming Call Presentation**:
  - *Chosen Approach*: Notification with `setFullScreenIntent()` pointing to `IncomingCallActivity` plus standard heads-up notification action buttons (Answer, Snooze, Dismiss).
  - *Why*: Modern Android (API 29-36) restricts launching activities directly from background receivers unless initiated via full-screen intent notifications or when the user is actively using the phone.
- **Local Voice Recording & Storage**:
  - *Chosen Approach*: Android `MediaRecorder` recording `.m4a` files directly into `context.filesDir/voice_messages/`, storing the local file path in Room.
  - *Why*: Zero network dependencies, zero privacy leaks, and instant offline playback.
- **On-Device Text-to-Speech (TTS)**:
  - *Chosen Approach*: Integrates `android.speech.tts.TextToSpeech`.
  - *Why*: Guarantees every reminder has an audible call message even when the user didn't record a voice clip.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           SYSTEM ARCHITECTURE                               │
└─────────────────────────────────────────────────────────────────────────────┘
  ┌─────────────────────────────────────────────────────────────────────────┐
  │                         Jetpack Compose UI                              │
  │  HomeScreen  │  ReminderEditSheet  │  IncomingCallActivity  │  Settings │
  └────────────────────────────────────┬────────────────────────────────────┘
                                       │ (StateFlow / Events)
  ┌────────────────────────────────────▼────────────────────────────────────┐
  │                           ViewModel Layer                               │
  │              ReminderViewModel  │  IncomingCallViewModel                │
  └──────────────────┬──────────────────────────────────────┬───────────────┘
                     │                                      │
  ┌──────────────────▼──────────────────┐  ┌────────────────▼───────────────┐
  │        ReminderRepository           │  │   Alarm & Execution Engine     │
  │  - ReminderDao (Room SQLite)        │  │  - ReminderScheduler           │
  │  - HistoryDao (Room SQLite)         │  │  - ReminderAlarmReceiver       │
  │  - SettingsRepository (DataStore)   │  │  - ReminderNotificationManager │
  │  - VoiceRecordingManager            │  │  - BootCompletedReceiver       │
  │  - TtsPlaybackManager               │  │  - ReminderActionReceiver      │
  └─────────────────────────────────────┘  └────────────────────────────────┘
```

### Database Schema (Room)
1. **`reminders` Table**:
   - `id: Long` (Primary Key, auto-generate)
   - `title: String`
   - `description: String?`
   - `targetTimestamp: Long`
   - `callerName: String` (defaults to "Personal Assistant")
   - `voiceRecordingPath: String?`
   - `repeatType: String` (NONE, DAILY, WEEKDAYS, WEEKENDS, CUSTOM)
   - `repeatDaysMask: Int` (bitmask for Mon-Sun)
   - `snoozeDurationMinutes: Int` (defaults to 10)
   - `priority: Int` (NORMAL, HIGH, URGENT)
   - `isEnabled: Boolean`
   - `isSnoozed: Boolean`
   - `createdAt: Long`

2. **`reminder_history` Table**:
   - `id: Long` (Primary Key, auto-generate)
   - `reminderId: Long?`
   - `title: String`
   - `scheduledTimestamp: Long`
   - `actionTimestamp: Long`
   - `status: String` (COMPLETED, SNOOZED, DISMISSED, MISSED)

---

## 5. Phased Implementation Steps

1. **Phase 1: Project Setup & Dependencies**:
   - Configure `metadata.json` (Set app name: "CallMe").
   - Enable `androidx-datastore-preferences` in `build.gradle.kts`.
   - Update `AndroidManifest.xml` with `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, `RECORD_AUDIO`, `VIBRATE`, and register `IncomingCallActivity`, receivers, and notification channels.
2. **Phase 2: Database & Domain Layer**:
   - Implement `ReminderEntity`, `ReminderHistoryEntity`, `ReminderDao`, `HistoryDao`, and `CallMeDatabase`.
   - Implement `SettingsRepository` using DataStore.
   - Implement `ReminderRepository`.
3. **Phase 3: Scheduling & Notification Engine**:
   - Implement `ReminderScheduler` with exact alarms, repeat schedule calculation, and PendingIntent management.
   - Implement `ReminderAlarmReceiver` to trigger notifications and full-screen intents.
   - Implement `ReminderNotificationManager` with high-priority call channel, full-screen intent, and action buttons.
   - Implement `BootCompletedReceiver` to restore all active alarms after reboot.
   - Implement `ReminderActionReceiver` to handle background Answer, Snooze, and Dismiss.
4. **Phase 4: Audio & TTS Engine**:
   - Implement `VoiceRecordingManager` (MediaRecorder + MediaPlayer).
   - Implement `TtsPlaybackManager` for speech synthesis.
5. **Phase 5: Incoming Call Experience**:
   - Create `IncomingCallActivity` styled in modern minimalist monochrome with pulsating call animations, answer slider, snooze controls, and task details.
6. **Phase 6: Home & Creation UI**:
   - Build `HomeScreen` with Next Due countdown card, today's schedule, and filters.
   - Build `ReminderEditSheet` with date/time pickers, caller name, repeat rules, voice recorder, and live preview.
   - Build `HistoryScreen` and `SettingsScreen` (data export/import, sound test, permissions check).
7. **Phase 7: Verification & Testing**:
   - Add unit and Robolectric tests for Room storage, recurring date math, and snooze calculation.
   - Compile and verify the build.

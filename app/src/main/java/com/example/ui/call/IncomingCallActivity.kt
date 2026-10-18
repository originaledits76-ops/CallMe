package com.example.ui.call

import android.app.KeyguardManager
import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.TtsPlaybackManager
import com.example.audio.VoiceRecordingManager
import com.example.data.model.ReminderEntity
import com.example.data.repository.ReminderRepository
import com.example.notification.ReminderNotificationManager
import com.example.scheduling.ReminderScheduler
import com.example.ui.theme.CallMeTheme
import com.example.ui.theme.CardBorderGray
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.MutedTextGray
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftGraySurface
import com.example.ui.theme.SolidBlack
import com.example.ui.theme.SophisticatedBlue
import com.example.ui.theme.SophisticatedBlueGlow
import com.example.ui.theme.SophisticatedBlueSoft
import com.example.ui.theme.LiquidGlassSurface
import com.example.ui.theme.LiquidGlassHighlight
import com.example.ui.theme.LiquidGlassBorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IncomingCallActivity : ComponentActivity() {

    companion object {
        const val EXTRA_REMINDER_ID = ReminderNotificationManager.EXTRA_REMINDER_ID
    }

    private var reminderId: Long = -1L
    private var voiceManager: VoiceRecordingManager? = null
    private var ttsManager: TtsPlaybackManager? = null
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Turn screen on and show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        reminderId = intent.getLongExtra(ReminderNotificationManager.EXTRA_REMINDER_ID, -1L)
        if (reminderId == -1L) {
            reminderId = intent.getLongExtra(ReminderScheduler.EXTRA_REMINDER_ID, -1L)
        }

        // Cancel notification since we are displaying full screen
        if (reminderId != -1L) {
            ReminderNotificationManager(this).dismissNotification(reminderId)
        }

        voiceManager = VoiceRecordingManager(this)
        ttsManager = TtsPlaybackManager(this)

        startRingingSoundAndVibration()

        val autoAnswer = intent.getBooleanExtra("auto_answer", false)

        setContent {
            CallMeTheme(darkTheme = false) {
                IncomingCallScreen(
                    reminderId = reminderId,
                    autoAnswer = autoAnswer,
                    onStopRinging = { stopRingingSoundAndVibration() },
                    voiceManager = voiceManager!!,
                    ttsManager = ttsManager!!,
                    onFinishActivity = {
                        stopRingingSoundAndVibration()
                        finish()
                    }
                )
            }
        }
    }

    private fun startRingingSoundAndVibration() {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ringtone = RingtoneManager.getRingtone(applicationContext, uri)
            ringtone?.play()

            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val pattern = longArrayOf(0, 800, 800)
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 800, 800), 0)
            }
        } catch (e: Exception) {
            // Ignore ringtone playback issues
        }
    }

    private fun stopRingingSoundAndVibration() {
        try {
            ringtone?.let {
                if (it.isPlaying) it.stop()
            }
            vibrator?.cancel()
        } catch (e: Exception) {
            // Ignore
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRingingSoundAndVibration()
        voiceManager?.release()
        ttsManager?.shutdown()
    }
}

@Composable
fun IncomingCallScreen(
    reminderId: Long,
    autoAnswer: Boolean,
    onStopRinging: () -> Unit,
    voiceManager: VoiceRecordingManager,
    ttsManager: TtsPlaybackManager,
    onFinishActivity: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { ReminderRepository(context) }
    val scope = rememberCoroutineScope()

    var reminder by remember { mutableStateOf<ReminderEntity?>(null) }
    var isAnswered by remember { mutableStateOf(autoAnswer) }
    var isAudioPlaying by remember { mutableStateOf(false) }
    var audioStatusText by remember { mutableStateOf("") }
    var callDurationSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(reminderId) {
        if (reminderId != -1L) {
            val r = withContext(Dispatchers.IO) { repository.getReminderById(reminderId) }
            reminder = r
        } else {
            // Preview / test reminder
            reminder = ReminderEntity(
                id = -99L,
                title = "Review Weekly Goals & Plan",
                description = "Ensure high priority deliverables are tracked and completed.",
                targetTimestamp = System.currentTimeMillis(),
                callerName = "Personal Assistant"
            )
        }
    }

    LaunchedEffect(isAnswered) {
        if (isAnswered) {
            onStopRinging()
            // Increment call duration timer
            scope.launch {
                while (true) {
                    delay(1000L)
                    callDurationSeconds++
                }
            }

            // Play voice recording or speak via TTS
            reminder?.let { r ->
                if (!r.voiceRecordingPath.isNullOrBlank()) {
                    audioStatusText = "Playing Voice Message..."
                    isAudioPlaying = true
                    voiceManager.playAudio(
                        filePath = r.voiceRecordingPath,
                        onCompletion = {
                            isAudioPlaying = false
                            audioStatusText = "Voice Message Finished"
                        },
                        onError = {
                            isAudioPlaying = false
                            audioStatusText = "Voice Message Unavailable"
                        }
                    )
                } else {
                    audioStatusText = "Speaking Reminder via TTS..."
                    isAudioPlaying = true
                    val speechText = "Reminder from ${r.callerName}. Title: ${r.title}. ${r.description}"
                    ttsManager.speak(speechText) {
                        isAudioPlaying = false
                        audioStatusText = "Reminder Readout Complete"
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceManager.stopPlayback()
            ttsManager.stop()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PureWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Caller Identity & Time
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 28.dp)
            ) {
                Text(
                    text = if (isAnswered) "Connected Call" else "Incoming Reminder Call",
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                    color = MutedTextGray,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = reminder?.callerName?.ifBlank { "Personal Assistant" } ?: "Personal Assistant",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                    color = SolidBlack,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (isAnswered) {
                    val minutes = callDurationSeconds / 60
                    val seconds = callDurationSeconds % 60
                    Text(
                        text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        color = SolidBlack,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(
                        Date(reminder?.targetTimestamp ?: System.currentTimeMillis())
                    )
                    Text(
                        text = "Scheduled for $timeStr",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        color = MutedTextGray
                    )
                }
            }

            // Center Content: Pulsing Rings (When Ringing) or Reminder Inset Card (When Answered)
            if (!isAnswered) {
                PulsingCallRings(title = reminder?.title ?: "Reminder Due")
            } else {
                AnsweredCallCard(
                    reminder = reminder,
                    audioStatusText = audioStatusText,
                    isAudioPlaying = isAudioPlaying,
                    onReplayAudio = {
                        reminder?.let { r ->
                            voiceManager.stopPlayback()
                            ttsManager.stop()
                            if (!r.voiceRecordingPath.isNullOrBlank()) {
                                audioStatusText = "Playing Voice Message..."
                                isAudioPlaying = true
                                voiceManager.playAudio(
                                    filePath = r.voiceRecordingPath,
                                    onCompletion = {
                                        isAudioPlaying = false
                                        audioStatusText = "Voice Message Finished"
                                    }
                                )
                            } else {
                                audioStatusText = "Speaking Reminder..."
                                isAudioPlaying = true
                                val speechText = "Reminder from ${r.callerName}. Title: ${r.title}. ${r.description}"
                                ttsManager.speak(speechText) {
                                    isAudioPlaying = false
                                    audioStatusText = "Reminder Readout Complete"
                                }
                            }
                        }
                    },
                    onStopAudio = {
                        voiceManager.stopPlayback()
                        ttsManager.stop()
                        isAudioPlaying = false
                        audioStatusText = "Audio Stopped"
                    }
                )
            }

            // Bottom Actions: Answer / Snooze / Dismiss OR Complete / Snooze / End
            if (!isAnswered) {
                IncomingCallActionButtons(
                    onAnswer = { isAnswered = true },
                    onSnooze = { minutes ->
                        onStopRinging()
                        scope.launch {
                            if (reminderId > 0) {
                                repository.snoozeReminder(reminderId, minutes)
                            }
                            onFinishActivity()
                        }
                    },
                    onDismiss = {
                        onStopRinging()
                        scope.launch {
                            if (reminderId > 0) {
                                repository.dismissReminder(reminderId)
                            }
                            onFinishActivity()
                        }
                    }
                )
            } else {
                ActiveCallActionButtons(
                    onComplete = {
                        scope.launch {
                            if (reminderId > 0) {
                                repository.completeReminder(reminderId)
                            }
                            onFinishActivity()
                        }
                    },
                    onSnooze = { minutes ->
                        scope.launch {
                            if (reminderId > 0) {
                                repository.snoozeReminder(reminderId, minutes)
                            }
                            onFinishActivity()
                        }
                    },
                    onEndCall = {
                        scope.launch {
                            if (reminderId > 0) {
                                repository.dismissReminder(reminderId)
                            }
                            onFinishActivity()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PulsingCallRings(title: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale1"
    )
    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha1"
    )
    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale2"
    )
    val pulseAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha2"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(250.dp)
        ) {
            // Outer pulse ring in Sophisticated Blue
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(pulseScale1)
            ) {
                drawCircle(
                    color = SophisticatedBlue.copy(alpha = pulseAlpha1),
                    radius = size.minDimension / 2,
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }

            // Inner pulse ring in subtle blue glow
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(pulseScale2)
            ) {
                drawCircle(
                    color = SophisticatedBlueGlow.copy(alpha = pulseAlpha2),
                    radius = size.minDimension / 2.2f,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Static circular container: Frosted glass styling with sophisticated blue border
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(LiquidGlassSurface)
                    .border(2.dp, SophisticatedBlue, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Simulated Call Glyph",
                    tint = SophisticatedBlue,
                    modifier = Modifier.size(54.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = title,
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            color = SolidBlack,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun AnsweredCallCard(
    reminder: ReminderEntity?,
    audioStatusText: String,
    isAudioPlaying: Boolean,
    onReplayAudio: () -> Unit,
    onStopAudio: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = LiquidGlassSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LiquidGlassBorder))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Audio playing",
                    tint = SophisticatedBlue,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = audioStatusText,
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                    color = CharcoalBlack
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = reminder?.title ?: "",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = SolidBlack,
                textAlign = TextAlign.Center
            )

            if (!reminder?.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = reminder.description,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    color = MutedTextGray,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Audio Controls (Replay / Stop)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onReplayAudio,
                    shape = RoundedCornerShape(50),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SolidBlack)),
                    modifier = Modifier.testTag("replay_audio_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Replay Audio",
                        tint = SolidBlack,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Replay",
                        color = SolidBlack,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (isAudioPlaying) {
                    OutlinedButton(
                        onClick = onStopAudio,
                        shape = RoundedCornerShape(50),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SolidBlack)),
                        modifier = Modifier.testTag("stop_audio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Audio",
                            tint = SolidBlack,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Stop",
                            color = SolidBlack,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IncomingCallActionButtons(
    onAnswer: () -> Unit,
    onSnooze: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Snooze Options Row (5m, 10m, 15m)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(5, 10, 15).forEach { mins ->
                OutlinedButton(
                    onClick = { onSnooze(mins) },
                    shape = RoundedCornerShape(50),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderGray)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = PureWhite),
                    modifier = Modifier.testTag("snooze_${mins}m_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Snooze,
                        contentDescription = "Snooze $mins minutes",
                        tint = SolidBlack,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${mins}m",
                        color = SolidBlack,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Main Decline and Answer Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Decline / Dismiss Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .border(2.dp, SolidBlack, CircleShape)
                        .background(PureWhite)
                        .clickable { onDismiss() }
                        .testTag("dismiss_call_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Dismiss Reminder",
                        tint = SolidBlack,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Decline",
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                    color = SolidBlack,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Answer Call Button (Tactile Sophisticated Blue Button with Glow)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(78.dp)
                        .clip(CircleShape)
                        .background(SophisticatedBlue)
                        .clickable { onAnswer() }
                        .testTag("answer_call_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Answer Reminder",
                        tint = PureWhite,
                        modifier = Modifier.size(38.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Answer",
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                    color = SophisticatedBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ActiveCallActionButtons(
    onComplete: () -> Unit,
    onSnooze: (Int) -> Unit,
    onEndCall: () -> Unit
) {
    var showDialpad by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Prominent Mark as Completed Button in Sophisticated Blue
        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("complete_reminder_button"),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SophisticatedBlue,
                contentColor = PureWhite
            )
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Complete Task",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Mark as Completed",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { showDialpad = true },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("open_keypad_button"),
                shape = RoundedCornerShape(24.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderGray)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = PureWhite)
            ) {
                Icon(
                    imageVector = Icons.Default.Dialpad,
                    contentDescription = "Keypad",
                    tint = SolidBlack,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Keypad", color = SolidBlack, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { onSnooze(10) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("snooze_10m_active_button"),
                shape = RoundedCornerShape(24.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorderGray)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = PureWhite)
            ) {
                Icon(
                    imageVector = Icons.Default.Snooze,
                    contentDescription = "Snooze 10 minutes",
                    tint = SolidBlack,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Snooze 10m", color = SolidBlack, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onEndCall,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("end_call_button"),
                shape = RoundedCornerShape(24.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SolidBlack)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = PureWhite)
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End call",
                    tint = SolidBlack,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "End", color = SolidBlack, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showDialpad) {
        DialpadDialog(onDismiss = { showDialpad = false }) { digit ->
            Toast.makeText(context, "DTMF: $digit pressed", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun DialpadDialog(onDismiss: () -> Unit, onNumberPressed: (String) -> Unit) {
    var dialedDigits by remember { mutableStateOf("") }
    val context = LocalContext.current

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = LiquidGlassSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LiquidGlassBorder))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Keypad",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SolidBlack
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = dialedDigits.ifBlank { "Press key..." },
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = SophisticatedBlue,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))

                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("*", "0", "#")
                )

                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { digit ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(PureWhite)
                                    .border(1.dp, CardBorderGray, CircleShape)
                                    .clickable {
                                        dialedDigits += digit
                                        onNumberPressed(digit)
                                    }
                            ) {
                                Text(
                                    text = digit,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SolidBlack
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Keypad", fontWeight = FontWeight.Bold, color = SolidBlack)
                }
            }
        }
    }
}

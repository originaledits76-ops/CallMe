package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TtsPlaybackManager(context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingSpeechText: String? = null

    companion object {
        private const val TAG = "TtsPlaybackManager"
        private const val UTTERANCE_ID = "callme_reminder_utterance"
    }

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.language = Locale.getDefault()
                Log.d(TAG, "TTS initialized successfully")
                pendingSpeechText?.let {
                    speak(it)
                    pendingSpeechText = null
                }
            } else {
                Log.e(TAG, "Failed to initialize TTS engine with status: $status")
            }
        }
    }

    fun speak(
        text: String,
        onDone: () -> Unit = {}
    ) {
        if (!isInitialized) {
            pendingSpeechText = text
            return
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                onDone()
            }
            override fun onError(utteranceId: String?) {
                onDone()
            }
        })

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    fun stop() {
        try {
            if (tts?.isSpeaking == true) {
                tts?.stop()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        }
    }

    fun shutdown() {
        stop()
        try {
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS", e)
        }
    }
}

package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

class VoiceRecordingManager(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentRecordingFile: File? = null

    companion object {
        private const val TAG = "VoiceRecordingManager"
    }

    private val recordingsDir: File
        get() {
            val dir = File(context.filesDir, "voice_recordings")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

    fun startRecording(): String? {
        stopPlayback()
        stopRecording()

        val audioFile = File(recordingsDir, "voice_${System.currentTimeMillis()}.m4a")
        currentRecordingFile = audioFile

        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        try {
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            Log.d(TAG, "Recording started at: ${audioFile.absolutePath}")
            return audioFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error starting voice recording", e)
            mediaRecorder?.release()
            mediaRecorder = null
            audioFile.delete()
            return null
        }
    }

    fun stopRecording(): String? {
        val recorder = mediaRecorder ?: return null
        return try {
            recorder.stop()
            recorder.release()
            mediaRecorder = null
            Log.d(TAG, "Recording stopped successfully")
            currentRecordingFile?.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recorder", e)
            mediaRecorder?.release()
            mediaRecorder = null
            null
        }
    }

    fun playAudio(
        filePath: String,
        onCompletion: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        stopPlayback()

        val file = File(filePath)
        if (!file.exists()) {
            onError("Recording file not found")
            return
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnCompletionListener {
                    onCompletion()
                    release()
                    mediaPlayer = null
                }
                setOnErrorListener { _, what, extra ->
                    onError("Playback error: $what, $extra")
                    release()
                    mediaPlayer = null
                    true
                }
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing audio file", e)
            onError(e.message ?: "Failed to play recording")
        }
    }

    fun stopPlayback() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping playback", e)
        } finally {
            mediaPlayer = null
        }
    }

    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying == true
        } catch (e: Exception) {
            false
        }
    }

    fun deleteRecording(filePath: String?) {
        if (filePath.isNullOrBlank()) return
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
                Log.d(TAG, "Deleted recording file: $filePath")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting recording", e)
        }
    }

    fun release() {
        stopRecording()
        stopPlayback()
    }
}

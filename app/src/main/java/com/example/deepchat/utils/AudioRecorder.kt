package com.example.deepchat.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

suspend fun startAudioRecording(
    context: Context,
    onAudioData: (ByteArray) -> Unit
) = withContext(Dispatchers.IO) {
    // Check for permission
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED) {

        // Show toast on main thread
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                "Audio recording permission not granted",
                Toast.LENGTH_SHORT
            ).show()
        }

        Log.e("AudioRecorder", "Missing RECORD_AUDIO permission")
        return@withContext
    }

    val sampleRate = 44100
    val channelConfig = AudioFormat.CHANNEL_IN_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    try {
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Failed to initialize audio recorder",
                    Toast.LENGTH_SHORT
                ).show()
            }
            Log.e("AudioRecorder", "AudioRecord initialization failed")
            return@withContext
        }

        val buffer = ByteArray(bufferSize)

        audioRecord.startRecording()

        try {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Recording started",
                    Toast.LENGTH_SHORT
                ).show()
            }

            while (isActive) {
                val readResult = audioRecord.read(buffer, 0, bufferSize)
                if (readResult > 0) {
                    onAudioData(buffer.copyOf(readResult))
                }
            }
        } finally {
            audioRecord.stop()
            audioRecord.release()

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Recording stopped",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                "Error: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
        Log.e("AudioRecorder", "Error in audio recording", e)
    }
}

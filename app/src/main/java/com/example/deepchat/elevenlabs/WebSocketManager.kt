package com.example.deepchat.elevenlabs

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import okio.ByteString

class ElevenLabsWebSocketManager(
    private val context: Context,
    private val agentId: String = "iwFxFjZC0ZE6GBn5PwrI", // Your agent ID
    val apiKey: String,
    private val onTextReceived: (String) -> Unit,
    private val onAudioReceived: (String) -> Unit // URL to the audio file
) {
    private var webSocket: WebSocket? = null
    private val mediaPlayer = MediaPlayer()

    fun connect() {
        val client = OkHttpClient.Builder().build()
        val uri = "wss://api.elevenlabs.io/v1/convai/conversation?agent_id=$agentId"

        val request = Request.Builder()
            .url(uri)
            .header("xi-api-key", apiKey)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Connected to ElevenLabs")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    when (json.optString("type")) {
                        "interruption" -> {
                            Log.d("WebSocket", "Audio interruption")
                        }
                        "user_transcript" -> {
                            val transcript = json.getString("user_transcript")
                            Log.d("WebSocket", "User said: $transcript")
                            CoroutineScope(Dispatchers.Main).launch {
                                onTextReceived(transcript)
                            }
                        }
                        "agent_response" -> {
                            val response = json.getString("agent_response")
                            Log.d("WebSocket", "Agent responded: $response")
                            CoroutineScope(Dispatchers.Main).launch {
                                onTextReceived(response)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("WebSocket", "Error parsing message", e)
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                try {
                    // Save audio to file and play it
                    CoroutineScope(Dispatchers.IO).launch {
                        val audioFile = saveAudioToFile(bytes.toByteArray())
                        CoroutineScope(Dispatchers.Main).launch {
                            onAudioReceived(audioFile.absolutePath)
                            playAudio(audioFile)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("WebSocket", "Error processing audio", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Connection failed", t)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Connection closed: $reason")
            }
        })
    }

    fun sendAudio(audioData: ByteArray) {
        val byteString = ByteString.of(*audioData)
        webSocket?.send(byteString)
    }

    fun sendText(text: String) {
        val jsonObject = JSONObject()
        jsonObject.put("text", text)
        jsonObject.put("name", "text") // This may need to be adjusted based on ElevenLabs API
        webSocket?.send(jsonObject.toString())
    }

    private fun saveAudioToFile(audioData: ByteArray): File {
        val file = File(context.cacheDir, "response_${System.currentTimeMillis()}.mp3")
        FileOutputStream(file).use { it.write(audioData) }
        return file
    }

    private fun playAudio(file: File) {
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(file.absolutePath)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener { it.start() }
        } catch (e: Exception) {
            Log.e("MediaPlayer", "Error playing audio", e)
        }
    }

    fun disconnect() {
        webSocket?.close(1000, null)
        mediaPlayer.release()
    }
}

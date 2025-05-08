package com.example.deepchat.ui

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.deepchat.elevenlabs.ElevenLabsWebSocketManager
import com.example.deepchat.utils.startAudioRecording
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import java.io.File
import java.util.*

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val audioUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(onSignOut: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var textInput by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var recordingJob: Job? by remember { mutableStateOf(null) }

    // Initialize ElevenLabs WebSocket Manager
    val elevenLabsManager = remember {
        ElevenLabsWebSocketManager(
            context = context,
            apiKey = "sk_d27b947a336d9384d02eeb5d4092ff95578b3814b720d65d", // My API Key
            onTextReceived = { text ->
                val isUser = text.startsWith("User:") // Adjust logic as needed
                messages = messages + ChatMessage(
                    text = text,
                    isUser = isUser
                )
            },
            onAudioReceived = { audioUrl ->
                // Handle audio URL from ElevenLabs
                // This might update the last AI message with the audio URL
                if (messages.isNotEmpty() && !messages.last().isUser) {
                    val lastMessage = messages.last()
                    messages = messages.dropLast(1) + lastMessage.copy(audioUrl = audioUrl)
                }
            }
        )
    }

    // Connect to ElevenLabs when screen opens
    LaunchedEffect(Unit) {
        elevenLabsManager.connect()
    }

    // Cleanup when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            elevenLabsManager.disconnect()
            recordingJob?.cancel()
        }
    }

    // Functions for handling audio recording
    val startRecording = {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            recordingJob = scope.launch {
                startAudioRecording(context) { audioData ->
                    elevenLabsManager.sendAudio(audioData)
                }
            }
            isRecording = true
            true
        } else {
            Toast.makeText(
                context,
                "Recording permission is required",
                Toast.LENGTH_SHORT
            ).show()
            false
        }
    }

    val stopRecording = {
        recordingJob?.cancel()
        recordingJob = null
        isRecording = false
        true
    }

    val sendTextMessage = {
        if (textInput.isNotBlank()) {
            // Add user message
            val userMessage = ChatMessage(
                text = textInput,
                isUser = true
            )
            messages = messages + userMessage

            // Clear input
            val messageToSend = textInput
            textInput = ""

            // Send to ElevenLabs (modify this based on their text API)
            scope.launch {
                try {
                    // You'd need to implement this method in your WebSocketManager
                    // to send text messages instead of audio
                    elevenLabsManager.sendText(messageToSend)
                    isProcessing = true
                } catch (e: Exception) {
                    Log.e("ChatScreen", "Error sending message", e)
                    isProcessing = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DeepChat with ElevenLabs") },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.Rounded.ExitToApp,
                            contentDescription = "Sign Out"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Messages list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                reverseLayout = true
            ) {
                items(messages.reversed()) { message ->
                    MessageBubble(
                        message = message,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Start a conversation with ElevenLabs AI",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Input area
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Type a message") },
                        modifier = Modifier.weight(1f),
                        enabled = !isProcessing
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text send button
                    IconButton(
                        onClick = { sendTextMessage() },
                        enabled = textInput.isNotBlank() && !isProcessing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send"
                        )
                    }

                    // Voice record button
                    IconButton(
                        onClick = {
                            if (isRecording) {
                                stopRecording()
                            } else {
                                startRecording()
                            }
                        },
                        enabled = !isProcessing,
                        modifier = Modifier
                            .padding(4.dp)
                            .size(48.dp)
                            .background(
                                if (isRecording) Color.Red else MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isUser) 16.dp else 0.dp,
                        bottomEnd = if (message.isUser) 0.dp else 16.dp
                    )
                )
                .background(
                    if (message.isUser) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.secondaryContainer
                )
                .padding(16.dp)
        ) {
            Text(
                text = message.text,
                color = if (message.isUser) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        // Audio playback button if message has audio
        message.audioUrl?.let {
            TextButton(
                onClick = { /* Play audio logic */ },
                modifier = Modifier.align(if (message.isUser) Alignment.End else Alignment.Start)
            ) {
                Text("Play Audio")
            }
        }
    }
}

package com.example.deepchat.model

/**
 * Request body for your backend
 */
data class ChatRequest(
    val text: String? = null,    // Text input
    val audio: String? = null    // Base64 audio (if using voice)
)

/**
 * Response from your backend (assuming it returns text + audio URL)
 */
data class ChatResponse(
    val text: String,     // Response text (for UI)
    val audioUrl: String  // URL to ElevenLabs-generated audio
)
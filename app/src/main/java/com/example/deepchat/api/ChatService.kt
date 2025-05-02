package com.example.deepchat.api

import com.example.deepchat.model.ChatRequest
import com.example.deepchat.model.ChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ChatService {
    /**
     * Send text/voice to your backend
     * @param apiKey Your backend API key (if required)
     * @param request Contains text OR audio
     */
    @POST("chat") // Replace with your actual endpoint
    suspend fun sendMessage(
        @Header("Authorization") apiKey: String,
        @Body request: ChatRequest
    ): Response<ChatResponse>
}
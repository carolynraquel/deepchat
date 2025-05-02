package com.example.deepchat.network

import com.example.deepchat.api.ChatService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {
    // Replace with your backend's base URL
    private const val BASE_URL = "https://your-backend.com/api/v1/"

    val chatService: ChatService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatService::class.java)
    }
}
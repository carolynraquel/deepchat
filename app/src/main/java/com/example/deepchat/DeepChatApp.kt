package com.example.deepchat

import android.app.Application
import com.google.firebase.FirebaseApp

class DeepChatApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)

    }
}
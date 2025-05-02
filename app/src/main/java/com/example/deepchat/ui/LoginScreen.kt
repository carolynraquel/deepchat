package com.example.deepchat.ui

import android.Manifest
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.deepchat.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import com.example.deepchat.auth.SignInResult
import com.example.deepchat.auth.signInWithIntent

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val auth = Firebase.auth

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }

    // This is important - we need to create a reference to handle this flow
    var shouldRequestMicPermission by remember { mutableStateOf(false) }
    var googleSignInSuccess by remember { mutableStateOf(false) }

    // Voice permission state - defined first, but only launched when needed
    val recordPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted - proceed with full voice features
            Toast.makeText(
                context,
                "Voice features enabled",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            // Show rationale but still allow the user to proceed with limited functionality
            Toast.makeText(
                context,
                "Voice features disabled. Some functionality will be limited.",
                Toast.LENGTH_LONG
            ).show()
        }
        // Proceed to main screen regardless of permission result
        onLoginSuccess()
    }

    // For Google Sign In
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            isGoogleLoading = false
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                scope.launch {
                    result.data?.let { intent ->
                        when (val signInResult = signInWithIntent(intent)) {
                            is SignInResult.Success -> {
                                Toast.makeText(context, "Sign in successful", Toast.LENGTH_SHORT).show()
                                googleSignInSuccess = true
                                // Set flag to request permission
                                shouldRequestMicPermission = true
                            }
                            is SignInResult.Failure -> {
                                Toast.makeText(
                                    context,
                                    "Sign in failed: ${signInResult.exception.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            is SignInResult.Canceled -> {
                                Toast.makeText(
                                    context,
                                    "Sign in canceled",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    )

    // Effect to handle mic permission request after successful Google sign in
    LaunchedEffect(shouldRequestMicPermission) {
        if (shouldRequestMicPermission) {
            recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            shouldRequestMicPermission = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "DeepChat",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        isLoading = false
                        if (task.isSuccessful) {
                            //Mic permission after successful login by setting flag
                            shouldRequestMicPermission = true
                        } else {
                            Toast.makeText(
                                context,
                                "Authentication failed: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Sign In")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "OR", modifier = Modifier.padding(vertical = 16.dp))

        OutlinedButton(
            onClick = {
                isGoogleLoading = true

                // Create intent for Google Sign-In
                // Note: I'll need to implement this part separately as it's not in the provided GoogleAuth.kt
                // This is a placeholder for your actual Google Sign-In implementation
                val googleSignInIntent = Intent().apply {
                    putExtra("sign_in_action", "google")
                    // Add necessary data for your Google Sign-In implementation
                }

                try {
                    launcher.launch(googleSignInIntent)
                } catch (e: Exception) {
                    isGoogleLoading = false
                    Toast.makeText(
                        context,
                        "Could not start Google Sign In: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isGoogleLoading
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isGoogleLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    // Make sure you have the Google logo in your drawable resources
                    Image(
                        painter = painterResource(id = R.drawable.google_logo),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Sign In with Google")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // For demo/testing: Skip straight to the main app
                onLoginSuccess()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Skip Login (Demo)")
        }
    }
}
package com.example.marketpredictionapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.marketpredictionapp.ui.screens.AuthScreen
import com.example.marketpredictionapp.ui.screens.MainScreen
import com.example.marketpredictionapp.utils.SessionManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    private lateinit var analytics: FirebaseAnalytics
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        analytics      = Firebase.analytics
        sessionManager = SessionManager(this)

        // Barra de status oscura
        window.statusBarColor = Color(0xFF0A0E1A).toArgb()

        setContent {
            // Estado de sesión: token guardado o null
            var token by remember {
                mutableStateOf(sessionManager.getToken())
            }

            if (token != null) {
                MainScreen(
                    token = token!!,
                    onLogout = {
                        analytics.logEvent("logout") {}
                        sessionManager.logout()
                        token = null
                    }
                )
            } else {
                AuthScreen(
                    onLoginSuccess = { newToken ->
                        sessionManager.saveToken(newToken)
                        analytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
                            param(FirebaseAnalytics.Param.METHOD, "jwt_fastapi")
                        }
                        token = newToken
                    }
                )
            }
        }
    }
}
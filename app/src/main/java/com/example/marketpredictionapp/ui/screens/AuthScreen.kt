package com.example.marketpredictionapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marketpredictionapp.network.RetrofitClient
import com.example.marketpredictionapp.model.LoginRequest
import com.example.marketpredictionapp.model.RegisterRequest
import kotlinx.coroutines.launch

// ─── Colores ──────────────────────────────────────────────────
private val BgTop    = Color(0xFF0A0E1A)
private val BgBot    = Color(0xFF0F1629)
private val Accent   = Color(0xFF00D4FF)
private val Accent2  = Color(0xFF00FF88)
private val CardBg   = Color(0xFF1A2540)
private val TextMain = Color(0xFFE2EAF8)
private val TextMuted= Color(0xFF4A6080)
private val ErrorClr = Color(0xFFFF6B6B)

@Composable
fun AuthScreen(onLoginSuccess: (String) -> Unit) {
    var isLogin by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passVisible by remember { mutableStateOf(false) }
    var loading  by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    fun submit() {
        if (username.isBlank() || password.isBlank()) {
            errorMsg = "Completa todos los campos"; return
        }
        errorMsg = ""
        loading = true
        scope.launch {
            try {
                val response = if (isLogin) {
                    RetrofitClient.instance.login(LoginRequest(username, password))
                } else {
                    if (email.isBlank()) { errorMsg = "El email es requerido"; loading = false; return@launch }
                    RetrofitClient.instance.register(RegisterRequest(username, password, email))
                }
                if (response.isSuccessful && response.body() != null) {
                    onLoginSuccess(response.body()!!.accessToken)
                } else {
                    errorMsg = "Error: ${response.code()} — verifica tus credenciales"
                }
            } catch (e: Exception) {
                errorMsg = "Sin conexión con el servidor"
            } finally {
                loading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgBot))),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título
                Text(
                    text = "CLIMA & MERCADOS",
                    color = Accent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = if (isLogin) "Iniciar sesión" else "Crear cuenta",
                    color = TextMuted,
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(4.dp))

                // Username
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Usuario", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedTextFieldColors(),
                )

                // Email (solo registro)
                if (!isLogin) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedTextFieldColors(),
                    )
                }

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña", color = TextMuted) },
                    singleLine = true,
                    visualTransformation = if (passVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { passVisible = !passVisible }) {
                            Text(if (passVisible) "Ocultar" else "Ver",
                                color = Accent, fontSize = 11.sp)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedTextFieldColors(),
                )

                // Error
                if (errorMsg.isNotEmpty()) {
                    Text(errorMsg, color = ErrorClr, fontSize = 12.sp)
                }

                // Botón principal
                Button(
                    onClick = { submit() },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    if (loading) CircularProgressIndicator(
                        color = BgTop, modifier = Modifier.size(20.dp), strokeWidth = 2.dp
                    )
                    else Text(
                        if (isLogin) "Ingresar" else "Registrarse",
                        color = BgTop, fontWeight = FontWeight.Bold
                    )
                }

                // Toggle login/registro
                TextButton(onClick = {
                    isLogin = !isLogin; errorMsg = ""
                }) {
                    Text(
                        if (isLogin) "¿No tienes cuenta? Regístrate"
                        else "¿Ya tienes cuenta? Inicia sesión",
                        color = Accent2, fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = Accent,
    unfocusedBorderColor = Color(0xFF1E2D4A),
    focusedTextColor     = TextMain,
    unfocusedTextColor   = TextMain,
    cursorColor          = Accent,
)
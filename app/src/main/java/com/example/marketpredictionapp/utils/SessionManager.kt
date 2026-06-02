package com.example.marketpredictionapp.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Maneja la sesión del usuario guardando el JWT en SharedPreferences.
 * Uso: SessionManager(context).saveToken(token)
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME  = "clima_mercados_prefs"
        private const val KEY_TOKEN   = "jwt_token"
        private const val KEY_USER    = "username"
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun saveUsername(username: String) {
        prefs.edit().putString(KEY_USER, username).apply()
    }

    fun getUsername(): String? = prefs.getString(KEY_USER, null)

    fun isLoggedIn(): Boolean = getToken() != null

    fun logout() {
        prefs.edit().clear().apply()
    }
}
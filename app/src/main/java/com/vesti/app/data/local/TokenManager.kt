package com.vesti.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import org.json.JSONObject
import android.util.Base64

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {
    companion object {
        private val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[JWT_TOKEN_KEY] = token
        }
    }

    suspend fun saveUser(name: String?, email: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name ?: ""
            preferences[USER_EMAIL_KEY] = email
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(JWT_TOKEN_KEY)
            preferences.remove(USER_NAME_KEY)
            preferences.remove(USER_EMAIL_KEY)
        }
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[JWT_TOKEN_KEY]
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        val savedName = preferences[USER_NAME_KEY]
        if (!savedName.isNullOrEmpty()) {
            savedName
        } else {
            val token = preferences[JWT_TOKEN_KEY]
            parseEmailFromToken(token)?.split("@")?.firstOrNull() ?: "Kullanıcı"
        }
    }

    val userEmailFlow: Flow<String> = context.dataStore.data.map { preferences ->
        val savedEmail = preferences[USER_EMAIL_KEY]
        if (!savedEmail.isNullOrEmpty()) {
            savedEmail
        } else {
            val token = preferences[JWT_TOKEN_KEY]
            parseEmailFromToken(token) ?: "destek@vesti.com"
        }
    }

    private fun parseEmailFromToken(token: String?): String? {
        if (token == null || !token.startsWith("jwt_session_token_")) return null
        return try {
            val base64Part = token.substring("jwt_session_token_".length)
            val decodedBytes = Base64.decode(base64Part, Base64.DEFAULT)
            val decodedString = String(decodedBytes, Charsets.UTF_8)
            val json = JSONObject(decodedString)
            json.optString("email", null)
        } catch (e: Exception) {
            null
        }
    }
}

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
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[JWT_TOKEN_KEY] = token
        }
    }

    suspend fun saveUser(id: String?, name: String?, email: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = id ?: ""
            preferences[USER_NAME_KEY] = name ?: ""
            preferences[USER_EMAIL_KEY] = email
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(JWT_TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_NAME_KEY)
            preferences.remove(USER_EMAIL_KEY)
        }
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[JWT_TOKEN_KEY]
    }

    val userIdFlow: Flow<String> = context.dataStore.data.map { preferences ->
        val savedId = preferences[USER_ID_KEY]
        if (!savedId.isNullOrEmpty()) {
            savedId
        } else {
            val token = preferences[JWT_TOKEN_KEY]
            parseUserIdFromToken(token) ?: ""
        }
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
        if (token == null) return null
        return try {
            if (token.startsWith("jwt_session_token_")) {
                val base64Part = token.substring("jwt_session_token_".length)
                val decodedBytes = Base64.decode(base64Part, Base64.DEFAULT)
                val decodedString = String(decodedBytes, Charsets.UTF_8)
                val json = JSONObject(decodedString)
                return json.optString("email", null)
            }
            val parts = token.split(".")
            if (parts.size >= 2) {
                val payload = parts[1]
                val decodedBytes = Base64.decode(payload, Base64.DEFAULT or Base64.NO_WRAP or Base64.URL_SAFE)
                val decodedString = String(decodedBytes, Charsets.UTF_8)
                val json = JSONObject(decodedString)
                return json.optString("email", null)
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun parseUserIdFromToken(token: String?): String? {
        if (token == null) return null
        return try {
            if (token.startsWith("jwt_session_token_")) {
                val base64Part = token.substring("jwt_session_token_".length)
                val decodedBytes = Base64.decode(base64Part, Base64.DEFAULT)
                val decodedString = String(decodedBytes, Charsets.UTF_8)
                val json = JSONObject(decodedString)
                return json.optString("id", null) ?: json.optString("userId", null)
            }
            val parts = token.split(".")
            if (parts.size >= 2) {
                val payload = parts[1]
                val decodedBytes = Base64.decode(payload, Base64.DEFAULT or Base64.NO_WRAP or Base64.URL_SAFE)
                val decodedString = String(decodedBytes, Charsets.UTF_8)
                val json = JSONObject(decodedString)
                return json.optString("id", null) ?: json.optString("userId", null)
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}

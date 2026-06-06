package com.vesti.app.data.repository

import com.vesti.app.data.local.TokenManager
import com.vesti.app.data.network.AuthApi
import com.vesti.app.data.network.LoginRequest
import com.vesti.app.data.network.RegisterRequest
import org.json.JSONObject

class AuthRepository(private val authApi: AuthApi, private val tokenManager: TokenManager) {

    suspend fun login(request: LoginRequest): Result<String> {
        return try {
            val response = authApi.login(request)
            if (response.isSuccessful && response.body() != null) {
                val token = response.body()?.token
                if (!token.isNullOrEmpty()) {
                    tokenManager.saveToken(token)
                    val user = response.body()?.user
                    if (user != null) {
                        tokenManager.saveUser(user.name, user.email)
                    }
                    Result.success("Giriş başarılı")
                } else {
                    Result.failure(Exception("Sunucudan token alınamadı"))
                }
            } else {
                // Hata mesajını JSON'dan düzgün parse et
                val errorJson = response.errorBody()?.string()
                val errorMsg = try {
                    JSONObject(errorJson ?: "{}").optString("error", null)
                        ?: JSONObject(errorJson ?: "{}").optString("message", "Giriş başarısız")
                } catch (e: Exception) {
                    "E-posta veya şifre hatalı"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val msg = when {
                e.message?.contains("Unable to resolve host") == true ||
                e.message?.contains("failed to connect") == true ->
                    "Sunucuya bağlanılamadı. İnternet bağlantınızı kontrol edin."
                e.message?.contains("timeout") == true ->
                    "Sunucu yanıt vermiyor. Lütfen tekrar deneyin."
                else -> e.message ?: "Bilinmeyen bir hata oluştu"
            }
            Result.failure(Exception(msg))
        }
    }

    suspend fun register(request: RegisterRequest): Result<String> {
        return try {
            val response = authApi.register(request)
            if (response.isSuccessful && response.body() != null) {
                val token = response.body()?.token
                if (!token.isNullOrEmpty()) {
                    tokenManager.saveToken(token)
                    val user = response.body()?.user
                    if (user != null) {
                        tokenManager.saveUser(user.name, user.email)
                    }
                }
                Result.success("Kayıt başarılı")
            } else {
                val errorJson = response.errorBody()?.string()
                val errorMsg = try {
                    JSONObject(errorJson ?: "{}").optString("error", null)
                        ?: JSONObject(errorJson ?: "{}").optString("message", "Kayıt başarısız")
                } catch (e: Exception) {
                    "Bu e-posta adresi zaten kullanımda olabilir"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val msg = when {
                e.message?.contains("Unable to resolve host") == true ||
                e.message?.contains("failed to connect") == true ->
                    "Sunucuya bağlanılamadı. İnternet bağlantınızı kontrol edin."
                else -> e.message ?: "Bilinmeyen bir hata oluştu"
            }
            Result.failure(Exception(msg))
        }
    }

    suspend fun loginWithGoogle(idToken: String): Result<String> {
        return try {
            val response = authApi.googleLogin(com.vesti.app.data.network.GoogleAuthRequest(idToken))
            if (response.isSuccessful && response.body() != null) {
                val token = response.body()?.token
                if (!token.isNullOrEmpty()) {
                    tokenManager.saveToken(token)
                    val user = response.body()?.user
                    if (user != null) {
                        tokenManager.saveUser(user.name, user.email)
                    }
                    Result.success("Google ile başarıyla giriş yapıldı")
                } else {
                    Result.failure(Exception("Google girişi sırasında token alınamadı"))
                }
            } else {
                val errorJson = response.errorBody()?.string()
                val errorMsg = try {
                    JSONObject(errorJson ?: "{}").optString("error", "Google girişi başarısız")
                } catch (e: Exception) { "Google girişi başarısız" }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Google girişi sırasında hata oluştu"))
        }
    }

    suspend fun logout() {
        tokenManager.clearToken()
    }
}


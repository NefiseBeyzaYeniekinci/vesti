package com.vesti.app.data.repository

import com.vesti.app.data.local.TokenManager
import com.vesti.app.data.network.AuthApi
import com.vesti.app.data.network.LoginRequest
import com.vesti.app.data.network.RegisterRequest

class AuthRepository(private val authApi: AuthApi, private val tokenManager: TokenManager) {

    suspend fun login(request: LoginRequest): Result<String> {
        // MOCK LOGIN: Backend olmadığı için doğrudan giriş başarılı kabul ediliyor.
        kotlinx.coroutines.delay(1000) // 1 saniye bekle
        tokenManager.saveToken("mock_token_12345")
        return Result.success("Login successful")
        
        /* Gerçek API Uygulaması
        return try {
            val response = authApi.login(request)
            if (response.isSuccessful && response.body() != null) {
                val token = response.body()?.token
                if (!token.isNullOrEmpty()) {
                    tokenManager.saveToken(token)
                    Result.success("Login successful")
                } else {
                    Result.failure(Exception("Token missing from response"))
                }
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
        */
    }

    suspend fun register(request: RegisterRequest): Result<String> {
        // MOCK REGISTER: Backend olmadığı için doğrudan kayıt başarılı kabul ediliyor.
        kotlinx.coroutines.delay(1000)
        return Result.success("Registration successful")
        
        /* Gerçek API Uygulaması
        return try {
            val response = authApi.register(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success("Registration successful")
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
        */
    }
    
    suspend fun logout() {
        tokenManager.clearToken()
    }
}

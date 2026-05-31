package com.vesti.app.data.repository

import com.vesti.app.data.local.TokenManager
import com.vesti.app.data.network.AuthApi
import com.vesti.app.data.network.LoginRequest
import com.vesti.app.data.network.RegisterRequest

class AuthRepository(private val authApi: AuthApi, private val tokenManager: TokenManager) {

    suspend fun login(request: LoginRequest): Result<String> {
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
    }

    suspend fun register(request: RegisterRequest): Result<String> {
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
    }
    
    suspend fun loginWithGoogle(): Result<String> {
        tokenManager.saveToken("google_mock_token_abc123")
        return Result.success("Google login successful")
    }

    suspend fun logout() {
        tokenManager.clearToken()
    }
}

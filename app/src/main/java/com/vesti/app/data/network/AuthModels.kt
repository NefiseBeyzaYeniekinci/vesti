package com.vesti.app.data.network

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class GoogleAuthRequest(
    val idToken: String
)

data class AuthResponse(
    val message: String,
    val token: String?,
    val user: UserDto?
)

data class UserResponse(
    val user: UserDto
)

data class UserDto(
    val id: String,
    val name: String?,
    val email: String
)


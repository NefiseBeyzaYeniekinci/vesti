package com.vesti.app.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    // For emulator testing, use 10.0.2.2 instead of localhost
    // Real devices use your computer's active local Wi-Fi IP (192.168.1.103)
    private const val BASE_URL = "http://192.168.1.103:8080/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }
}

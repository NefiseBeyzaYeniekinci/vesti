package com.vesti.app.data.network

import com.vesti.app.data.local.TokenManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Kendi makinenizdeki backend'e ulaşmak için emulator üzerinden 10.0.2.2 kullanılır.
    // Ancak fiziksel cihazlar için bilgisayarınızın yerel Wi-Fi IP'si (örn. 192.168.1.103) kullanılır.
    private const val HOST_IP = "10.0.2.2"
    private val AUTH_BASE_URL = "http://$HOST_IP:8080/"
    private val WARDROBE_BASE_URL = "http://$HOST_IP:8081/"
    private val AI_BASE_URL = "http://$HOST_IP:8082/"
    private val MARKETPLACE_BASE_URL = "http://$HOST_IP:8083/"
    private val PAYMENT_BASE_URL = "http://$HOST_IP:8084/"
    // OpenWeather API Base URL
    private const val WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/"
    
    private fun getBaseOkHttpClient(tokenManager: TokenManager): OkHttpClient {
        val authInterceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            
            // Dispatchers.IO bağlamında bloklayarak tokeni alıyoruz zira interceptor asenkron flow desteklemiyor, senkron çalışıyor
            val token = runBlocking {
                tokenManager.tokenFlow.firstOrNull()
            }
            
            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
            chain.proceed(requestBuilder.build())
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY    
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun getAuthApi(tokenManager: TokenManager): AuthApi {
        return Retrofit.Builder()
            .baseUrl(AUTH_BASE_URL)
            .client(getBaseOkHttpClient(tokenManager))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }

    fun getWardrobeApi(tokenManager: TokenManager): WardrobeApi {
        return Retrofit.Builder()
            .baseUrl(WARDROBE_BASE_URL)
            .client(getBaseOkHttpClient(tokenManager))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WardrobeApi::class.java)
    }

    fun getAiApi(tokenManager: TokenManager): AiApi {
        return Retrofit.Builder()
            .baseUrl(AI_BASE_URL)
            .client(getBaseOkHttpClient(tokenManager))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AiApi::class.java)
    }

    fun getWeatherApi(): WeatherApi {
        // Hava durumu API'si için token eklemeye gerek yok, bu yüzden standart OkHttpClient kullanıyoruz
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY    
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(WEATHER_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }

    fun getMarketplaceApi(tokenManager: TokenManager): MarketplaceApi {
        return Retrofit.Builder()
            .baseUrl(MARKETPLACE_BASE_URL)
            .client(getBaseOkHttpClient(tokenManager))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MarketplaceApi::class.java)
    }

    fun getPaymentApi(tokenManager: TokenManager): PaymentApi {
        return Retrofit.Builder()
            .baseUrl(PAYMENT_BASE_URL)
            .client(getBaseOkHttpClient(tokenManager))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PaymentApi::class.java)
    }
}

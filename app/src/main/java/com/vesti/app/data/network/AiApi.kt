package com.vesti.app.data.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AiApi {

    @GET("health")
    suspend fun getHealthStatus(): Response<HealthStatusResponse>

    @POST("api/ai/recommend")
    suspend fun getRecommendation(@Body request: OutfitRecommendationRequest): Response<RecommendationResponse>

    @Multipart
    @POST("api/ai/tag-image")
    suspend fun tagImage(
        @Part image: MultipartBody.Part
    ): Response<AiTagResponse>
}

data class HealthStatusResponse(val status: String)

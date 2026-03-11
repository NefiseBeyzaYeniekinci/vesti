package com.vesti.app.data.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface WardrobeApi {

    @GET("api/wardrobe/items")
    suspend fun getWardrobeItems(): Response<List<WardrobeItemDto>>

    @Multipart
    @POST("api/wardrobe/upload")
    suspend fun uploadClothing(
        @Part image: MultipartBody.Part,
        @Part("category") category: RequestBody?,
        @Part("color") color: RequestBody?,
        @Part("brand") brand: RequestBody?,
        @Part("size") size: RequestBody?
    ): Response<UploadResponse>

    @DELETE("api/wardrobe/items/{id}")
    suspend fun deleteItem(@Path("id") id: String): Response<Unit>
}

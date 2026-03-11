package com.vesti.app.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MarketplaceApi {

    @GET("api/marketplace/items")
    suspend fun getFeedItems(): Response<List<MarketplaceItemDto>>

    @GET("api/marketplace/items/{id}")
    suspend fun getItemDetails(@Path("id") id: String): Response<MarketplaceItemDto>

    @POST("api/marketplace/items")
    suspend fun createItem(@Body request: CreateMarketplaceItemRequest): Response<MarketplaceItemDto>
}

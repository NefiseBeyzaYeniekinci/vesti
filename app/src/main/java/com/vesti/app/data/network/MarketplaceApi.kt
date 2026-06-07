package com.vesti.app.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class FavoriteItemDto(
    val id: String,
    val userId: String,
    val listingId: String,
    val listing: MarketplaceItemDto
)

data class FavoritesResponse(
    val success: Boolean,
    val data: List<FavoriteItemDto>
)

data class ToggleFavoriteRequest(
    val listingId: String
)

data class ToggleFavoriteResponse(
    val success: Boolean,
    val action: String
)

interface MarketplaceApi {

    @GET("api/marketplace/items")
    suspend fun getFeedItems(): Response<List<MarketplaceItemDto>>

    @GET("api/marketplace/items/{id}")
    suspend fun getItemDetails(@Path("id") id: String): Response<MarketplaceItemDto>

    @POST("api/marketplace/items")
    suspend fun createItem(@Body request: CreateMarketplaceItemRequest): Response<MarketplaceItemDto>

    @GET("api/favorites")
    suspend fun getFavorites(): Response<FavoritesResponse>

    @POST("api/favorites")
    suspend fun toggleFavorite(@Body request: ToggleFavoriteRequest): Response<ToggleFavoriteResponse>

    @retrofit2.http.DELETE("api/marketplace/items/{id}")
    suspend fun deleteItem(@Path("id") id: String): Response<Unit>
}

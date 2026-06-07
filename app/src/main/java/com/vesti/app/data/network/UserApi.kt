package com.vesti.app.data.network

import retrofit2.Response
import retrofit2.http.*

data class CityResponse(val cityCode: String)
data class CityRequest(val cityCode: String)

data class ProfileUpdateRequest(
    val name: String? = null,
    val bio: String? = null,
    val location: String? = null,
    val isPublic: Boolean? = null,
    val currentPassword: String? = null,
    val newPassword: String? = null
)

data class CardRequest(
    val cardName: String,
    val cardNumber: String,
    val expiryDate: String
)

data class SavedCardDto(
    val id: String,
    val cardName: String,
    val cardNumber: String,
    val expiryDate: String
)

data class SavedCardResponse(
    val success: Boolean,
    val card: SavedCardDto
)

data class TrackingEventDto(
    val id: String,
    val status: String,
    val description: String,
    val location: String?,
    val createdAt: String
)

data class OrderListingDto(
    val id: String,
    val title: String,
    val images: List<String>,
    val category: String? = null
)

data class OrderUserDto(
    val name: String?,
    val image: String?
)

data class OrderDto(
    val id: String,
    val status: String,
    val price: Double,
    val currency: String,
    val trackingNumber: String?,
    val trackingCarrier: String?,
    val createdAt: String,
    val listing: OrderListingDto,
    val seller: OrderUserDto,
    val buyer: OrderUserDto,
    val events: List<TrackingEventDto>? = null
)

data class TrackingRequest(
    val trackingNumber: String,
    val trackingCarrier: String
)

data class PromoCodeDto(
    val code: String,
    val discountType: String,
    val discountValue: Double,
    val description: String?,
    val expiresAt: String?
)

data class RedeemedPromoDto(
    val id: String,
    val redeemedAt: String,
    val promoCode: PromoCodeDto
)

data class RedeemRequest(val code: String)
data class RedeemResponse(
    val message: String,
    val redemption: RedeemedPromoDto
)

data class UserProfileDto(
    val id: String,
    val name: String?,
    val email: String,
    val image: String?,
    val bio: String?,
    val location: String?,
    val isPublic: Boolean,
    val trustScore: Double?,
    val savedCards: List<SavedCardDto>?
)

data class UserProfileResponse(
    val success: Boolean,
    val user: UserProfileDto
)

data class StyleProfileDto(
    val favoriteColors: List<String>? = emptyList(),
    val unwantedColors: List<String>? = emptyList(),
    val stylePreference: String? = "CASUAL",
    val fitPreference: String? = "",
    val fabricPreference: String? = "",
    val bodyType: String? = "UNKNOWN",
    val sizeTops: String? = "",
    val sizeBottoms: String? = "",
    val sizeShoes: String? = ""
)

interface UserApi {
    @GET("api/user/profile")
    suspend fun getProfile(): Response<UserProfileResponse>

    @GET("api/user/city")
    suspend fun getCity(): Response<CityResponse>

    @PUT("api/user/city")
    suspend fun updateCity(@Body request: CityRequest): Response<Unit>

    @PUT("api/user/profile")
    suspend fun updateProfile(@Body request: ProfileUpdateRequest): Response<Unit>

    @POST("api/user/cards")
    suspend fun addCard(@Body request: CardRequest): Response<SavedCardResponse>

    @DELETE("api/user/cards")
    suspend fun deleteCard(@Query("id") id: String): Response<Unit>

    @GET("api/orders")
    suspend fun getOrders(@Query("role") role: String): Response<List<OrderDto>>

    @PATCH("api/orders/{id}/tracking")
    suspend fun updateTracking(
        @Path("id") id: String,
        @Body request: TrackingRequest
    ): Response<Unit>

    @GET("api/user/promotions")
    suspend fun getPromotions(): Response<List<RedeemedPromoDto>>

    @POST("api/user/promotions")
    suspend fun redeemPromotion(@Body request: RedeemRequest): Response<RedeemResponse>

    @GET("api/style-profile")
    suspend fun getStyleProfile(): Response<StyleProfileDto?>

    @POST("api/style-profile")
    suspend fun updateStyleProfile(@Body request: StyleProfileDto): Response<Unit>
}

package com.vesti.app.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH

data class NotificationDto(
    val id: String,
    val title: String,
    val description: String,
    val type: String,
    val href: String,
    val read: Boolean,
    val createdAt: String
)

interface NotificationApi {
    @GET("api/notifications")
    suspend fun getNotifications(): Response<List<NotificationDto>>

    @PATCH("api/notifications")
    suspend fun markAllAsRead(): Response<Unit>
}

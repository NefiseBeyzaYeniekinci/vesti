package com.vesti.app.data.network

data class WardrobeItemDto(
    val id: String,
    val imageUrl: String,
    val category: String,
    val color: String,
    val brand: String,
    val size: String,
    val createdAt: String
)

data class UploadResponse(
    val message: String,
    val item: WardrobeItemDto
)

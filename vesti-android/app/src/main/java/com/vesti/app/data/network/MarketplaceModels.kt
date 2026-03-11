package com.vesti.app.data.network

data class MarketplaceItemDto(
    val id: String,
    val sellerId: String,
    val title: String,
    val description: String,
    val price: Double,
    val currency: String,
    val imageUrl: String,
    val category: String,
    val size: String,
    val condition: String,
    val createdAt: String
)

data class CreateMarketplaceItemRequest(
    val title: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String,
    val size: String,
    val condition: String
)

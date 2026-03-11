package com.vesti.app.data.network

data class OutfitRecommendationRequest(
    val user_id: String,
    val weather_condition: String,
    val temperature: Int,
    val style_preference: String
)

data class RecommendationResponse(
    val outfit_id: String,
    val description: String,
    val items: List<WardrobeItemDto>
)

data class AiTagResponse(
    val filename: String,
    val predicted_category: String,
    val predicted_color: String,
    val predicted_style: String,
    val confidence_score: Double
)

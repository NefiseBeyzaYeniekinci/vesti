package com.vesti.app.ui.outfit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vesti.app.data.network.AiApi
import com.vesti.app.data.network.OutfitRecommendationRequest
import com.vesti.app.data.network.RecommendationResponse
import com.vesti.app.data.network.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.vesti.app.data.network.WeatherApi
import com.vesti.app.data.network.WardrobeApi
import com.vesti.app.data.network.WardrobeItemDto

sealed class OutfitState {
    object Idle : OutfitState()
    object Loading : OutfitState()
    data class Success(val recommendation: RecommendationResponse) : OutfitState()
    data class Error(val message: String) : OutfitState()
}

class OutfitViewModel(
    private val aiApi: AiApi,
    private val weatherApi: WeatherApi,
    private val wardrobeApi: WardrobeApi
) : ViewModel() {

    private val _state = MutableStateFlow<OutfitState>(OutfitState.Idle)
    val state: StateFlow<OutfitState> = _state.asStateFlow()

    private val _wardrobeItems = MutableStateFlow<List<WardrobeItemDto>>(emptyList())
    val wardrobeItems: StateFlow<List<WardrobeItemDto>> = _wardrobeItems.asStateFlow()

    fun loadWardrobeItems() {
        viewModelScope.launch {
            try {
                val response = wardrobeApi.getWardrobeItems()
                if (response.isSuccessful && response.body() != null) {
                    _wardrobeItems.value = response.body()!!
                }
            } catch (_: Exception) {}
        }
    }

    fun getRecommendation(
        userId: String = "test-user",
        weather: String = "Güneşli",
        temp: Int = 25,
        style: String = "Casual"
    ) {
        viewModelScope.launch {
            _state.value = OutfitState.Loading
            try {
                val request = OutfitRecommendationRequest(
                    user_id = userId,
                    weather_condition = weather,
                    temperature = temp,
                    style_preference = style
                )

                val response = aiApi.getRecommendation(request)
                if (response.isSuccessful && response.body() != null) {
                    _state.value = OutfitState.Success(response.body()!!)
                } else {
                    _state.value = OutfitState.Error(response.errorBody()?.string() ?: "Öneri alınamadı")
                }
            } catch (e: Exception) {
                _state.value = OutfitState.Error(e.message ?: "Öneri servisine ulaşılamadı")
            }
        }
    }

    fun fetchRecommendationWithLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            _state.value = OutfitState.Loading
            try {
                val weatherResponse = weatherApi.getCurrentWeather(
                    lat = lat,
                    lon = lon,
                    appId = "46d68849b621de45187315bdcbfd1121"
                )

                if (weatherResponse.isSuccessful && weatherResponse.body() != null) {
                    val weatherData = weatherResponse.body()!!
                    val temp = Math.round(weatherData.main.temp).toInt()
                    val description = weatherData.weather.firstOrNull()?.description ?: "Açık"
                    
                    getRecommendation(weather = description, temp = temp)
                } else {
                    _state.value = OutfitState.Error("Hava durumu bilgisi alınamadı. (Code: ${weatherResponse.code()})")
                }
            } catch (e: Exception) {
                _state.value = OutfitState.Error("Konum/Hava Durumu hatası: ${e.message}")
            }
        }
    }

    /**
     * Chatbot için anlık hava durumunu döndürür.
     * OutfitState'i değiştirmeden sadece veri döndürür.
     */
    suspend fun getWeatherForChat(lat: Double, lon: Double): WeatherResponse? {
        return try {
            val response = weatherApi.getCurrentWeather(
                lat = lat,
                lon = lon,
                appId = "46d68849b621de45187315bdcbfd1121"
            )
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }
}


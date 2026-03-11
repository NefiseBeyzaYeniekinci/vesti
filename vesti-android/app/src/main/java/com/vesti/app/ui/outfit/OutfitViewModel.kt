package com.vesti.app.ui.outfit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vesti.app.data.network.AiApi
import com.vesti.app.data.network.OutfitRecommendationRequest
import com.vesti.app.data.network.RecommendationResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class OutfitState {
    object Idle : OutfitState()
    object Loading : OutfitState()
    data class Success(val recommendation: RecommendationResponse) : OutfitState()
    data class Error(val message: String) : OutfitState()
}

class OutfitViewModel(
    private val aiApi: AiApi,
    private val weatherApi: WeatherApi
) : ViewModel() {

    private val _state = MutableStateFlow<OutfitState>(OutfitState.Idle)
    val state: StateFlow<OutfitState> = _state.asStateFlow()

    fun getRecommendation(
        userId: String = "test-user",
        weather: String = "Güneşli",
        temp: Int = 25,
        style: String = "Casual"
    ) {
        viewModelScope.launch {
            _state.value = OutfitState.Loading
            try {
                // Burada doğrudan gerçek hava durumunu AI API'ye yolluyoruz.
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
                    _state.value = OutfitState.Error("Failed to get recommendation: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.value = OutfitState.Error("AI Service unavailable: ${e.message}")
            }
    fun fetchRecommendationWithLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            _state.value = OutfitState.Loading
            try {
                // WeatherApi üzerinden OpenWeather verisini çağır
                val weatherResponse = weatherApi.getCurrentWeather(
                    lat = lat,
                    lon = lon,
                    appId = "46d68849b621de45187315bdcbfd1121"
                )

                if (weatherResponse.isSuccessful && weatherResponse.body() != null) {
                    val weatherData = weatherResponse.body()!!
                    val temp = Math.round(weatherData.main.temp).toInt()
                    val description = weatherData.weather.firstOrNull()?.description ?: "Açık"
                    
                    // Alınan gerçek veriyi AI Modeline pasla
                    getRecommendation(weather = description, temp = temp)
                } else {
                    _state.value = OutfitState.Error("Hava durumu bilgisi alınamadı. (Code: ${weatherResponse.code()})")
                }
            } catch (e: Exception) {
                _state.value = OutfitState.Error("Konum/Hava Durumu hatası: ${e.message}")
            }
        }
    }
}

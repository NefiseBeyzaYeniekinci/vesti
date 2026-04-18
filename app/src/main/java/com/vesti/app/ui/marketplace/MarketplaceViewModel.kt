package com.vesti.app.ui.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vesti.app.data.network.MarketplaceApi
import com.vesti.app.data.network.MarketplaceItemDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MarketplaceState {
    object Loading : MarketplaceState()
    data class Success(val items: List<MarketplaceItemDto>) : MarketplaceState()
    data class Error(val message: String) : MarketplaceState()
}

class MarketplaceViewModel(private val api: MarketplaceApi) : ViewModel() {

    private val _state = MutableStateFlow<MarketplaceState>(MarketplaceState.Loading)
    val state: StateFlow<MarketplaceState> = _state.asStateFlow()

    init {
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _state.value = MarketplaceState.Loading
            try {
                val response = api.getFeedItems()
                if (response.isSuccessful && response.body() != null) {
                    _state.value = MarketplaceState.Success(response.body()!!)
                } else {
                    _state.value = MarketplaceState.Error("Failed to load feed: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.value = MarketplaceState.Error("Network error: ${e.message}")
            }
        }
    }
}

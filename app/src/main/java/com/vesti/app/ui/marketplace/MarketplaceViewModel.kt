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

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    init {
        loadFeed()
    }

    fun loadFeed() {
        loadFavorites()
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

    fun loadFavorites() {
        viewModelScope.launch {
            try {
                val response = api.getFavorites()
                if (response.isSuccessful && response.body() != null) {
                    val ids = response.body()!!.data.map { it.listingId }.toSet()
                    _favoriteIds.value = ids
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun toggleFavorite(listingId: String) {
        viewModelScope.launch {
            try {
                val response = api.toggleFavorite(com.vesti.app.data.network.ToggleFavoriteRequest(listingId))
                if (response.isSuccessful && response.body() != null) {
                    val current = _favoriteIds.value.toMutableSet()
                    if (response.body()!!.action == "added") {
                        current.add(listingId)
                    } else {
                        current.remove(listingId)
                    }
                    _favoriteIds.value = current
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun createListing(title: String, price: String, condition: String, imageUri: android.net.Uri?, existingImageUrl: String? = null) {
        viewModelScope.launch {
            try {
                val imgPath = existingImageUrl ?: imageUri?.toString() ?: "/dummy.jpg"
                val request = com.vesti.app.data.network.CreateMarketplaceItemRequest(
                    title = title,
                    description = "Yeni ilan",
                    price = price.toDoubleOrNull() ?: 0.0,
                    imageUrl = imgPath,
                    category = "Uncategorized",
                    size = "M",
                    condition = condition
                )
                val response = api.createItem(request)
                if (response.isSuccessful) {
                    loadFeed()
                }
            } catch (e: Exception) {
                // Ignore for now
            }
        }
    }
}

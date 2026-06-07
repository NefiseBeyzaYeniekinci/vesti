package com.vesti.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vesti.app.data.network.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileLoadState {
    object Loading : ProfileLoadState()
    data class Success(val profile: UserProfileDto) : ProfileLoadState()
    data class Error(val message: String) : ProfileLoadState()
}

sealed class OrdersLoadState {
    object Loading : OrdersLoadState()
    data class Success(val orders: List<OrderDto>) : OrdersLoadState()
    data class Error(val message: String) : OrdersLoadState()
}

sealed class SalesLoadState {
    object Loading : SalesLoadState()
    data class Success(val sales: List<OrderDto>) : SalesLoadState()
    data class Error(val message: String) : SalesLoadState()
}

sealed class PromotionsLoadState {
    object Loading : PromotionsLoadState()
    data class Success(val promotions: List<RedeemedPromoDto>) : PromotionsLoadState()
    data class Error(val message: String) : PromotionsLoadState()
}

sealed class StyleProfileLoadState {
    object Loading : StyleProfileLoadState()
    data class Success(val styleProfile: StyleProfileDto) : StyleProfileLoadState()
    data class Error(val message: String) : StyleProfileLoadState()
}

class ProfileViewModel(private val api: UserApi) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileLoadState>(ProfileLoadState.Loading)
    val profileState: StateFlow<ProfileLoadState> = _profileState.asStateFlow()

    private val _styleProfileState = MutableStateFlow<StyleProfileLoadState>(StyleProfileLoadState.Loading)
    val styleProfileState: StateFlow<StyleProfileLoadState> = _styleProfileState.asStateFlow()

    private val _ordersState = MutableStateFlow<OrdersLoadState>(OrdersLoadState.Loading)
    val ordersState: StateFlow<OrdersLoadState> = _ordersState.asStateFlow()

    private val _salesState = MutableStateFlow<SalesLoadState>(SalesLoadState.Loading)
    val salesState: StateFlow<SalesLoadState> = _salesState.asStateFlow()

    private val _promotionsState = MutableStateFlow<PromotionsLoadState>(PromotionsLoadState.Loading)
    val promotionsState: StateFlow<PromotionsLoadState> = _promotionsState.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _isSavingStyleProfile = MutableStateFlow(false)
    val isSavingStyleProfile: StateFlow<Boolean> = _isSavingStyleProfile.asStateFlow()

    private val _updateResult = MutableStateFlow<String?>(null)
    val updateResult: StateFlow<String?> = _updateResult.asStateFlow()

    private val _styleSaveResult = MutableStateFlow<String?>(null)
    val styleSaveResult: StateFlow<String?> = _styleSaveResult.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        loadProfile()
        loadStyleProfile()
        loadOrders()
        loadSales()
        loadPromotions()
    }

    fun clearUpdateResult() {
        _updateResult.value = null
        _styleSaveResult.value = null
    }

    fun loadStyleProfile() {
        viewModelScope.launch {
            _styleProfileState.value = StyleProfileLoadState.Loading
            try {
                val response = api.getStyleProfile()
                if (response.isSuccessful) {
                    val profile = response.body() ?: StyleProfileDto()
                    _styleProfileState.value = StyleProfileLoadState.Success(profile)
                } else {
                    _styleProfileState.value = StyleProfileLoadState.Error(response.errorBody()?.string() ?: "Hata oluştu")
                }
            } catch (e: Exception) {
                _styleProfileState.value = StyleProfileLoadState.Error(e.message ?: "Ağ hatası")
            }
        }
    }

    fun saveStyleProfile(profile: StyleProfileDto) {
        viewModelScope.launch {
            _isSavingStyleProfile.value = true
            _styleSaveResult.value = null
            try {
                val response = api.updateStyleProfile(profile)
                if (response.isSuccessful) {
                    _styleSaveResult.value = "SUCCESS"
                    _styleProfileState.value = StyleProfileLoadState.Success(profile)
                } else {
                    _styleSaveResult.value = response.errorBody()?.string() ?: "Tarz profili kaydedilemedi"
                }
            } catch (e: Exception) {
                _styleSaveResult.value = e.message ?: "Ağ hatası"
            } finally {
                _isSavingStyleProfile.value = false
            }
        }
    }

    fun clearStyleSaveResult() {
        _styleSaveResult.value = null
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileLoadState.Loading
            try {
                val response = api.getProfile()
                if (response.isSuccessful && response.body() != null) {
                    _profileState.value = ProfileLoadState.Success(response.body()!!.user)
                } else {
                    _profileState.value = ProfileLoadState.Error(response.errorBody()?.string() ?: "Hata oluştu")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileLoadState.Error(e.message ?: "Ağ hatası")
            }
        }
    }

    fun loadOrders() {
        viewModelScope.launch {
            _ordersState.value = OrdersLoadState.Loading
            try {
                val response = api.getOrders("buyer")
                if (response.isSuccessful && response.body() != null) {
                    _ordersState.value = OrdersLoadState.Success(response.body()!!)
                } else {
                    _ordersState.value = OrdersLoadState.Error(response.errorBody()?.string() ?: "Hata oluştu")
                }
            } catch (e: Exception) {
                _ordersState.value = OrdersLoadState.Error(e.message ?: "Ağ hatası")
            }
        }
    }

    fun loadSales() {
        viewModelScope.launch {
            _salesState.value = SalesLoadState.Loading
            try {
                val response = api.getOrders("seller")
                if (response.isSuccessful && response.body() != null) {
                    _salesState.value = SalesLoadState.Success(response.body()!!)
                } else {
                    _salesState.value = SalesLoadState.Error(response.errorBody()?.string() ?: "Hata oluştu")
                }
            } catch (e: Exception) {
                _salesState.value = SalesLoadState.Error(e.message ?: "Ağ hatası")
            }
        }
    }

    fun loadPromotions() {
        viewModelScope.launch {
            _promotionsState.value = PromotionsLoadState.Loading
            try {
                val response = api.getPromotions()
                if (response.isSuccessful && response.body() != null) {
                    _promotionsState.value = PromotionsLoadState.Success(response.body()!!)
                } else {
                    _promotionsState.value = PromotionsLoadState.Error(response.errorBody()?.string() ?: "Hata oluştu")
                }
            } catch (e: Exception) {
                _promotionsState.value = PromotionsLoadState.Error(e.message ?: "Ağ hatası")
            }
        }
    }

    fun updateProfile(request: ProfileUpdateRequest) {
        viewModelScope.launch {
            _isUpdating.value = true
            try {
                val response = api.updateProfile(request)
                if (response.isSuccessful) {
                    _updateResult.value = "SUCCESS"
                    loadProfile()
                } else {
                    _updateResult.value = response.errorBody()?.string() ?: "Profil güncellenemedi"
                }
            } catch (e: Exception) {
                _updateResult.value = e.message ?: "Ağ hatası"
            } finally {
                _isUpdating.value = false
            }
        }
    }

    fun updateCity(cityCode: String) {
        viewModelScope.launch {
            try {
                api.updateCity(CityRequest(cityCode))
            } catch (e: Exception) {
                // Hata yoksayılabilir
            }
        }
    }

    fun addCard(cardName: String, cardNumber: String, expiryDate: String, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.addCard(CardRequest(cardName, cardNumber, expiryDate))
                if (response.isSuccessful) {
                    loadProfile()
                    onDone(true, "Kart başarıyla eklendi")
                } else {
                    onDone(false, response.errorBody()?.string() ?: "Kart eklenemedi")
                }
            } catch (e: Exception) {
                onDone(false, e.message ?: "Ağ hatası")
            }
        }
    }

    fun deleteCard(cardId: String) {
        viewModelScope.launch {
            try {
                val response = api.deleteCard(cardId)
                if (response.isSuccessful) {
                    loadProfile()
                }
            } catch (e: Exception) {
                // Hata yoksayılabilir
            }
        }
    }

    fun updateTracking(orderId: String, trackingNumber: String, trackingCarrier: String, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.updateTracking(orderId, TrackingRequest(trackingNumber, trackingCarrier))
                if (response.isSuccessful) {
                    loadSales()
                    onDone(true, "Kargo bilgisi kaydedildi")
                } else {
                    onDone(false, response.errorBody()?.string() ?: "Kargo kaydedilemedi")
                }
            } catch (e: Exception) {
                onDone(false, e.message ?: "Ağ hatası")
            }
        }
    }

    fun redeemPromo(code: String, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.redeemPromotion(RedeemRequest(code))
                if (response.isSuccessful && response.body() != null) {
                    loadPromotions()
                    onDone(true, response.body()!!.message)
                } else {
                    onDone(false, response.errorBody()?.string() ?: "Kod uygulanamadı")
                }
            } catch (e: Exception) {
                onDone(false, e.message ?: "Ağ hatası")
            }
        }
    }
}

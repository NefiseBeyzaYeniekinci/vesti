package com.vesti.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vesti.app.data.network.NotificationApi
import com.vesti.app.data.network.NotificationDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class NotificationState {
    object Loading : NotificationState()
    data class Success(val notifications: List<NotificationDto>) : NotificationState()
    data class Error(val message: String) : NotificationState()
}

class NotificationViewModel(private val api: NotificationApi) : ViewModel() {

    private val _state = MutableStateFlow<NotificationState>(NotificationState.Loading)
    val state: StateFlow<NotificationState> = _state.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _state.value = NotificationState.Loading
            try {
                val response = api.getNotifications()
                if (response.isSuccessful && response.body() != null) {
                    _state.value = NotificationState.Success(response.body()!!)
                } else {
                    _state.value = NotificationState.Error(response.errorBody()?.string() ?: "Hata oluştu")
                }
            } catch (e: Exception) {
                _state.value = NotificationState.Error(e.message ?: "Ağ hatası")
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                val response = api.markAllAsRead()
                if (response.isSuccessful) {
                    loadNotifications()
                }
            } catch (e: Exception) {
                // Hata durumunu yoksayabiliriz
            }
        }
    }
}

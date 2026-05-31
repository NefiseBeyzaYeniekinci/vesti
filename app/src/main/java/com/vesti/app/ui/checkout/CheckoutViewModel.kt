package com.vesti.app.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vesti.app.data.network.CheckoutRequest
import com.vesti.app.data.network.CheckoutResponse
import com.vesti.app.data.network.PaymentApi
import com.vesti.app.data.network.PaymentMethodDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CheckoutState {
    object Idle : CheckoutState()
    object Processing : CheckoutState()
    data class Success(val response: CheckoutResponse) : CheckoutState()
    data class Error(val message: String) : CheckoutState()
}

class CheckoutViewModel(private val api: PaymentApi) : ViewModel() {

    private val _state = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val state: StateFlow<CheckoutState> = _state.asStateFlow()

    fun processPayment(
        itemId: String,
        amount: Double,
        cardNumber: String,
        expiryDate: String,
        cvv: String,
        cardholderName: String
    ) {
        viewModelScope.launch {
            _state.value = CheckoutState.Processing
            try {
                val request = CheckoutRequest(
                    itemId = itemId,
                    amount = amount,
                    paymentMethod = PaymentMethodDto(
                        cardNumber = cardNumber,
                        expiryDate = expiryDate,
                        cvv = cvv,
                        cardholderName = cardholderName
                    )
                )
                
                val response = api.processPayment(request)
                if (response.isSuccessful && response.body() != null) {
                    val checkoutResponse = response.body()!!
                    if (checkoutResponse.success) {
                        _state.value = CheckoutState.Success(checkoutResponse)
                    } else {
                        val errMsg = checkoutResponse.error ?: if (com.vesti.app.AppConfig.language == "en") "Payment rejected." else "Ödeme reddedildi."
                        _state.value = CheckoutState.Error(errMsg)
                    }
                } else {
                    val prefix = if (com.vesti.app.AppConfig.language == "en") "Transaction failed: " else "İşlem başarısız: "
                    _state.value = CheckoutState.Error(prefix + response.code())
                }
            } catch (e: Exception) {
                val prefix = if (com.vesti.app.AppConfig.language == "en") "Network error: " else "Ağ hatası: "
                _state.value = CheckoutState.Error(prefix + e.message)
            }
        }
    }

    fun resetState() {
        _state.value = CheckoutState.Idle
    }
}

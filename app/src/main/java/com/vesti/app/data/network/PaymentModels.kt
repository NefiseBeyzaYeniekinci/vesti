package com.vesti.app.data.network

data class CheckoutRequest(
    val itemId: String,
    val amount: Double,
    val currency: String = "TRY",
    val paymentMethod: PaymentMethodDto
)

data class PaymentMethodDto(
    val cardNumber: String,
    val expiryDate: String,
    val cvv: String,
    val cardholderName: String
)

data class CheckoutResponse(
    val success: Boolean,
    val transactionId: String?,
    val message: String?,
    val receiptUrl: String?,
    val error: String?
)

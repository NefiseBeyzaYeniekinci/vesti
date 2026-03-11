package com.vesti.app.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PaymentApi {
    @POST("api/payment/checkout")
    suspend fun processPayment(@Body request: CheckoutRequest): Response<CheckoutResponse>
}

package com.marketfinance.app.ui.fragments.core.orders

data class UserOrderData(
    val symbol: String,
    val quantity: Double,
    val quoteType: String,
    val orderType: Int,
    val orderPrice: Double,
    val statusCode: Int,
    val transactionTime: Int,
    val expirationTime: Int?
)

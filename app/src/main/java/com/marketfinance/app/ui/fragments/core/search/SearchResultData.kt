package com.marketfinance.app.ui.fragments.core.search

data class SearchResultData(
    // If symbol is null, quoteType-chartData will all be empty or 0.00 values.
    val symbol: String?,
    val quoteType: String,
    val name: String,
    val currentPrice: Double,
    val change: Double,
    val percentage: Double,
    val errorMessage: String?
)

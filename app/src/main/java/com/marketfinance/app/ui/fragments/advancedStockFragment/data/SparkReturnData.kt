package com.marketfinance.app.ui.fragments.advancedStockFragment.data

data class SparkReturnData(
    val currentMarketPrice: Double,
    val previousClosePrice: Double,
    val timestamps: List<Int>,
    val chartData: List<Double>,
    val responseCode: Int
)

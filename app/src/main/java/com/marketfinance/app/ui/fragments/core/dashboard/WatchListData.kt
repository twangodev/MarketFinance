package com.marketfinance.app.ui.fragments.core.dashboard

data class WatchListData(
    val symbol: String,
    val name: String,
    val quoteType: String,
    val lastPrice: Double,
    val lastPreviousClosePrice: Double,
)


package com.marketfinance.app.utils.network.responses.historical

data class HistoricalData(
    val meta: Meta?,
    val timestamp: List<Long?>?,
    val indicators: Indicators?
)

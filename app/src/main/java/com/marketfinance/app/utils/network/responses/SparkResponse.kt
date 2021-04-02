package com.marketfinance.app.utils.network.responses

import com.marketfinance.app.utils.network.responses.historical.HistoricalData

data class SparkResponse(
    val symbol: String?,
    val historicalData: HistoricalData?
)

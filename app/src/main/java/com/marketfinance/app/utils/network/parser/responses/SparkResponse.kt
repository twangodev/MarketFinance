package com.marketfinance.app.utils.network.parser.responses

import com.marketfinance.app.utils.network.parser.responses.historical.HistoricalData

data class SparkResponse(
    val symbol: String?,
    val historicalData: HistoricalData?
)

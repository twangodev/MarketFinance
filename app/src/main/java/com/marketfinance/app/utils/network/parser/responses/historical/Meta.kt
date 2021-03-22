package com.marketfinance.app.utils.network.parser.responses.historical

data class Meta(
    val currency: String?,
    val symbol: String?,
    val exchangeName: String?,
    val instrumentType: String?,
    val firstTradeDate: Long?,
    val regularMarketTime: Long?,
    val gmtOffset: Long?,
    val ZoneID: String?,
    val exchangeTimezoneName: String?,
    val regularMarketPrice: Double?,
    val chartPreviousClose: Double?,
    val priceHint: Int?,
    val currentTradingPeriod: CurrentTradingPeriod?,
    val dataGranularity: String?,
    val range: String?,
    val validRanges: List<String?>?
)

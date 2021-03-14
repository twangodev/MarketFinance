package com.marketfinance.app.utils.network.parser.responses.historical

data class TradingPeriod(
    val timezone: String?,
    val start: Long?,
    val end: Long?,
    val gmtOffset: Long?
)

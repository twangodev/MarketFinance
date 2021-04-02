package com.marketfinance.app.utils.network.responses.historical

data class CurrentTradingPeriod(
    val pre: TradingPeriod?,
    val regular: TradingPeriod?,
    val post: TradingPeriod?
)

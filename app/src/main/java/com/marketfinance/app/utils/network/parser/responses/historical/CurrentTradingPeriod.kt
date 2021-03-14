package com.marketfinance.app.utils.network.parser.responses.historical

data class CurrentTradingPeriod(
    val pre: TradingPeriod?,
    val regular: TradingPeriod?,
    val post: TradingPeriod?
)

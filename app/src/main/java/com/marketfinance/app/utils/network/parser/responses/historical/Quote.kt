package com.marketfinance.app.utils.network.parser.responses.historical

data class Quote(
    val open: List<Double?>?,
    val close: List<Double?>?,
    val low: List<Double?>?,
    val high: List<Double?>?,
    val volume: List<Long?>?
)

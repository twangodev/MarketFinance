package com.marketfinance.app.utils.network.parser.responses

data class Trend(
    val period: String?,
    val strongBuy: Int?,
    val buy: Int?,
    val hold: Int?,
    val sell: Int?,
    val strongSell: Int?
)
package com.marketfinance.app.utils.network.parser.responses

data class EarningsHistory(
    val history: List<History?>,
    val maxAge: Long?
)

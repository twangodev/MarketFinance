package com.marketfinance.app.utils.network.responses

data class EarningsHistory(
    val history: List<History?>,
    val maxAge: Long?
)

package com.marketfinance.app.utils.network.responses

data class RecommendationTrend(
    val trend: List<Trend>,
    val maxAge: Long?
)

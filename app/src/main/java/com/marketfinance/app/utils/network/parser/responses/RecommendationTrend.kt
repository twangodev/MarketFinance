package com.marketfinance.app.utils.network.parser.responses

data class RecommendationTrend(
    val trend: List<Trend>,
    val maxAge: Long?
)

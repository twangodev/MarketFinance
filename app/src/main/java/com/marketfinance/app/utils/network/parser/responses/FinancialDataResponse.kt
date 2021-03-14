package com.marketfinance.app.utils.network.parser.responses

import com.marketfinance.app.utils.network.parser.RawFMT
import com.marketfinance.app.utils.network.parser.RawLongFMT

data class FinancialDataResponse(
    val maxAge: Long?,
    val currentPrice: RawFMT?,
    val targetHighPrice: RawFMT?,
    val targetLowPrice: RawFMT?,
    val targetMeanPrice: RawFMT?,
    val targetMedianPrice: RawFMT?,
    val recommendationMeanPrice: RawFMT?,
    val recommendationKey: String?,
    val numberOfAnalystOpinions: RawLongFMT?,
    val totalCash: RawLongFMT?,
    val totalCashPerShare: RawFMT?,
    val ebitda: RawLongFMT?,
    val totalDebt: RawLongFMT?,
    val quickRatio: RawFMT?,
    val currentRatio: RawFMT?,
    val totalRevenue: RawLongFMT?,
    val debtToEquity: RawFMT?,
    val revenuePerShare: RawFMT?,
    val returnOnAssets: RawFMT?,
    val returnOnEquity: RawFMT?,
    val grossProfits: RawLongFMT?,
    val freeCashFlow: RawLongFMT?,
    val operatingCashFlow: RawLongFMT?,
    val earningsGrowth: RawFMT?,
    val revenueGrowth: RawFMT?,
    val grossMargins: RawFMT?,
    val ebitdaMargins: RawFMT?,
    val operatingMargins: RawFMT?,
    val profitMargins: RawFMT?,
    val financialCurrency: String?
)

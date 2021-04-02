package com.marketfinance.app.utils.network.responses

import com.marketfinance.app.utils.network.parser.RawFMT

data class History(
    val maxAge: Long?,
    val epsActual: RawFMT?,
    val epsEstimate: RawFMT?,
    val epsDifference: RawFMT?,
    val surprisePercent: RawFMT?,
    val quarter: RawFMT?,
    val period: String?
)

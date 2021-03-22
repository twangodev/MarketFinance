package com.marketfinance.app.utils.network.parser.responses

data class SearchDataItems(
    val symbol: String?,
    val name: String?,
    val exch: String?,
    val type: String?,
    val exchDisp: String?,
    val typeDisp: String?
)

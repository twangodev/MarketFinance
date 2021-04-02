package com.marketfinance.app.utils.network.responses

data class SearchDataResponse(
    val suggestionTitleAccessor: String?,
    val suggestionMeta: List<String?>?,
    val hiConf: Boolean?,
    val items: List<SearchDataItems?>
)

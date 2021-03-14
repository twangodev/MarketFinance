package com.marketfinance.app.utils.network

import com.marketfinance.app.ui.fragments.advancedStockFragment.data.RangeIntervalData

interface URLRetriever {

    /**
     * Retrieves financial data URL from Yahoo Finance
     * @param symbol Symbol for financial data
     * @return URL
     */
    fun getFinanceURL(symbol: String) =
        "https://query1.finance.yahoo.com/v10/finance/quoteSummary/$symbol?modules=financialData"

    /**
     * Retrieves spark data URL from Yahoo Finance
     * @param symbol Symbol for spark data
     * @param rangeInterval Range and interval for spark data
     * @return URL
     */
    fun getSparkURL(symbol: String, rangeInterval: RangeIntervalData) =
        "https://query1.finance.yahoo.com/v7/finance/spark?symbols=$symbol&range=${rangeInterval.range}&interval=${rangeInterval.interval}&includePrePost=true"

    /**
     * Retrieves historical data URL from Yahoo Finance
     * @param symbol Symbol for historical data
     * @param rangeInterval Range and interval for historical data
     * @return URL
     */
    fun getHistoricalURL(symbol: String, rangeInterval: RangeIntervalData) =
        "https://query1.finance.yahoo.com/v8/finance/chart/$symbol?range=${rangeInterval.range}&interval=${rangeInterval.interval}&includePrePost=true"

    /**
     * Retrieves asset profile, earnings history, and recommendation trend
     * @param symbol Symbol for asset profile, earnings history, and recommendation trend
     * @return URL
     */
    fun getQuoteSummaryURL(symbol: String) =
        "https://query1.finance.yahoo.com/v10/finance/quoteSummary/$symbol?modules=assetProfile,earningsHistory,recommendationTrend"


    /**
     * Retrieves news data using an RSS to JSON API
     *
     * @param symbol Symbol for news
     * @return URL
     */
    fun getSymbolNewsData(symbol: String) =
        "https://api.rss2json.com/v1/api.json?rss_url=http://feeds.finance.yahoo.com/rss/2.0/headline?s=$symbol&region=US&lang=en-US"

    fun getOptionsURL(symbol: String) =
        "https://query1.finance.yahoo.com/v7/finance/options/$symbol"

}
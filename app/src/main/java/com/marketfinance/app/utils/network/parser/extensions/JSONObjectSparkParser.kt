package com.marketfinance.app.utils.network.parser.extensions

import com.marketfinance.app.utils.network.JSONArrayWrapper.Companion.toList
import com.marketfinance.app.utils.network.JSONObjectWrapper.Companion.first
import com.marketfinance.app.utils.network.JSONObjectWrapper.Companion.getElement
import com.marketfinance.app.utils.network.parser.responses.SparkResponse
import com.marketfinance.app.utils.network.parser.responses.historical.HistoricalData
import com.marketfinance.app.utils.network.parser.responses.historical.Indicators
import com.marketfinance.app.utils.network.parser.responses.historical.Meta
import com.marketfinance.app.utils.network.parser.responses.historical.Quote
import org.json.JSONArray
import org.json.JSONObject

/**
 * Required for successful compilation. Android will reach a limit in the normal [JSONObjectParser] interface
 */
interface JSONObjectSparkParser {

    fun JSONObject.extensionParseSparkResponse(): SparkResponse? {
        getElement<JSONObject>("spark")?.first<JSONObject>("result").apply {
            return if (this != null) {
                val historicalData: HistoricalData?
                first<JSONObject>("response").apply {
                    if (this != null) {
                        val meta: Meta?
                        getElement<JSONObject>("meta").apply {
                            meta = if (this != null) {
                                Meta(
                                    getElement("currency"),
                                    getElement("symbol"),
                                    getElement("exchangeName"),
                                    getElement("instrumentType"),
                                    getElement("firstTradeDate"),
                                    getElement("regularMarketTime"),
                                    getElement("gmtoffset"),
                                    getElement("timezone"),
                                    getElement("exchangeTimezoneName"),
                                    getElement("regularMarketPrice"),
                                    getElement("chartPreviousClose"),
                                    getElement("priceHint"),
                                    getElement("currentTradingPeriod"),
                                    getElement("dataGranularity"),
                                    getElement("range"),
                                    getElement<JSONArray>("validRanges")?.toList()
                                )
                            } else {
                                null
                            }

                        }

                        val indicators: Indicators?
                        getElement<JSONObject>("indicators").apply {
                            indicators = if (this != null) {
                                val quote: Quote?
                                first<JSONObject>("quote").apply {
                                    quote = if (this != null) {
                                        Quote(null, getElement<JSONArray>("close")?.toList(), null, null, null)
                                    } else {
                                        null
                                    }
                                }

                                Indicators(quote, null)
                            } else {
                                null
                            }
                        }

                        historicalData = HistoricalData(
                            meta,
                            getElement<JSONArray>("timestamp")?.toList(),
                            indicators
                        )
                    } else {
                        historicalData = null
                    }
                }

                SparkResponse(
                    getElement("symbol"),
                    historicalData
                )
            } else {
                null
            }
        }
    }

}
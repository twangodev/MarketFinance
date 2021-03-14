package com.marketfinance.app.utils.network.parser

import com.marketfinance.app.utils.network.JSONArrayWrapper.Companion.toList
import com.marketfinance.app.utils.network.JSONObjectWrapper.Companion.first
import com.marketfinance.app.utils.network.JSONObjectWrapper.Companion.getElement
import com.marketfinance.app.utils.network.parser.responses.*
import com.marketfinance.app.utils.network.parser.responses.historical.*
import org.json.JSONArray
import org.json.JSONObject

interface JSONObjectParser {

    /**
     * Parses [JSONObject] as [FinancialDataResponse]
     *
     * @return [FinancialDataResponse]
     */
    fun JSONObject.parseFinanceResponse(): FinancialDataResponse? {
        getElement<JSONObject>("quoteSummary")?.first<JSONObject>("result")?.getElement<JSONObject>("financialData").apply {
            return if (this != null) {
                FinancialDataResponse(
                    getElement("maxAge"),
                    getElement("currentPrice"),
                    getElement("targetHighPrice"),
                    getElement("targetLowPrice"),
                    getElement("targetMeanPrice"),
                    getElement("targetMedianPrice"),
                    getElement("recommendationMean"),
                    getElement("recommendationKey"),
                    getElement("numberOfAnalystOpinions"),
                    getElement("totalCash"),
                    getElement("totalCashPerShare"),
                    getElement("ebitda"),
                    getElement("totalDebt"),
                    getElement("quickRatio"),
                    getElement("currentRatio"),
                    getElement("totalRevenue"),
                    getElement("debtToEquity"),
                    getElement("revenuePerShare"),
                    getElement("returnOnAssets"),
                    getElement("returnOnEquity"),
                    getElement("grossProfits"),
                    getElement("freeCashflow"),
                    getElement("operatingCashflow"),
                    getElement("earningsGrowth"),
                    getElement("revenueGrowth"),
                    getElement("grossMargins"),
                    getElement("ebitdaMargins"),
                    getElement("operatingMargins"),
                    getElement("profitMargins"),
                    getElement("financialCurrency")
                )
            } else {
                null
            }
        }
    }

    /**
     * Parses [JSONObject] as [AssetProfile]
     *
     * @return [AssetProfile]
     */
    fun JSONObject.parseAssetProfile(): AssetProfile? {
        getElement<JSONObject>("quoteSummary")?.first<JSONObject>("result")?.getElement<JSONObject>("assetProfile").apply {
            return if (this != null) {
                val companyOfficersList = mutableListOf<CompanyOfficer>()
                getElement<JSONArray>("companyOfficers")?.toList<JSONObject>()?.forEach { officer ->
                    officer?.apply {
                        companyOfficersList.add(
                            CompanyOfficer(
                                getElement("maxAge"),
                                getElement("name"),
                                getElement("age"),
                                getElement("title"),
                                getElement("yearBorn"),
                                getElement("fiscalYear"),
                                getElement("totalPay"),
                                getElement("exercisedValue"),
                                getElement("unexercisedValue")
                            )
                        )
                    }

                }

                AssetProfile(
                    getElement("city"),
                    getElement("state"),
                    getElement("phone"),
                    getElement("website"),
                    getElement("industry"),
                    getElement("sector"),
                    getElement("longBusinessSummary"),
                    getElement("fullTimeEmployees"),
                    companyOfficersList,
                    getElement("auditRisk"),
                    getElement("boardRisk"),
                    getElement("compensationRisk"),
                    getElement("shareHolderRightsRisk"),
                    getElement("overallRisk"),
                    getElement("governanceEpochDate"),
                    getElement("compensationAsOfEpochDate"),
                    getElement("maxAge")
                )
            } else {
                null
            }
        }
    }

    /**
     * Parses [JSONObject] as [RecommendationTrend]
     *
     * @return [RecommendationTrend]
     */
    fun JSONObject.parseRecommendationTrend(): RecommendationTrend? {
        getElement<JSONObject>("quoteSummary")?.first<JSONObject>("result")?.getElement<JSONObject>("recommendationTrend").apply {
            return if (this != null) {
                val trendList = mutableListOf<Trend>()
                getElement<JSONArray>("trend")?.toList<JSONObject>()?.forEach { trend ->
                    trendList.add(
                        Trend(
                            trend?.getElement("period"),
                            trend?.getElement("strongBuy"),
                            trend?.getElement("buy"),
                            trend?.getElement("hold"),
                            trend?.getElement("sell"),
                            trend?.getElement("strongSell")
                        )
                    )
                }
                RecommendationTrend(
                    trendList,
                    getElement("maxAge")
                )
            } else {
                null
            }
        }
    }

    /**
     * Parses [JSONObject] as [EarningsHistory]
     *
     * @return [EarningsHistory]
     */
    fun JSONObject.parseEarningsHistory(): EarningsHistory? {
        getElement<JSONObject>("quoteSummary")?.first<JSONObject>("result")?.getElement<JSONObject>("earningsHistory").apply {
            return if (this != null) {
                val historyList = mutableListOf<History>()
                getElement<JSONArray>("history")?.toList<JSONObject>()?.forEach { history ->
                    history?.apply {
                        historyList.add(
                            History(
                                getElement("maxAge"),
                                getElement("epsActual"),
                                getElement("epsEstimate"),
                                getElement("epsDifference"),
                                getElement("surprisePercent"),
                                getElement("quarter"),
                                getElement("period")
                            )
                        )
                    }

                }
                EarningsHistory(
                    historyList,
                    getElement("maxAge")
                )
            } else {
                null
            }
        }
    }


    fun JSONObject.parseHistoricalData(): HistoricalData? {
        getElement<JSONObject>("chart")?.first<JSONObject>("result").apply {
            return if (this != null) {

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
                                Quote(
                                    getElement<JSONArray>("open")?.toList(),
                                    getElement<JSONArray>("close")?.toList(),
                                    getElement<JSONArray>("low")?.toList(),
                                    getElement<JSONArray>("high")?.toList(),
                                    getElement<JSONArray>("volume")?.toList(),
                                )
                            } else {
                                null
                            }
                        }
                        val adjclose: Adjclose // todo not working apparently
                        first<JSONObject>("adjclose").apply {
                            adjclose = Adjclose(getElement<JSONArray>("adjclose")?.toList())
                        }
                        Indicators(quote, adjclose)
                    } else {
                        null
                    }
                }

                HistoricalData(
                    meta,
                    getElement<JSONArray>("timestamp")?.toList(),
                    indicators
                )
            } else {
                null
            }
        }
    }

}
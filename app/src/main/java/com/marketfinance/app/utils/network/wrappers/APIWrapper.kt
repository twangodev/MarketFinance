package com.marketfinance.app.utils.network.wrappers

import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.marketfinance.app.ui.fragments.advancedStockFragment.data.RangeIntervalData
import com.marketfinance.app.utils.network.URLRetriever
import org.json.JSONObject

/**
 * Simple wrapper for Yahoo Finance's public APIs
 *
 * @param symbol Data for the [symbol]
 * @param rangeInterval The specified range of the data
 * @throws NullPointerException Thrown if [RangeIntervalData] is `null` for an API with a required range
 * @author James Ding
 *
 */
class APIWrapper(var symbol: String, var rangeInterval: RangeIntervalData?) : URLRetriever {

    private val TAG = "APIWrapper"

    /**
     * Gets financial data for [symbol]
     *
     * @param callback The callback for successful responses
     * @param errorCallback The callback for failed responses
     * @return The [JsonObjectRequest] for financial data with the specified [callback] and [errorCallback]
     * @author James Ding
     */
    fun finance(
        callback: Response.Listener<JSONObject>,
        errorCallback: Response.ErrorListener
    ) = JsonObjectRequest(
        Request.Method.GET,
        getFinanceURL(symbol),
        null,
        callback,
        errorCallback
    )

    /**
     * Gets spark data for [symbol] and with [rangeInterval]
     *
     * @throws NullPointerException RangeIntervalData cannot be null
     */
    fun spark(
        callback: Response.Listener<JSONObject>,
        errorCallback: Response.ErrorListener
    ) = JsonObjectRequest(
        Request.Method.GET,
        rangeInterval?.let { getSparkURL(symbol, it) }
            ?: throw NullPointerException("RangeInterval cannot be null"),
        null,
        callback,
        errorCallback
    )

    /**
     * Gets historical data for [symbol] and with [rangeInterval]
     *
     * @throws NullPointerException RangeIntervalData cannot be null
     */
    fun historical(
        callback: Response.Listener<JSONObject>,
        errorCallback: Response.ErrorListener
    ) = JsonObjectRequest(
        Request.Method.GET,
        rangeInterval?.let { getHistoricalURL(symbol, it) }
            ?: throw NullPointerException("RangeInterval cannot be null"),
        null,
        callback,
        errorCallback
    )

    fun getOneTimeData(
        callback: Response.Listener<JSONObject>,
        errorCallback: Response.ErrorListener
    ) = JsonObjectRequest(
        Request.Method.GET,
        getQuoteSummaryURL(symbol),
        null,
        callback,
        errorCallback
    )

    fun getSymbolNewsData(
        callback: Response.Listener<JSONObject>,
        errorCallback: Response.ErrorListener
    ) = JsonObjectRequest(
        Request.Method.GET,
        getSymbolNewsData(symbol),
        null,
        callback,
        errorCallback
    )

    fun getOptionsData(
        callback: Response.Listener<JSONObject>,
        errorCallback: Response.ErrorListener
    ) = JsonObjectRequest(
        Request.Method.GET,
        getOptionsURL(symbol),
        null,
        callback,
        errorCallback
    )

}
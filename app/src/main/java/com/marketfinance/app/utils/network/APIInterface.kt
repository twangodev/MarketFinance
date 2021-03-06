package com.marketfinance.app.utils.network

import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.marketfinance.app.ui.fragments.advancedStockFragment.data.RangeIntervalData
import org.json.JSONObject
import java.lang.NullPointerException

class APIInterface(var symbol: String, var rangeInterval: RangeIntervalData?): URLGetter {

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
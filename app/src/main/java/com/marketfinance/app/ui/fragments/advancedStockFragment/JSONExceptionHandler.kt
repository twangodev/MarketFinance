package com.marketfinance.app.ui.fragments.advancedStockFragment

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

object JSONExceptionHandler { // TODO convert to interface

    fun jsonObject(key: String): JSONObject? {
        Log.e("Volley", "[JSON OBJECT HANDLER] An error occurred while retrieving $key")
        return null
    }

    fun jsonArray(key: String): JSONArray? {
        Log.e("Volley", "[JSON ARRAY HANDLER] An error occurred while retrieving $key")
        return null
    }

    fun string(key: String): String? {
        Log.e("Volley", "[STRING HANDLER] An error occurred while retrieving $key")
        return null
    }

    fun double(key: String): Double? {
        Log.e("Volley", "[DOUBLE HANDLER] An error occurred while retrieving $key")
        return null
    }

    fun long(key: String): Long? {
        Log.e("Volley", "[LONG HANDLER] An error occurred while retrieving $key")
        return null
    }

}
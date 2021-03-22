package com.marketfinance.app.utils.network

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

/**
 * Code sourced from [https://developer.android.com/training/volley/requestqueue]
 * @author Android Development Team
 */
class RequestSingleton(val context: Context) {

    companion object {

        private const val TAG = "RequestSingleton"

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: RequestSingleton? = null
        fun getInstance(context: Context): RequestSingleton {
            Log.d(TAG, "[REQUEST] Getting Request Queue from ${context.packageName}")
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RequestSingleton(context).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val requestQueue: RequestQueue by lazy {
        // applicationContext is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        Volley.newRequestQueue(context.applicationContext)
    }

    fun <T> addToRequestQueue(request: Request<T>) {
        requestQueue.add(request)
    }


}
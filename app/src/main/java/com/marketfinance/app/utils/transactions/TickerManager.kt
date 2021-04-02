package com.marketfinance.app.utils.transactions

import android.content.Context
import com.google.gson.Gson
import com.marketfinance.app.ui.fragments.core.orders.UserOrderData
import com.marketfinance.app.utils.security.EncryptedPreference

interface TickerManager : PortfolioManager {

    private companion object {
        val gson = Gson()
    }

    fun createOrder(context: Context?, userOrderData: UserOrderData) {

        val updatedPortfolio = getActivePortfolio(context)




        if (context != null) {
            EncryptedPreference("portfolioData").getPreference(context).edit().apply {

                apply()
            }
        }


    }

}
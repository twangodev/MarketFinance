package com.marketfinance.app.utils.transactions

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.marketfinance.app.utils.security.EncryptedPreference
import com.marketfinance.app.utils.storage.PortfolioData

interface PortfolioManager {

    private companion object {
        val gson = Gson()
    }

    fun getPortfolioDataList(context: Context?): MutableList<PortfolioData>? {
        return gson.fromJson<MutableList<PortfolioData>>(
            context?.let {
                EncryptedPreference("portfolioData")
                    .getPreference(it)
                    .getString("gson", gson.toJson(mutableListOf<PortfolioData>()))
            },
            object : TypeToken<MutableList<PortfolioData>>() {}.type
        )
    }

    fun getActivePortfolio(context: Context?): PortfolioData? {
        return getPortfolioDataList(context)?.get(0)
    }

    fun updateActivePortfolio(context: Context?, portfolioData: PortfolioData) {
        val updatedData = getPortfolioDataList(context)
        updatedData?.set(0, portfolioData)

        if (context != null) {
            EncryptedPreference("portfolioData").getPreference(context).edit().apply {
                putString("gson", gson.toJson(updatedData))
                apply()
            }
        }
    }

}
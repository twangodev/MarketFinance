package com.marketfinance.app.utils.storage

import com.marketfinance.app.utils.security.Hashing
import com.marketfinance.app.ui.fragments.core.dashboard.UserWatchListData
import com.marketfinance.app.ui.fragments.core.orders.UserOrderData

/**
 * Parent Data Class
 * @param portfolioName Name of the portfolio
 * @param portfolioID Uses [Hashing] to generate a specific ID
 * @param initialBuyingPower User initialBuyingPower
 * @param currentPortfolioValue Live value of portfolio
 * @param userWatchList A [MutableList] of all shares user is watching
 * @param userOrders A [MutableList] of all user orders
 */
data class PortfolioData(
    val portfolioName: String,
    val portfolioID: String,
    val initialBuyingPower: Double,
    val currentPortfolioValue: Double,
    val userWatchList: MutableList<UserWatchListData>,
    val userOrders: MutableList<UserOrderData>
)

package com.marketfinance.app.ui.fragments.advancedStockFragment

import com.marketfinance.app.R

object AdvancedStockLayoutIDs {

    val timeSelectors = listOf(
        R.id.advancedStockFragment_timeRangeSelector_1D_textView,
        R.id.advancedStockFragment_timeRangeSelector_5D_textView,
        R.id.advancedStockFragment_timeRangeSelector_1M_textView,
        R.id.advancedStockFragment_timeRangeSelector_3M_textView,
        R.id.advancedStockFragment_timeRangeSelector_1Y_textView
    )

    val directTextColorChangeTickerViews = listOf(
        R.id.advancedStockFragment_floatingScrollView_price_tickerView,
        R.id.advancedStockFragment_floatingScrollView_change_tickerView,
        R.id.advancedStockFragment_price_tickerView,
        R.id.advancedStockFragment_change_tickerView,
        R.id.advancedStockFragment_quickStatistics_chartHigh_tickerView,
        R.id.advancedStockFragment_quickStatistics_chartLow_tickerView,
        R.id.advancedStockFragment_quickStatistics_chartPreviousClose_tickerView,
        R.id.advancedStockFragment_quickStatistics_volume_tickerView,
        R.id.advancedStockFragment_quickStatistics_peRatio_tickerView,
        R.id.advancedStockFragment_quickStatistics_dividend_tickerView,
        R.id.advancedStockFragment_quickStatistics_eps_tickerView,
        R.id.advancedStockFragment_quickStatistics_oneYearTargetEst_tickerView,
        R.id.advancedStockFragment_trade_statistics_units_tickerView,
        R.id.advancedStockFragment_trade_statistics_totalValue_tickerView,
        R.id.advancedStockFragment_trade_statistics_averageCost_tickerView,
        R.id.advancedStockFragment_trade_statistics_portfolioMakeup_tickerView,
        R.id.advancedStockFragment_trade_statistics_todayUnrealizedReturn_tickerView,
        R.id.advancedStockFragment_trade_statistics_todayRealizedReturn_tickerView,
        R.id.advancedStockFragment_trade_statistics_totalUnrealizedReturn_tickerView,
        R.id.advancedStockFragment_trade_statistics_totalRealizedReturn_tickerView,
    )

    val bidAskTickerViews = listOf(
        R.id.advancedStockFragment_quickStatistics_bid_tickerView,
        R.id.advancedStockFragment_quickStatistics_ask_tickerView
    )

    val timeRanges = mapOf(
        "1d" to R.id.advancedStockFragment_timeRangeSelector_1D_textView,
        "5d" to R.id.advancedStockFragment_timeRangeSelector_5D_textView,
        "1mo" to R.id.advancedStockFragment_timeRangeSelector_1M_textView,
        "3mo" to R.id.advancedStockFragment_timeRangeSelector_3M_textView,
        "1y" to R.id.advancedStockFragment_timeRangeSelector_1Y_textView
    )

    val analystRatingsTextViews = listOf(
        R.id.advancedStockFragment_analystRating_statistics_buy_textView,
        R.id.advancedStockFragment_analystRating_statistics_hold_textView,
        R.id.advancedStockFragment_analystRating_statistics_sell_textView
    )

    val analystRatingsProgressBars = listOf(
        R.id.advancedStockFragment_analystRating_statistics_buy_progressBar,
        R.id.advancedStockFragment_analystRating_statistics_hold_progressBar,
        R.id.advancedStockFragment_analystRating_statistics_sell_progressBar
    )

    val earningsDate = listOf(
        R.id.advancedStockFragment_earnings_dateOne_textView,
        R.id.advancedStockFragment_earnings_dateTwo_textView,
        R.id.advancedStockFragment_earnings_dateThree_textView,
        R.id.advancedStockFragment_earnings_dateFour_textView
    )

    val earningsChange = listOf(
        R.id.advancedStockFragment_earnings_changeOne_textView,
        R.id.advancedStockFragment_earnings_changeTwo_textView,
        R.id.advancedStockFragment_earnings_changeThree_textView,
        R.id.advancedStockFragment_earnings_changeFour_textView
    )

}
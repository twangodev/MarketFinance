package com.marketfinance.app.ui.fragments.advancedStockFragment

import com.marketfinance.app.ui.fragments.advancedStockFragment.data.RangeIntervalData

object ValidIntervals {

    /**
     * General is used to assign either a [Spark] or [CandleChart]
     */
    object General {
        const val ONE_DAY = 1
        const val FIVE_DAY = 2
        const val ONE_MONTH = 3
        const val THREE_MONTH = 4
        const val ONE_YEAR = 5
    }


    /**
     * Spark is used for massive amounts of chart data (Preferably for Line Charts)
     */
    object Spark {

        val ONE_DAY = RangeIntervalData("1d", "2m")

        val FIVE_DAY = RangeIntervalData("5d", "15m")

        val ONE_MONTH = RangeIntervalData("1mo", "30m")

        val THREE_MONTH = RangeIntervalData("3mo", "1h")

        val ONE_YEAR = RangeIntervalData("1y", "1d")

    }

    /**
     * Candle Charts should have a lot less data (preferably 30 sets)
     */
    object CandleChart {

        val ONE_DAY = RangeIntervalData("1d", "15m")

        val FIVE_DAY = RangeIntervalData("5d", "1h")

        val ONE_MONTH = RangeIntervalData("1mo", "90m")

        val THREE_MONTH = RangeIntervalData("3mo", "1d")

        val ONE_YEAR = RangeIntervalData("1y", "5d")

    }


}
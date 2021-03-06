package com.marketfinance.app.utils

import com.marketfinance.app.utils.objects.Defaults
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Calculate market related Values
 */
interface Calculations {

    /**
     * Calculate the difference between two values [currentMarketPrice] and [previousClosePrice]
     * @param currentMarketPrice Current Price to compare to
     * @param previousClosePrice Previous Price to compare
     */
    fun calculateChange(currentMarketPrice: Double, previousClosePrice: Double): Double {
        return BigDecimal(currentMarketPrice - previousClosePrice)
            .setScale(Defaults.roundLimit, RoundingMode.HALF_EVEN).toDouble()
    }

    fun calculatePercentage(change: Double, currentMarketPrice: Double): Double {
        return try {
            BigDecimal((change / (currentMarketPrice - change)) * 100)
                .setScale(Defaults.roundLimit, RoundingMode.HALF_EVEN).toDouble()
        } catch (error: NumberFormatException) {
            0.00
        }
    }

}
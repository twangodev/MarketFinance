package com.marketfinance.app.utils

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
     * @return [Double] or `null`. Return can be cast as non-nullable if [currentMarketPrice] and [previousClosePrice] are not `null`
     */
    fun calculateChange(currentMarketPrice: Double?, previousClosePrice: Double?): Double? {
        return if (currentMarketPrice != null && previousClosePrice != null) {
            BigDecimal(currentMarketPrice - previousClosePrice)
                .setScale(Defaults.roundLimit, RoundingMode.HALF_EVEN).toDouble()
        } else {
            null
        }
    }

    /**
     * Calculate percentage with [change] and [currentMarketPrice]
     *
     * @param change The change calculated using [calculateChange]
     * @param currentMarketPrice Current Price to compare to
     * @return [Double] or `null`. Return can be cast as non-nullable if [change] and [currentMarketPrice] are not `null`
     */
    fun calculatePercentage(change: Double?, currentMarketPrice: Double?): Double? {
        return if (change != null && currentMarketPrice != null) {
            try {
                BigDecimal((change / (currentMarketPrice - change)) * 100).setScale(Defaults.roundLimit, RoundingMode.HALF_EVEN).toDouble()
            } catch (error: NumberFormatException) {
                0.00
            }
        } else {
            null
        }
    }

}
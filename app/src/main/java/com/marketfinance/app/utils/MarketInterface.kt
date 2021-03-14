package com.marketfinance.app.utils

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.marketfinance.app.R
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.math.abs

/**
 * Interface for Market related functions
 * @author James Ding
 */
interface MarketInterface {

    /**
     * Initializes ticker. Should be called in onCreateView or equivalent
     *
     * @param ticker [TickerView] to initialize
     * @param initialText The initial text the [TickerView] should have
     * @param font Sets the [Typeface] for the [TickerView]
     * @param duration The animation duration
     * @author James Ding
     */
    fun initializeTicker(ticker: TickerView, initialText: String, font: Typeface, duration: Long) {
        ticker.typeface = font
        ticker.setCharacterLists(TickerUtils.provideNumberList())
        ticker.animationDuration = duration
        ticker.animationInterpolator = AccelerateDecelerateInterpolator()
        ticker.setPreferredScrollingDirection(TickerView.ScrollingDirection.ANY)
        ticker.text = initialText
    }

    /**
     * Formats decimal with US dollar with at least 2 decimal digits and a max of 3 decimal digits
     *
     * @param double Given [double] or to format
     * @return Formatted [Double] or N/A
     * @author James Ding
     */
    fun formatDoubleDollar(double: Double?): String = if (double != null) {
        "$" + DecimalFormat("0.00#").format(double)
    } else {
        "N/A"
    }

    /**
     * Formats decimal without US dollar with at least 2 decimal digits and a max of 3 decimal digits
     *
     * @param double Given [double] or to format
     * @return Formatted [Double] or N/A
     * @author James Ding
     */
    fun formatDouble(double: Double?): String = if (double != null) {
        DecimalFormat("0.00#").format(double)
    } else {
        "N/A"
    }

    /**
     * Appends corresponding abbreviation for large numbers
     *
     * @param long Given [long]
     * @return Formatted [Long]
     * @author James Ding
     */
    fun formatLargeNumber(long: Long?): String {
        val roundLimit = Defaults.roundLimit
        return if (long != null) {
            when {
                abs(long / 1000000000000) > 1 -> BigDecimal(long / 1000000000000).setScale(
                    roundLimit,
                    RoundingMode.HALF_EVEN
                ).toString() + "T"
                abs(long / 1000000000) > 1 -> BigDecimal(long / 1000000000).setScale(
                    roundLimit,
                    RoundingMode.HALF_EVEN
                ).toString() + "B"
                abs(long / 1000000) > 1 -> BigDecimal(long / 1000000).setScale(
                    roundLimit,
                    RoundingMode.HALF_EVEN
                ).toString() + "M"
                abs(long / 1000) > 1 -> BigDecimal(long / 1000).setScale(
                    roundLimit,
                    RoundingMode.HALF_EVEN
                ).toString() + "K"
                else -> formatDouble(long.toDouble())
            }
        } else {
            "N/A"
        }
    }

    /**
     * Retrieves interpreted color with [change]
     *
     * @param context Required to get the interpreted color with [ContextCompat]
     * @param change The change to determine the color
     * @return Color [Int]
     * @author James Ding
     */
    fun getColor(context: Context, change: Double) = if (change >= 0) {
        ContextCompat.getColor(context, R.color.stock_positive)
    } else {
        ContextCompat.getColor(context, R.color.stock_negative)
    }

    /**
     * Retrieves color identification with [change]
     *
     * @param change The change to determine the color
     * @return Color Identification [Int]
     * @author James Ding
     */
    fun getRawColor(change: Double) = if (change >= 0) {
        R.color.stock_positive
    } else {
        R.color.stock_negative
    }

    /**
     * Retrieves drawable with [change]
     *
     * @param context Required to get the drawable with [ContextCompat]
     * @param change The change to determine the color
     */
    fun getChangeDrawable(context: Context, change: Double) = if (change >= 0) {
        ContextCompat.getDrawable(context, R.drawable.avd_tdtu)
    } else {
        ContextCompat.getDrawable(context, R.drawable.avd_tutd)
    }

    /**
     * Formats [EditText] for [Int]
     *
     * Code sourced from [https://stackoverflow.com/questions/34607560/add-thousand-separators-automatically-while-text-changes-in-android-edittext/34607831]
     *
     * @author Shree Krishna
     */
    class NumberTextWatcherForThousand(var editText: EditText) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) {
            try {
                editText.removeTextChangedListener(this)
                val value = editText.text.toString()
                if (value != "") {
                    if (value.startsWith(".")) {
                        editText.setText("0.")
                    }
                    if (value.startsWith("0") && !value.startsWith("0.")) {
                        editText.setText("")
                    }
                    val str = editText.text.toString().replace(",".toRegex(), "")
                    if (value != "") editText.setText(getDecimalFormattedString(str))
                    editText.setSelection(editText.text.toString().length)
                }
                editText.addTextChangedListener(this)
                return
            } catch (ex: Exception) {
                ex.printStackTrace()
                editText.addTextChangedListener(this)
            }
        }

        companion object {
            fun getDecimalFormattedString(value: String): String {
                val lst = StringTokenizer(value, ".")
                var str1 = value
                var str2 = ""
                if (lst.countTokens() > 1) {
                    str1 = lst.nextToken()
                    str2 = lst.nextToken()
                }
                var str3 = ""
                var i = 0
                var j = -1 + str1.length
                if (str1[-1 + str1.length] == '.') {
                    j--
                    str3 = "."
                }
                var k = j
                while (true) {
                    if (k < 0) {
                        if (str2.isNotEmpty()) str3 = "$str3.$str2"
                        return str3
                    }
                    if (i == 3) {
                        str3 = ",$str3"
                        i = 0
                    }
                    str3 = str1[k].toString() + str3
                    i++
                    k--
                }
            }

            fun trimCommaOfString(string: String): String {
                return if (string.contains(",")) {
                    string.replace(",", "")
                } else {
                    string
                }
            }
        }
    }

    /**
     * Sourced from [https://stackoverflow.com/questions/8140571/resizing-layouts-programmatically-as-animation]
     *
     * @author Tom Howard, Faylon
     */
    class ResizeAnimation(
        private val view: View,
        private val toHeight: Float,
        private val fromHeight: Float,
        private val toWidth: Float,
        private val fromWidth: Float,
        duration: Long
    ) : Animation() {

        init {
            this.duration = duration
        }

        override fun applyTransformation(
            interpolatedTime: Float,
            t: Transformation?
        ) {
            val height = (toHeight - fromHeight) * interpolatedTime + fromHeight
            val width = (toWidth - fromWidth) * interpolatedTime + fromWidth
            val layoutParams = view.layoutParams
            layoutParams.height = height.toInt()
            layoutParams.width = width.toInt()
            view.requestLayout()
        }
    }
}
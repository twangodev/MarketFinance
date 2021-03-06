package com.marketfinance.app.ui.fragments.advancedStockFragment

import android.annotation.SuppressLint
import android.graphics.DashPathEffect
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.mikephil.charting.charts.BubbleChart
import com.github.mikephil.charting.data.BubbleData
import com.github.mikephil.charting.data.BubbleDataSet
import com.github.mikephil.charting.data.BubbleEntry
import com.github.mikephil.charting.interfaces.datasets.IBubbleDataSet
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.marketfinance.app.R
import com.marketfinance.app.ui.fragments.advancedStockFragment.adapters.NewsRecyclerViewAdapter
import com.marketfinance.app.ui.fragments.advancedStockFragment.adapters.NewsResponseData
import com.marketfinance.app.ui.fragments.advancedStockFragment.adapters.SparkViewAdapter
import com.marketfinance.app.ui.fragments.advancedStockFragment.adapters.SparkViewData
import com.marketfinance.app.ui.fragments.advancedStockFragment.data.AdvancedStockIntentData
import com.marketfinance.app.ui.fragments.advancedStockFragment.data.CandlestickData
import com.marketfinance.app.ui.fragments.transactions.PurchaseFragment
import com.marketfinance.app.utils.Calculations
import com.marketfinance.app.utils.FragmentTransactions
import com.marketfinance.app.utils.MarketInterface
import com.marketfinance.app.utils.RequestSingleton
import com.marketfinance.app.utils.network.APIInterface
import com.marketfinance.app.utils.objects.Defaults
import com.marketfinance.app.utils.threads.ThreadManager
import com.robinhood.spark.SparkView
import com.robinhood.spark.animation.LineSparkAnimator
import com.robinhood.ticker.TickerView
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.math.abs
import kotlin.math.round

class AdvancedStockFragment : Fragment(), FragmentTransactions, MarketInterface, Calculations {

    private val TAG = "AdvancedStockFragment"

    private val gson = Gson()
    private val threadManager = ThreadManager()
    private val apiInterface = APIInterface("", ValidIntervals.Spark.ONE_DAY)

    private var queue: RequestSingleton? = null
    private var paused = false
    private var jsonData = ""
    private var symbol = ""
    private var name = ""
    private var quoteType = ""
    private var currentMarketPrice: Double? = null
    private var chartPreviousClose: Double? = null
    private var change: Double? = null
    private var percentage: Double? = null
    private var sparkData = SparkViewData(mutableListOf(), 0.00)
    private var candleStickData = mutableListOf<CandlestickData>()
    private var financialDataRequestCount = 0
    private var financialDataRequestErrorCount = 0
    private var financialDataRequestErrorLimit = 10
    private var historicalDataRequestCount = 0
    private var newsList = mutableListOf<NewsResponseData?>(null)
    private var actualEarningsList = mutableListOf<BubbleEntry>()
    private var estimateEarningsList = mutableListOf<BubbleEntry>()

    var polarityColor = R.color.stock_offline
    var chartType = ChartType.LineChart

    @SuppressLint("SetTextI18n") // TODO remove
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_advanced_stock, container, false)

        jsonData = arguments?.getString("json")!!
        val advancedStockIntentData = gson.fromJson(jsonData, AdvancedStockIntentData::class.java)
        Log.d(TAG, "Received IntentData: $advancedStockIntentData")

        symbol = advancedStockIntentData.symbol
        name = advancedStockIntentData.name
        quoteType = advancedStockIntentData.quoteType

        apiInterface.symbol = symbol

        view.findViewById<SwipeRefreshLayout>(R.id.advancedStockFragment_swipeRefreshLayout).apply {
            setOnRefreshListener {
                performRefresh()
                isRefreshing = false
            }
        }
        view.findViewById<TextView>(R.id.advancedStockFragment_symbol_textView).text = symbol
        view.findViewById<TextView>(R.id.advancedStockFragment_floatingScrollView_symbol_textView).text =
            symbol
        view.findViewById<TextView>(R.id.advancedStockFragment_details_textView).text = try {
            name.substring(0, Defaults.nameSubstringLimit) + "…"
        } catch (error: StringIndexOutOfBoundsException) {
            name
        }

        val highlightColor = ContextCompat.getColor(requireContext(), R.color.theme_colorPrimary)

        fun setTimeInterval(generalInterval: Int, textView: TextView) {
            val selectedChartType = when (chartType) {
                ChartType.LineChart -> {
                    when (generalInterval) {
                        ValidIntervals.General.ONE_DAY -> ValidIntervals.Spark.ONE_DAY
                        ValidIntervals.General.FIVE_DAY -> ValidIntervals.Spark.FIVE_DAY
                        ValidIntervals.General.ONE_MONTH -> ValidIntervals.Spark.ONE_MONTH
                        ValidIntervals.General.THREE_MONTH -> ValidIntervals.Spark.THREE_MONTH
                        ValidIntervals.General.ONE_YEAR -> ValidIntervals.Spark.ONE_YEAR
                        else -> ValidIntervals.Spark.ONE_DAY
                    }
                }
                ChartType.CandleChart -> {
                    when (generalInterval) {
                        ValidIntervals.General.ONE_DAY -> ValidIntervals.CandleChart.ONE_DAY
                        ValidIntervals.General.FIVE_DAY -> ValidIntervals.CandleChart.FIVE_DAY
                        ValidIntervals.General.ONE_MONTH -> ValidIntervals.CandleChart.ONE_MONTH
                        ValidIntervals.General.THREE_MONTH -> ValidIntervals.CandleChart.THREE_MONTH
                        ValidIntervals.General.ONE_YEAR -> ValidIntervals.CandleChart.ONE_YEAR
                        else -> ValidIntervals.CandleChart.ONE_DAY
                    }
                }
                else -> ValidIntervals.Spark.ONE_DAY
            }
            if (apiInterface.rangeInterval == selectedChartType) {
                return
            }
            apiInterface.rangeInterval = selectedChartType
            clearTimeSelectors()
            textView.apply {
                setTextColor(highlightColor)
                background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.background_stock_offline)
                background.setTint(ContextCompat.getColor(requireContext(), polarityColor))
            }
            Log.d(TAG, "[ONCLICK] RangeInterval updated to ${apiInterface.rangeInterval}")
            queue = context?.let { RequestSingleton.getInstance(it) }
            queue?.addToRequestQueue(
                getHistoricalDataRequest(
                    true,
                    financialDataRequestErrorCount >= financialDataRequestErrorLimit,
                    true
                )
            )
            queue?.addToRequestQueue(getFinancialDataRequest())
        }

        view.findViewById<SparkView>(R.id.advancedStockFragment_chart_sparkView)?.apply {
            adapter = SparkViewAdapter(sparkData)
            baseLinePaint.pathEffect = DashPathEffect(floatArrayOf(10F, 10F), 0F)

            scrubListener = SparkView.OnScrubListener { value ->
                if (value == null) {
                    paused = false
                    activity?.findViewById<SwipeRefreshLayout>(R.id.advancedStockFragment_swipeRefreshLayout)?.isEnabled =
                        true
                    view.apply {
                        setPrice()
                        setChange()
                    }
                } else {
                    paused = true
                    activity?.findViewById<SwipeRefreshLayout>(R.id.advancedStockFragment_swipeRefreshLayout)?.isEnabled =
                        false
                    view.apply {
                        val scrubPrice = value as Double
                        findViewById<TickerView>(R.id.advancedStockFragment_price_tickerView)?.setText(
                            formatNullableDoubleWithDollar(scrubPrice),
                            false
                        )
                        val scrubChange =
                            chartPreviousClose?.let { calculateChange(scrubPrice, it) }
                        val scrubPercentage =
                            scrubChange?.let { calculatePercentage(it, scrubPrice) }
                        val changeText =
                            "${formatNullableDoubleWithDollar(scrubChange?.let { abs(it) })} (${
                                formatNullableDouble(scrubPercentage?.let { abs(it) })
                            }%)"
                        findViewById<TickerView>(R.id.advancedStockFragment_change_tickerView)?.setText(
                            changeText,
                            false
                        )
                        if (scrubChange != null) {
                            val assumedColor = getRawColor(scrubChange)
                            if (polarityColor != assumedColor) {
                                setPolarityTheme(assumedColor)
                                activity?.findViewById<ImageView>(R.id.advancedStockFragment_change_imageView)
                                    ?.apply {
                                        setImageDrawable(context?.let {
                                            getChangeDrawable(
                                                it,
                                                scrubChange
                                            )
                                        })
                                        (drawable as AnimatedVectorDrawable).start()
                                    }
                            }
                        }
                    }
                }
            }
        }



        // TODO move this to view.apply
        view.findViewById<TextView>(R.id.advancedStockFragment_timeRangeSelector_1D_textView)
            .apply {
                setTextColor(highlightColor)
                background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.background_stock_offline)
                background.setTint(ContextCompat.getColor(requireContext(), polarityColor))

                setOnClickListener { setTimeInterval(ValidIntervals.General.ONE_DAY, this) }
            }
        view.findViewById<TextView>(R.id.advancedStockFragment_timeRangeSelector_5D_textView)
            .apply {
                setOnClickListener { setTimeInterval(ValidIntervals.General.FIVE_DAY, this) }
            }
        view.findViewById<TextView>(R.id.advancedStockFragment_timeRangeSelector_1M_textView)
            .apply {
                setOnClickListener { setTimeInterval(ValidIntervals.General.ONE_MONTH, this) }
            }
        view.findViewById<TextView>(R.id.advancedStockFragment_timeRangeSelector_3M_textView)
            .apply {
                setOnClickListener { setTimeInterval(ValidIntervals.General.THREE_MONTH, this) }
            }
        view.findViewById<TextView>(R.id.advancedStockFragment_timeRangeSelector_1Y_textView)
            .apply {
                setOnClickListener { setTimeInterval(ValidIntervals.General.ONE_YEAR, this) }
            }

        view.apply {


            findViewById<RecyclerView>(R.id.advancedStockFragment_news_recyclerView).adapter =
                NewsRecyclerViewAdapter(newsList)

            for (id in AdvancedStockLayoutIDs.directTextColorChangeTickerViews) {
                initializeTicker(
                    findViewById(id),
                    getString(R.string.default_price),
                    resources.getFont(R.font.roboto_condensed),
                    Defaults.tickerDefaultAnimation
                )
            }

            for (id in AdvancedStockLayoutIDs.bidAskTickerViews) {
                initializeTicker(
                    findViewById(id),
                    getString(R.string.default_bidAsk),
                    resources.getFont(R.font.roboto_condensed),
                    Defaults.tickerDefaultAnimation
                )
            }



            findViewById<TickerView>(R.id.advancedStockFragment_change_tickerView).text =
                getString(R.string.default_change)
            findViewById<TickerView>(R.id.advancedStockFragment_quickStatistics_volume_tickerView).text =
                getString(R.string.default_double)
            findViewById<TickerView>(R.id.advancedStockFragment_trade_statistics_units_tickerView).text =
                getString(R.string.default_double)
            findViewById<TickerView>(R.id.advancedStockFragment_trade_statistics_portfolioMakeup_tickerView).text =
                getString(R.string.default_percentage)
        }

        // Final Initialization
        financialDataRequestCount = 0
        historicalDataRequestCount = 0
        financialDataRequestErrorCount = 0
        setPolarityTheme(R.color.stock_offline) // Reset Layout
        clearTimeSelectors()
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.apply {

            val transaction = supportFragmentManager.beginTransaction()
            val fragmentAnimations = FragmentTransactions.FragmentAnimations(
                R.anim.fragment_child_enter, R.anim.fragment_parent_exit,
                R.anim.fragment_parent_enter, R.anim.fragment_child_exit
            )

            findViewById<TextView>(R.id.advancedStockFragment_trade_sell_textView).setOnClickListener {
                val purchaseFragment = attachJSONtoFragment(PurchaseFragment(), jsonData)
                replaceFragment(purchaseFragment, transaction, fragmentAnimations, true)
                Log.d(TAG, "[ONCLICK] Attaching JSON to SellFragment. JSON: $jsonData")
                threadManager.killAllThreads()
            }
            findViewById<TextView>(R.id.advancedStockFragment_trade_buy_textView).setOnClickListener {
                val sellFragment = attachJSONtoFragment(PurchaseFragment(), jsonData)
                replaceFragment(sellFragment, transaction, fragmentAnimations, true)
                Log.d(TAG, "[ONCLICK] Attaching JSON to PurchaseFragment. JSON: $jsonData")
                threadManager.killAllThreads()
            }

        }

        activity?.findViewById<BottomNavigationView>(R.id.main_bottomNavigationView)?.menu?.findItem(
            R.id.menu_bottomNavigationView_search
        )?.isChecked = true
        val floatingScrollView =
            activity?.findViewById<ScrollView>(R.id.advancedStockFragment_scrollView)
        floatingScrollView?.viewTreeObserver?.addOnScrollChangedListener(
            FloatingScrollView(
                activity,
                floatingScrollView
            ).listener
        )

        queue = context?.let { RequestSingleton(it) }

        // Initialize
        val requests = listOf(
            getHistoricalDataRequest(true, true, true),
            getFinancialDataRequest(),
            getQuoteSummaryRequest(),
            getNewsDataRequest()
        )
        for (i in requests) {
            queue?.addToRequestQueue(i)
        }

        // Continuous Threads
        threadManager.createThread("FinancialDataThread", Thread {
            while (!Thread.interrupted() && financialDataRequestErrorCount < financialDataRequestErrorLimit) {
                if (!paused) {
                   try {
                       Thread.sleep(Defaults.priceAPIFrequency)
                       queue?.addToRequestQueue(getFinancialDataRequest())
                   } catch (error: InterruptedException) {
                       Log.e(TAG, error.stackTraceToString())
                       break
                   }
                }

            }
        })

        threadManager.createThread("HistoricalDataThread", Thread {
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(Defaults.chartAPIFrequency)
                    queue?.addToRequestQueue(
                        getHistoricalDataRequest(
                            false,
                            financialDataRequestErrorCount >= financialDataRequestErrorLimit,
                            false
                        )
                    )
                } catch (error: InterruptedException) {
                    Log.e(TAG, error.stackTraceToString())
                    break
                }
            }
        }
        )
    }

    override fun onResume() {
        super.onResume()

        threadManager.startAllThreads()
    }

    override fun onPause() {
        super.onPause()

        threadManager.killAllThreads()
    }

    override fun onStop() {
        super.onStop()

        threadManager.killAllThreads()
    }


    private fun clearTimeSelectors() {
        for (selector in AdvancedStockLayoutIDs.timeSelectors) {
            activity?.findViewById<TextView>(selector)?.apply {
                background = null
                setTextColor(ContextCompat.getColor(requireContext(), polarityColor))
            }
        }
    }

    private fun setPolarityTheme(colorID: Int) {

        // Note: Change Icon is not handled here

        polarityColor = colorID // Used for future input without recalling function

        val interpretedColor = context?.let { ContextCompat.getColor(it, colorID) }

        if (interpretedColor != null) { // TODO move into activity?.apply
            val buttonBackground = when (colorID) {
                R.color.stock_positive -> ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.rounded_stroke_positive
                )
                R.color.stock_negative -> ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.rounded_stroke_negative
                )
                else -> ContextCompat.getDrawable(requireContext(), R.drawable.rounded_stroke_offline)
            }

            activity?.findViewById<SwipeRefreshLayout>(R.id.advancedStockFragment_swipeRefreshLayout)
                ?.setColorSchemeColors(interpretedColor)

            for (id in AdvancedStockLayoutIDs.directTextColorChangeTickerViews) {
                activity?.findViewById<TickerView>(id)?.textColor = interpretedColor
            }

            clearTimeSelectors()
            val activeTimeSelector: TextView? = when (apiInterface.rangeInterval) {
                ValidIntervals.Spark.ONE_DAY, ValidIntervals.CandleChart.ONE_DAY -> activity?.findViewById(
                    R.id.advancedStockFragment_timeRangeSelector_1D_textView
                )
                ValidIntervals.Spark.FIVE_DAY, ValidIntervals.CandleChart.FIVE_DAY -> activity?.findViewById(
                    R.id.advancedStockFragment_timeRangeSelector_5D_textView
                )
                ValidIntervals.Spark.ONE_MONTH, ValidIntervals.CandleChart.ONE_MONTH -> activity?.findViewById(
                    R.id.advancedStockFragment_timeRangeSelector_1M_textView
                )
                ValidIntervals.Spark.THREE_MONTH, ValidIntervals.CandleChart.THREE_MONTH -> activity?.findViewById(
                    R.id.advancedStockFragment_timeRangeSelector_3M_textView
                )
                ValidIntervals.Spark.ONE_YEAR, ValidIntervals.CandleChart.ONE_YEAR -> activity?.findViewById(
                    R.id.advancedStockFragment_timeRangeSelector_1Y_textView
                )
                else -> activity?.findViewById(R.id.advancedStockFragment_timeRangeSelector_1D_textView)
            }

            activeTimeSelector?.apply {
                setTextColor(ContextCompat.getColor(requireContext(), R.color.theme_colorPrimary))
                background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.rounded_stroke_offline)
                background.setTint(interpretedColor)
            }

            activity?.findViewById<ImageView>(R.id.advancedStockFragment_chartSelector_imageView)
                ?.setColorFilter(interpretedColor)
            activity?.findViewById<ConstraintLayout>(R.id.advancedStockFragment_trade_sell_constraintLayout)?.background =
                buttonBackground
            activity?.findViewById<ConstraintLayout>(R.id.advancedStockFragment_trade_buy_constraintLayout)?.background =
                buttonBackground
            activity?.findViewById<SparkView>(R.id.advancedStockFragment_chart_sparkView)?.lineColor =
                interpretedColor
            activity?.findViewById<View>(R.id.advancedStockFragment_analystRatings_circle_background)?.background =
                when (colorID) {
                    R.color.stock_positive -> ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.circle_positive
                    )
                    R.color.stock_negative -> ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.circle_negative
                    )
                    else -> ContextCompat.getDrawable(requireContext(), R.drawable.circle_offline)
                }

            for (id in AdvancedStockLayoutIDs.analystRatingsProgressBars) {
                activity?.findViewById<ProgressBar>(id)?.progressDrawable?.setTint(interpretedColor)
            }

            updateBubbleCharts(interpretedColor)
            activity?.findViewById<TextView>(R.id.advancedStockFragment_about_readMore_textView)
                ?.setTextColor(interpretedColor)
        }


    }

    private fun performRefresh() {
        val requests = listOf(
            getQuoteSummaryRequest(),
            getNewsDataRequest(),
            getFinancialDataRequest(),
            getHistoricalDataRequest(
                true,
                financialDataRequestErrorCount >= financialDataRequestErrorLimit,
                true
            )
        )
        for (i in requests) {
            queue?.addToRequestQueue(i)
        }
    }

    @SuppressLint("SetTextI18n") // TODO remove
    private fun getQuoteSummaryRequest() = apiInterface.getOneTimeData({ response ->
        Log.d(TAG, "[REQUEST] Code 200. Request for oneTimeData.")

        val result = try {
            response.getJSONObject("quoteSummary").getJSONArray("result").getJSONObject(0)
        } catch (error: JSONException) {
            JSONExceptionHandler.jsonObject("result")
        }

        // Asset Profile
        val assetProfile = try {
            result?.getJSONObject("assetProfile")
        } catch (error: JSONException) {
            JSONExceptionHandler.jsonObject("assetProfile")
        }
        val businessSummary = try {
            assetProfile?.getString("longBusinessSummary")
        } catch (error: JSONException) {
            JSONExceptionHandler.string("longBusinessSummary")
        }
        if (businessSummary != null) {
            activity?.findViewById<TextView>(R.id.advancedStockFragment_about_summary_textView)?.text =
                try {
                    businessSummary.substring(0, Defaults.aboutSummaryLimit) + "…"
                } catch (error: StringIndexOutOfBoundsException) {
                    businessSummary
                }
            activity?.findViewById<TextView>(R.id.advancedStockFragment_about_readMore_textView)
                ?.setOnClickListener {
                    activity?.findViewById<TextView>(R.id.advancedStockFragment_about_summary_textView)?.text =
                        businessSummary
                    it.visibility = View.GONE
                }
        }

        // Earnings Data
        val earningsTrend = try {
            result?.getJSONObject("earningsHistory")
        } catch (error: JSONException) {
            JSONExceptionHandler.jsonObject("earningsHistory")
        }
        if (earningsTrend != null) {
            val history = try {
                earningsTrend.getJSONArray("history")
            } catch (error: JSONException) {
                JSONExceptionHandler.jsonArray("history")
            }
            val dateList = mutableListOf<String>()
            actualEarningsList.clear()
            estimateEarningsList.clear()
            if (history != null) {
                for (i in 0 until history.length()) {
                    val element = try {
                        history.getJSONObject(i)
                    } catch (error: JSONException) {
                        JSONExceptionHandler.jsonObject("history $i")
                    }
                    val date = try {
                        element?.getJSONObject("quarter")?.getString("fmt")
                    } catch (error: JSONException) {
                        JSONExceptionHandler.string("quarterFmt")
                    }
                    val actual = try {
                        element?.getJSONObject("epsActual")?.getDouble("raw")
                    } catch (error: JSONException) {
                        JSONExceptionHandler.double("epsActualRaw")
                    }?.toFloat()
                    val estimate = try {
                        element?.getJSONObject("epsEstimate")?.getDouble("raw")
                    } catch (error: JSONException) {
                        JSONExceptionHandler.double("epsEstimateRaw")
                    }?.toFloat()
                    if (date != null && estimate != null && actual != null) {
                        dateList.add(date)
                        actualEarningsList.add(
                            BubbleEntry(
                                BubbleConfiguration.bubbleX(i),
                                actual,
                                BubbleConfiguration.size
                            )
                        )
                        estimateEarningsList.add(
                            BubbleEntry(
                                BubbleConfiguration.bubbleX(i),
                                estimate,
                                BubbleConfiguration.size
                            )
                        )
                    }
                }
                for (i in actualEarningsList.indices) {
                    activity?.apply {
                        val earningPrice = BigDecimal(actualEarningsList[i].y.toDouble()).setScale(
                            Defaults.roundLimit,
                            RoundingMode.HALF_EVEN
                        ).toDouble()
                        val estimatePrice = estimateEarningsList[i].y.toDouble()
                        val difference = calculateChange(earningPrice, estimatePrice)
                        val earningText =
                            "${formatNullableDouble(earningPrice)} (${abs(difference)})"
                        findViewById<TextView>(AdvancedStockLayoutIDs.earningsDate[i]).text =
                            dateList[i]
                        findViewById<TextView>(AdvancedStockLayoutIDs.earningsChange[i]).apply {
                            text = earningText
                            setTextColor(getColor(context, difference))
                        }
                    }
                }
            }
            // No label because manual legend
            val interpretedColor = ContextCompat.getColor(requireContext(), polarityColor)
            updateBubbleCharts(interpretedColor)
        } else {
            activity?.findViewById<ConstraintLayout>(R.id.advancedStockFragment_earnings_constraintLayout)?.visibility =
                View.GONE
        }


        // Recommendation Trend Data
        val recommendationTrend = try {
            result?.getJSONObject("recommendationTrend")
        } catch (error: JSONException) {
            JSONExceptionHandler.jsonObject("recommendationTrend")
        }
        val trend = try {
            recommendationTrend?.getJSONArray("trend")
        } catch (error: JSONException) {
            JSONExceptionHandler.jsonArray("trend")
        }
        val latestTrend = try {
            trend?.getJSONObject(0)
        } catch (error: JSONException) {
            JSONExceptionHandler.jsonObject("0m")
        }
        val strongBuy = latestTrend?.getInt("strongBuy") // todo move into try catch
        val buy = latestTrend?.getInt("buy")
        val hold = latestTrend?.getInt("hold")
        val sell = latestTrend?.getInt("sell")
        val strongSell = latestTrend?.getInt("strongSell")
        if (strongBuy != null && buy != null && hold != null && sell != null && strongSell != null) {
            val totalBuy = strongBuy + buy
            val totalSell = strongSell + sell
            val total = totalBuy + totalSell + hold
            val analystData = if (total != 0) {
                mapOf(
                    getString(R.string.default_buy) to listOf(totalBuy, (totalBuy * 100) / total),
                    getString(R.string.advancedStock_analystRatings_hold) to listOf(
                        hold,
                        (hold * 100) / total
                    ),
                    getString(R.string.default_sell) to listOf(totalSell, (totalSell * 100) / total)
                )
            } else {
                null
            }
            val analystDataKeys = analystData?.keys?.toList()
            val analystDataValues = analystData?.values?.toList()
            val percentageList = mutableListOf<Int>()
            if (analystDataKeys != null && analystDataValues != null) {
                for (i in analystDataKeys.indices) {
                    val percentage = analystDataValues[i][1]
                    val text = "${analystDataKeys[i]}: ${analystDataValues[i][0]} ($percentage%)"
                    activity?.apply {
                        findViewById<TextView>(AdvancedStockLayoutIDs.analystRatingsTextViews[i])?.text =
                            text
                        findViewById<ProgressBar>(AdvancedStockLayoutIDs.analystRatingsProgressBars[i])?.progress =
                            percentage
                        percentageList.add(percentage)
                    }
                }
                val largestPercentage = percentageList.maxOrNull()
                if (largestPercentage != null) {
                    val largestItem = analystDataKeys[percentageList.indexOf(largestPercentage)]
                    activity?.apply {
                        findViewById<TextView>(R.id.advancedStockFragment_analystRating_rating_textView)?.text =
                            largestItem
                        findViewById<TextView>(R.id.advancedStockFragment_analystRating_circle_percentage_textView)?.text =
                            "$largestPercentage%"
                    }
                }
                activity?.findViewById<TextView>(R.id.advancedStockFragment_analystRating_circle_total_textView)?.text =
                    "$total total ratings"
            } else {
                for (i in AdvancedStockLayoutIDs.analystRatingsTextViews) {
                    activity?.findViewById<TextView>(i)?.text = getString(R.string.default_na)
                }
                for (i in AdvancedStockLayoutIDs.analystRatingsProgressBars) {
                    activity?.findViewById<ProgressBar>(i)?.progress = 0
                }
            }
        } else {
            Log.e(TAG, "Analyst Data not available. Removing layout.")
            activity?.findViewById<ConstraintLayout>(R.id.advancedStockFragment_analystRating_constraintLayout)?.visibility =
                View.GONE
        }
    }) { error ->
        Log.e(
            TAG,
            "Volley Error while getting analyst data. Removing Layout.\n${error.stackTraceToString()}"
        )
        activity?.findViewById<ConstraintLayout>(R.id.advancedStockFragment_analystRating_constraintLayout)?.visibility =
            View.GONE
    }

    private fun getNewsDataRequest() = apiInterface.getSymbolNewsData({ response ->
        Log.d(TAG, "NewsDataRequest [SUCCESS]")

        newsList.clear()
        val items = try {
            response.getJSONArray("items")
        } catch (error: JSONException) {
            JSONExceptionHandler.jsonArray("items")
        }

        if (items != null) {
            for (i in 0 until items.length()) {
                try {
                    val newsElement = items.getJSONObject(i)
                    newsElement.apply {
                        val title = getString("title")
                        val publishDate = getString("pubDate")
                        val link = getString("link")
                        val content = HtmlCompat.fromHtml(
                            getString("content"),
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        ).toString()
                        newsList.add(NewsResponseData(title, publishDate, link, content))
                    }
                } catch (error: JSONException) {
                    JSONExceptionHandler.jsonObject("item $i")
                } catch (error: NullPointerException) {
                    JSONExceptionHandler.jsonObject("item $i")
                }
            }
        }
        if (newsList.size == 0) {
            newsList.add(null)
        }
        activity?.findViewById<RecyclerView>(R.id.advancedStockFragment_news_recyclerView)?.adapter?.notifyDataSetChanged()
    }) { error ->
        newsList.clear()
        newsList.add(null)
        activity?.findViewById<RecyclerView>(R.id.advancedStockFragment_news_recyclerView)?.adapter?.notifyDataSetChanged()
    }

    private fun getFinancialDataRequest() = apiInterface.finance({ response ->
        financialDataRequestCount++
        Log.d(TAG, "[REQUEST] Code 200. Request for $symbol Count: $financialDataRequestCount.")

        val result = try {
            response.getJSONObject("quoteSummary")
                .getJSONArray("result")[0] as JSONObject
        } catch (error: JSONException) {
            JSONExceptionHandler.jsonObject("result")
        } catch (error: NullPointerException) {
            JSONExceptionHandler.jsonObject("result")
        }
        val financialData = try {
            result?.getJSONObject("financialData")
        } catch (error: JSONException) {
            JSONExceptionHandler.jsonObject("financialData")
        }

        currentMarketPrice = try {
            financialData?.getJSONObject("currentPrice")?.getDouble("raw")
        } catch (error: JSONException) {
            JSONExceptionHandler.double("currentRawPrice")
        }
        setPrice()
        if (currentMarketPrice != null && chartPreviousClose != null) {
            change = calculateChange(currentMarketPrice!!, chartPreviousClose!!)
            percentage = calculatePercentage(change!!, currentMarketPrice!!)

            setChange()

            val assumedColor = getRawColor(change!!)
            if (polarityColor != assumedColor) {
                setPolarityTheme(assumedColor)
                activity?.findViewById<ImageView>(R.id.advancedStockFragment_change_imageView)
                    ?.apply {
                        setImageDrawable(context?.let { getChangeDrawable(it, change!!) })
                        (drawable as AnimatedVectorDrawable).start()
                    }
            }

            sparkData.apply {
                yData[yData.size - 1] = currentMarketPrice!!
                sparkDataNotifyChanged(false)
            }
        }

        val oneYearTargetEstimate = try {
            financialData?.getJSONObject("targetMeanPrice")
                ?.getDouble("raw")
        } catch (error: JSONException) {
            JSONExceptionHandler.double("oneYearTargetEstimateRawPrice")
        }
        activity?.findViewById<TickerView>(R.id.advancedStockFragment_quickStatistics_oneYearTargetEst_tickerView)
            ?.setText(formatNullableDoubleWithDollar(oneYearTargetEstimate), true)
    }, {
        financialDataRequestErrorCount++
    })

    private fun getHistoricalDataRequest(
        firstTime: Boolean,
        financialDataRequestEnabled: Boolean,
        animate: Boolean
    ) = apiInterface.historical({ response ->
        historicalDataRequestCount++
        Log.d(TAG, "HistoricalDataRequest [SUCCESS] Count: $historicalDataRequestCount")

        val result = try {
            response.getJSONObject("chart")
                .getJSONArray("result")[0] as JSONObject
        } catch (error: JSONException) {
            JSONExceptionHandler.jsonObject("result")
        }
        val meta = try {
            result?.getJSONObject("meta")
        } catch (error: JSONException) {
            JSONExceptionHandler.jsonObject("meta")
        }
        val quote = try {
            result?.getJSONObject("indicators")
                ?.getJSONArray("quote")?.get(0) as JSONObject
        } catch (error: JSONException) {
            JSONExceptionHandler.jsonObject("quote")
        }
        chartPreviousClose = try {
            meta?.getDouble("chartPreviousClose")
        } catch (error: JSONException) {
            JSONExceptionHandler.double("chartPreviousClose")
        }
        activity?.findViewById<TickerView>(R.id.advancedStockFragment_quickStatistics_chartPreviousClose_tickerView)
            ?.setText(formatNullableDoubleWithDollar(chartPreviousClose), true)

        if (firstTime || !financialDataRequestEnabled) {
            currentMarketPrice = try {
                meta?.getDouble("regularMarketPrice")
            } catch (error: JSONException) {
                JSONExceptionHandler.double("currentMarketPrice")
            }
            setPrice()

            change = currentMarketPrice?.let {
                chartPreviousClose?.let { it1 ->
                    calculateChange(
                        it,
                        it1
                    )
                }
            }
            percentage =
                change?.let { currentMarketPrice?.let { it1 -> calculatePercentage(it, it1) } }

            setChange()

            val assumedColor = getRawColor(change!!)
            if (polarityColor != assumedColor) {
                setPolarityTheme(assumedColor)
                activity?.findViewById<ImageView>(R.id.advancedStockFragment_change_imageView)
                    ?.apply {
                        setImageDrawable(context?.let { getChangeDrawable(it, change!!) })
                        (drawable as AnimatedVectorDrawable).start()
                    }
            }

            // Logic for valid ranges (removes unavailable ranges)
            val validRanges = try {
                meta?.getJSONArray("validRanges")
            } catch (error: JSONException) {
                JSONExceptionHandler.jsonArray("validRanges")
            }
            val validRangesList = mutableListOf<String>()
            if (validRanges != null) {
                for (i in 0 until validRanges.length()) {
                    validRangesList.add(validRanges.getString(i))
                }
            }
            for (i in AdvancedStockLayoutIDs.timeRanges) {
                val textView = activity?.findViewById<TextView>(i.value)
                textView?.visibility = if (validRangesList.contains(i.key)) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }

        // Set Padding based off time
        val givenTimezone = try {
            meta?.getString("exchangeTimezoneName")
        } catch (error: JSONException) {
            JSONExceptionHandler.string("exchangeTimezoneName")
        }
        val timeZone = ZoneId.of(givenTimezone) // Note: Returns GMT if cannot parse
        val tradingPeriods = try {
            meta?.getJSONObject("tradingPeriods")
        } catch (error: JSONException) {
            JSONExceptionHandler.jsonObject("tradingPeriods")
        }
        val startingEpoch = try {
            tradingPeriods?.getJSONArray("pre")?.getJSONArray(0)?.getJSONObject(0)?.getLong("start")
        } catch (error: JSONException) {
            JSONExceptionHandler.long("startingEpoch")
        }
        val endingEpoch = try {
            val post = tradingPeriods?.getJSONArray("post")
            post?.getJSONArray(post.length() - 1)?.getJSONObject(0)?.getLong("end")
        } catch (error: JSONException) {
            JSONExceptionHandler.long("endingEpoch")
        }
        val currentEpochTime = ZonedDateTime.now(timeZone).toInstant().epochSecond
        if (startingEpoch != null && endingEpoch != null && currentEpochTime < endingEpoch) {
            val timePassed = currentEpochTime - startingEpoch
            val totalTime = endingEpoch - startingEpoch
            val dayRatio = timePassed.toDouble() / totalTime.toDouble()
            Log.i(
                TAG,
                "Current Padding Day Ratio: $dayRatio. Start: $startingEpoch, Current: $currentEpochTime, End: $endingEpoch. Calculated Time Passed: $timePassed, Calculated Total Time: $totalTime"
            )
            activity?.findViewById<SparkView>(R.id.advancedStockFragment_chart_sparkView)?.apply {
                val paddingRight = width - round(width * dayRatio)
                setPadding(paddingLeft, paddingTop, paddingRight.toInt(), paddingBottom)
            }
        }

        val volumeData = try {
            quote?.getJSONArray("volume")
        } catch (error: JSONException) {
            JSONExceptionHandler.jsonArray("volume")
        } // TODO implement candlestick/oplc charts
        val highData = try {
            quote?.getJSONArray("high")
        } catch (error: JSONException) {
            JSONExceptionHandler.jsonArray("high")
        }
        val closeData = try {
            quote?.getJSONArray("close")
        } catch (error: JSONException) {
            JSONExceptionHandler.jsonArray("close")
        }
        val lowData = try {
            quote?.getJSONArray("low")
        } catch (error: JSONException) {
            JSONExceptionHandler.jsonArray("low")
        }
        val openData = try {
            quote?.getJSONArray("open")
        } catch (error: JSONException) {
            JSONExceptionHandler.jsonArray("open")
        }

        val volumeDataList = mutableListOf<Long>()
        val highDataList = mutableListOf<Double>()
        val closeDataList = mutableListOf<Double>()
        val lowDataList = mutableListOf<Double>()
        val openDataList = mutableListOf<Double>()

        if (volumeData != null) {
            for (i in 0 until volumeData.length()) {
                try {
                    val volume = volumeData.getLong(i)
                    volumeDataList.add(volume)
                } catch (error: JSONException) {
                    Log.e(TAG, "Volume null element found at position $i")
                }
            }
            activity?.findViewById<TickerView>(R.id.advancedStockFragment_quickStatistics_volume_tickerView)
                ?.setText(formatLargeNumber(volumeDataList.sum().toDouble()), true)
        }
        if (closeData != null) {
            sparkData.yData.clear()
            for (i in 0 until closeData.length()) {
                try {
                    sparkData.yData.add(closeData.getDouble(i))
                    closeDataList.add(closeData.getDouble(i))
                } catch (error: JSONException) {
                    Log.e(TAG, "CloseData null element found at position $i")
                }
            }
            chartPreviousClose?.let { sparkData.chartPreviousClose = it }
            sparkDataNotifyChanged(animate)

            val sortedCloseData = closeDataList.sorted()
            val chartLow = try {
                sortedCloseData.first()
            } catch (error: NoSuchElementException) {
                null
            }
            val chartHigh = try {
                sortedCloseData.last()
            } catch (error: NoSuchElementException) {
                null
            }

            activity?.apply {
                findViewById<TickerView>(R.id.advancedStockFragment_quickStatistics_chartHigh_tickerView).setText(
                    formatNullableDoubleWithDollar(chartHigh),
                    true
                )
                findViewById<TickerView>(R.id.advancedStockFragment_quickStatistics_chartLow_tickerView).setText(
                    formatNullableDoubleWithDollar(chartLow),
                    true
                )
            }
        }

    }, { error ->
        // TODO
    })

    private fun sparkDataNotifyChanged(animate: Boolean) {
        activity?.findViewById<SparkView>(R.id.advancedStockFragment_chart_sparkView)?.apply {
            sparkAnimator = if (animate) {
                LineSparkAnimator()
            } else {
                null
            }
            adapter?.notifyDataSetChanged()
        }
    }

    private fun setPrice() {
        activity?.findViewById<TickerView>(R.id.advancedStockFragment_price_tickerView)
            ?.setText(formatNullableDoubleWithDollar(currentMarketPrice), true)
        activity?.findViewById<TickerView>(R.id.advancedStockFragment_floatingScrollView_price_tickerView)
            ?.setText(formatNullableDoubleWithDollar(currentMarketPrice), true)
    }

    private fun setChange() {
        val changeText = "${formatNullableDoubleWithDollar(change?.let { abs(it) })} (${
            formatNullableDouble(percentage?.let { abs(it) })
        }%)"

        activity?.findViewById<TickerView>(R.id.advancedStockFragment_change_tickerView)
            ?.setText(changeText, true)
        activity?.findViewById<TickerView>(R.id.advancedStockFragment_floatingScrollView_change_tickerView)
            ?.setText(changeText, true)
    }

    private fun updateBubbleCharts(interpretedColor: Int) {
        val actualEarningsSet = BubbleDataSet(actualEarningsList, "").apply {
            isNormalizeSizeEnabled = false
            color = interpretedColor
        }
        val estimateEarningsSet = BubbleDataSet(estimateEarningsList, "").apply {
            isNormalizeSizeEnabled = false
            setColor(interpretedColor, 155)
        }
        val bubbleDataSets = listOf<IBubbleDataSet>(actualEarningsSet, estimateEarningsSet)
        val bubbleData = BubbleData(bubbleDataSets)
        activity?.findViewById<BubbleChart>(R.id.advancedStockFragment_earnings_bubbleChart)
            ?.apply {
                description.text = ""
                legend.isEnabled = false
                setDrawBorders(false)
                setTouchEnabled(false)
                xAxis.apply {
                    spaceMin = BubbleConfiguration.horizontalPadding
                    spaceMax = BubbleConfiguration.horizontalPadding
                    setDrawLabels(false)
                    setDrawAxisLine(false)
                    setDrawGridLines(false)
                }
                axisLeft.apply {
                    spaceTop = BubbleConfiguration.verticalPadding
                    spaceBottom = BubbleConfiguration.verticalPadding
                    textSize = 12F
                    typeface = resources.getFont(R.font.roboto_condensed)
                    textColor =
                        ContextCompat.getColor(requireContext(), R.color.theme_colorOnPrimary)
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    setDrawLimitLinesBehindData(false)
                }
                axisRight.apply {
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    setDrawLimitLinesBehindData(false)
                    setDrawLabels(false)
                }
                data = bubbleData.apply {
                    setDrawValues(false)
                }
                invalidate()
            }
    }

}
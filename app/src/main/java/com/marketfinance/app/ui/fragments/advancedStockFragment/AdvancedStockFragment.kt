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
import com.marketfinance.app.ui.fragments.advancedStockFragment.adapters.SparkViewAdapter
import com.marketfinance.app.ui.fragments.advancedStockFragment.data.AdvancedStockIntentData
import com.marketfinance.app.ui.fragments.advancedStockFragment.data.AnalystData
import com.marketfinance.app.ui.fragments.advancedStockFragment.data.NewsResponseData
import com.marketfinance.app.ui.fragments.advancedStockFragment.data.SparkViewData
import com.marketfinance.app.ui.fragments.transactions.PurchaseFragment
import com.marketfinance.app.utils.Defaults
import com.marketfinance.app.utils.interfaces.*
import com.marketfinance.app.utils.network.RequestSingleton
import com.marketfinance.app.utils.network.parser.JSONObjectParser
import com.marketfinance.app.utils.network.responses.historical.Quote
import com.marketfinance.app.utils.network.wrappers.APIWrapper
import com.marketfinance.app.utils.network.wrappers.JSONArrayWrapper.Companion.toList
import com.marketfinance.app.utils.network.wrappers.JSONObjectWrapper.Companion.getElement
import com.marketfinance.app.utils.threads.ThreadManager
import com.robinhood.spark.SparkView
import com.robinhood.spark.animation.LineSparkAnimator
import com.robinhood.ticker.TickerView
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToInt

class AdvancedStockFragment : Calculations, Fragment(), FragmentTransactions, MapExtensions, MarketInterface, LayoutManager, JSONObjectParser {

    private val TAG = "AdvancedStockFragment"

    private val gson = Gson()
    private val threadManager = ThreadManager()
    private val apiInterface = APIWrapper("", ValidIntervals.Spark.ONE_DAY)

    private var queue: RequestSingleton? = null
    private var zoneID: ZoneId? = null
    private var paused = false
    private var jsonData = ""
    private var symbol = ""
    private var name = ""
    private var quoteType = ""
    private var currentMarketPrice: Double? = null
    private var previousClosePrice: Double? = null
    private var change: Double? = null
    private var percentage: Double? = null
    private var sparkData = SparkViewData(mutableListOf(), 0.00)
    private var quoteData = Quote(null, null, null, null, null)
    private var timeStampData = listOf<Long?>()
    private var financialDataRequestCount = 0
    private var financialDataRequestErrorCount = 0
    private var financialDataRequestErrorLimit = 10
    private var historicalDataRequestCount = 0
    private var newsList = mutableListOf<NewsResponseData?>(null)
    private var rawEpsActualList = mutableListOf<BubbleEntry>()
    private var rawEpsEstimateList = mutableListOf<BubbleEntry>()

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

            scrubListener = SparkView.OnScrubListener { rawIndex ->
                if (rawIndex != null) {
                    paused = true

                    val index = rawIndex as Int

                    val time = timeStampData[index]
                    val openPrice = quoteData.open?.get(index)
                    val highPrice = quoteData.high?.get(index)
                    val lowPrice = quoteData.low?.get(index)
                    val closePrice = quoteData.close?.get(index)
                    val volume = quoteData.volume?.get(index)

                    val dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yy hh:mm a")

                    val indexedZoneDateTime = ZonedDateTime.ofInstant(time?.let { Instant.ofEpochSecond(it) }, zoneID)
                    val localZonedDateTime = indexedZoneDateTime.withZoneSameInstant(ZoneId.systemDefault())
                    val formattedLocalZonedDateTime = localZonedDateTime.format(dateTimeFormatter)

                    val formattedOpen = formatDoubleDollar(openPrice)
                    val formattedHigh = formatDoubleDollar(highPrice)
                    val formattedLow = formatDoubleDollar(lowPrice)
                    val formattedClose = formatDoubleDollar(closePrice)
                    val formattedVolume = formatLargeNumber(volume)

                    activity?.apply {
                        findViewById<ConstraintLayout>(R.id.advancedStockFragment_chart_details_constraintLayout).show()

                        findViewById<TextView>(R.id.advancedStockFragment_chart_details_left_open_textView)?.text = formattedOpen
                        findViewById<TextView>(R.id.advancedStockFragment_chart_details_right_high_textView)?.text = formattedHigh
                        findViewById<TextView>(R.id.advancedStockFragment_chart_details_left_low_textView)?.text = formattedLow
                        findViewById<TextView>(R.id.advancedStockFragment_chart_details_right_close_textView)?.text = formattedClose
                        findViewById<TextView>(R.id.advancedStockFragment_chart_details_left_volume_textView)?.text = formattedVolume
                        findViewById<TextView>(R.id.advancedStockFragment_chart_details_right_time_textView)?.text = formattedLocalZonedDateTime
                    }


                } else {
                    paused = false
                    activity?.findViewById<ConstraintLayout>(R.id.advancedStockFragment_chart_details_constraintLayout)?.hide()

                    updatePrice()
                    setChange()
                }

                activity?.findViewById<SwipeRefreshLayout>(R.id.advancedStockFragment_swipeRefreshLayout)?.isEnabled = !paused
/*
                if (index == null) {
                    paused = false
                    activity?.findViewById<SwipeRefreshLayout>(R.id.advancedStockFragment_swipeRefreshLayout)?.isEnabled =
                        true
                    view.apply {
                        updatePrice()
                        setChange()
                    }
                } else {
                    paused = true
                    activity?.findViewById<SwipeRefreshLayout>(R.id.advancedStockFragment_swipeRefreshLayout)?.isEnabled =
                        false
                    view.apply {
                        val scrubPrice = (index as Int).toDouble()
                        findViewById<TickerView>(R.id.advancedStockFragment_price_tickerView)?.setText(
                            formatDoubleDollar(scrubPrice),
                            false
                        )
                        val scrubChange =
                            previousClosePrice?.let { calculateChange(scrubPrice, it) }
                        val scrubPercentage =
                            scrubChange?.let { calculatePercentage(it, scrubPrice) }
                        val changeText =
                            "${formatDoubleDollar(scrubChange?.let { abs(it) })} (${
                                formatDouble(scrubPercentage?.let { abs(it) })
                            }%)"
                        findViewById<TickerView>(R.id.advancedStockFragment_change_tickerView)?.setText(
                            changeText,
                            false
                        )

                        val assumedColor = scrubChange?.let { getRawColor(it) }
                        if (polarityColor != assumedColor && assumedColor != null) {
                            setPolarityTheme(assumedColor)
                            activity?.findViewById<ImageView>(R.id.advancedStockFragment_change_imageView)?.apply {
                                setImageDrawable(context?.let { getChangeDrawable(it, scrubChange) })
                                (drawable as AnimatedVectorDrawable).start()
                            }
                        }
                    }
                }*/
            }
        }


        // TODO move this to view.apply
        view.findViewById<TextView>(R.id.advancedStockFragment_timeRangeSelector_1D_textView).apply {
            setTextColor(highlightColor)
            background = ContextCompat.getDrawable(context, R.drawable.background_stock_offline)
            background.setTint(ContextCompat.getColor(context, polarityColor))

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
                    getString(R.string.Placeholder_Price),
                    resources.getFont(R.font.roboto_condensed),
                    Defaults.tickerDefaultAnimation
                )
            }

            for (id in AdvancedStockLayoutIDs.bidAskTickerViews) {
                initializeTicker(
                    findViewById(id),
                    getString(R.string.Placeholder_BidAsk),
                    resources.getFont(R.font.roboto_condensed),
                    Defaults.tickerDefaultAnimation
                )
            }


            findViewById<TickerView>(R.id.advancedStockFragment_change_tickerView).text =
                getString(R.string.Placeholder_Change)
            findViewById<TickerView>(R.id.advancedStockFragment_quickStatistics_volume_tickerView).text =
                getString(R.string.Placeholder_Double)
            findViewById<TickerView>(R.id.advancedStockFragment_trade_statistics_units_tickerView).text =
                getString(R.string.Placeholder_Double)
            findViewById<TickerView>(R.id.advancedStockFragment_trade_statistics_portfolioMakeup_tickerView).text =
                getString(R.string.Placeholder_DoublePercentage)
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

        activity?.findViewById<BottomNavigationView>(R.id.main_bottomNavigationView)?.menu?.findItem(R.id.menu_bottomNavigationView_search)?.isChecked = true
        activity?.findViewById<ScrollView>(R.id.advancedStockFragment_scrollView)?.apply {
            viewTreeObserver?.addOnScrollChangedListener(FloatingScrollView(activity, this).listener)
        }

        queue = context?.let { RequestSingleton(it) }

        // Initialize
        val requests = listOf(
            getHistoricalDataRequest(true, true, true),
            getFinancialDataRequest(),
            getOneTimeDataRequest(),
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
                else -> ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.rounded_stroke_offline
                )
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
        listOf(
            getOneTimeDataRequest(),
            getNewsDataRequest(),
            getFinancialDataRequest(),
            getHistoricalDataRequest(
                true,
                financialDataRequestErrorCount >= financialDataRequestErrorLimit,
                true
            )
        ).forEach { request ->
            queue?.addToRequestQueue(request)
        }
    }

    @SuppressLint("SetTextI18n") // TODO remove
    private fun getOneTimeDataRequest() = apiInterface.getOneTimeData({ response ->
        response.parseAssetProfile()?.apply {
            activity?.findViewById<TextView>(R.id.advancedStockFragment_about_summary_textView)?.text = try {
                longBusinessSummary?.substring(0, Defaults.aboutSummaryLimit) + "…"
            } catch (error: StringIndexOutOfBoundsException) {
                longBusinessSummary
            }
            activity?.findViewById<TextView>(R.id.advancedStockFragment_about_readMore_textView)?.setOnClickListener {
                activity?.findViewById<TextView>(R.id.advancedStockFragment_about_summary_textView)?.text = longBusinessSummary
                it.hide()
            }
        }

        response.parseEarningsHistory()?.apply {
            rawEpsActualList.clear()
            rawEpsEstimateList.clear()

            history.forEachIndexed { index, value ->
                value?.apply {
                    activity?.findViewById<TextView>(AdvancedStockLayoutIDs.earningsDate[index])?.text = quarter?.fmt

                    val bubbleX = BubbleConfiguration.bubbleX(index)
                    val bubbleSize = BubbleConfiguration.size

                    val rawEpsActual = epsActual?.raw
                    val rawEpsEstimate = epsEstimate?.raw
                    if (rawEpsActual != null && rawEpsEstimate != null) {
                        rawEpsActualList.add(BubbleEntry(bubbleX, rawEpsActual.toFloat(), bubbleSize))
                        rawEpsEstimateList.add(BubbleEntry(bubbleX, rawEpsEstimate.toFloat(), bubbleSize))
                    }

                    val formattedEpsActual = formatDouble(rawEpsActual)
                    val rawEpsDifference = rawEpsEstimate?.let { rawEpsActual?.minus(it) }
                    val formattedEpsDifference = formatDouble(rawEpsDifference?.let { abs(it) })
                    activity?.findViewById<TextView>(AdvancedStockLayoutIDs.earningsChange[index])?.apply {
                        text = "$formattedEpsActual ($formattedEpsDifference)" // todo put via strings xml
                        rawEpsDifference?.let { setTextColor(getColor(context, it)) }
                    }
                }
            }

            context?.let { ContextCompat.getColor(it, polarityColor) }?.let { updateBubbleCharts(it) }
        } ?: run {
            activity?.findViewById<ConstraintLayout>(R.id.advancedStockFragment_earnings_constraintLayout)?.hide()
        }

        response.parseRecommendationTrend()?.apply {
            trend.firstOrNull()?.apply {
                val buySum = buy?.let { strongBuy?.plus(it) } ?: strongBuy
                val sellSum = sell?.let { strongSell?.plus(it) } ?: strongSell
                val totalRatings = buySum?.let { sellSum?.plus(it) } ?: sellSum

                val analystData = mapOf(
                    getString(R.string.Static_Buy) to totalRatings?.let { ntr -> (buySum?.times(100))?.div(ntr)?.let { AnalystData(buySum, it) } },
                    getString(R.string.Static_Hold) to totalRatings?.let { ntr -> (hold?.times(100))?.div(ntr)?.let { AnalystData(hold, it) } },
                    getString(R.string.Static_Sell) to totalRatings?.let { ntr -> (sellSum?.times(100))?.div(ntr)?.let { AnalystData(sellSum, it) } }
                )

                analystData.onEachIndexed { index, entry ->
                    activity?.findViewById<TextView>(AdvancedStockLayoutIDs.analystRatingsTextViews[index])?.text =
                        "${entry.key}: ${entry.value?.amount} (${entry.value?.percentage}%)"
                    entry.value?.percentage?.let { activity?.findViewById<ProgressBar>(AdvancedStockLayoutIDs.analystRatingsProgressBars[index])?.progress = it }
                }

                val filteredAnalystData = analystData.filterNotNullValues()
                val highestPercent = filteredAnalystData.maxByOrNull { it.value.percentage }

                activity?.apply {
                    findViewById<TextView>(R.id.advancedStockFragment_analystRating_rating_textView)?.text = highestPercent?.key
                    findViewById<TextView>(R.id.advancedStockFragment_analystRating_circle_percentage_textView)?.text = "${highestPercent?.value?.percentage}%"
                    findViewById<TextView>(R.id.advancedStockFragment_analystRating_circle_total_textView)?.text = "$totalRatings total ratings"
                }
            }
        } ?: run {
            activity?.findViewById<ConstraintLayout>(R.id.advancedStockFragment_analystRating_constraintLayout)?.hide()
        }
    }) { error ->

    }

    private fun getNewsDataRequest() = apiInterface.getSymbolNewsData({ response ->
        Log.d(TAG, "NewsDataRequest [SUCCESS]")

        newsList.clear()
        response.getElement<JSONArray>("items")?.toList<JSONObject>()?.forEach { items ->
            items?.apply {
                val title = getElement<String>("title")
                val pubDate = getElement<String>("pubDate")
                val link = getElement<String>("link")
                val content = getElement<String>("content")?.let {
                    HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                }

                if (title != null && pubDate != null && link != null && content != null) {
                    newsList.add(NewsResponseData(title, pubDate, link, content))
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

        val financialDataResponse = response.parseFinanceResponse()

        currentMarketPrice = financialDataResponse?.currentPrice?.raw
        updatePrice()

        if (currentMarketPrice != null && previousClosePrice != null) {
            change = calculateChange(currentMarketPrice!!, previousClosePrice!!)
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

            /* TODO fix l8ter supposed to update data
        sparkData.apply {
            yData[yData.size - 1] = currentMarketPrice!!
            sparkDataNotifyChanged(false)
        }*/
        }

        val oneYearTargetEstimate = financialDataResponse?.targetMeanPrice?.raw

        activity?.findViewById<TickerView>(R.id.advancedStockFragment_quickStatistics_oneYearTargetEst_tickerView)?.setText(formatDoubleDollar(oneYearTargetEstimate), true)
    }, { error -> // todo build custom log function
        financialDataRequestErrorCount++
    })

    private fun getHistoricalDataRequest(
        firstTime: Boolean,
        financialDataRequestEnabled: Boolean,
        animate: Boolean
    ) = apiInterface.historical({ response ->
        historicalDataRequestCount++
        Log.d(TAG, "HistoricalDataRequest [SUCCESS] Count: $historicalDataRequestCount")

        response.parseHistoricalData()?.apply {
            meta?.apply {
                previousClosePrice = chartPreviousClose
                // todo create updateTickerFunction in MarketInterface
                activity?.findViewById<TickerView>(R.id.advancedStockFragment_quickStatistics_chartPreviousClose_tickerView)?.setText(formatDoubleDollar(chartPreviousClose), true)

                if (firstTime || !financialDataRequestEnabled) {
                    currentMarketPrice = regularMarketPrice
                    change = calculateChange(currentMarketPrice, chartPreviousClose)

                    updatePrice()
                    setChange()

                    // Ranges Logic
                    AdvancedStockLayoutIDs.timeRanges.forEach { (key, value) ->
                        activity?.findViewById<TextView>(value)?.visibility = if (validRanges?.contains(key) == true) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    }
                }

                // Padding Setter
                zoneID = ZoneId.of(exchangeTimezoneName)
                val currentEpochTime = ZonedDateTime.now(zoneID).toInstant().epochSecond
                val preStart = currentTradingPeriod?.pre?.start
                val postEnd = currentTradingPeriod?.post?.end
                if (preStart != null && postEnd != null && currentEpochTime < postEnd) {
                    val elapsedTime = currentEpochTime - preStart
                    val totalElapsedTime = postEnd - preStart
                    val timeElapsedRatio = elapsedTime.toDouble() / totalElapsedTime.toDouble()

                    activity?.findViewById<SparkView>(R.id.advancedStockFragment_chart_sparkView)?.apply {
                        val elapsedToPadding = width - (width * timeElapsedRatio)
                        setPadding(paddingLeft, paddingTop, elapsedToPadding.roundToInt(), paddingBottom)
                    }
                }
            }

            timeStampData = timestamp ?: listOf()

            indicators?.quote?.apply {
                quoteData = this
                val totalVolume = volume?.filterNotNull()?.sum()
                val formattedVolume = formatLargeNumber(totalVolume)
                activity?.findViewById<TickerView>(R.id.advancedStockFragment_quickStatistics_volume_tickerView)?.setText(formattedVolume, true)

                val filteredClose = close?.filterNotNull()

                sparkData.apply {
                    yData = filteredClose ?: listOf()
                    chartPreviousClose = previousClosePrice ?: 0.0
                }
                sparkDataNotifyChanged(animate)

                val chartLow = formatDoubleDollar(filteredClose?.minOrNull())
                val chartHigh = formatDoubleDollar(filteredClose?.maxOrNull())
                activity?.apply {
                    findViewById<TickerView>(R.id.advancedStockFragment_quickStatistics_chartHigh_tickerView).setText(chartHigh, true)
                    findViewById<TickerView>(R.id.advancedStockFragment_quickStatistics_chartLow_tickerView).setText(chartLow, true)
                }

            } ?: run {
                quoteData = Quote(null, null, null, null, null)
            }

        } ?: run {

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

    private fun updatePrice() {
        activity?.findViewById<TickerView>(R.id.advancedStockFragment_price_tickerView)
            ?.setText(formatDoubleDollar(currentMarketPrice), true)
        activity?.findViewById<TickerView>(R.id.advancedStockFragment_floatingScrollView_price_tickerView)
            ?.setText(formatDoubleDollar(currentMarketPrice), true)
    }

    private fun setChange() {
        val changeText = "${formatDoubleDollar(change?.let { abs(it) })} (${
            formatDouble(percentage?.let { abs(it) })
        }%)"

        activity?.findViewById<TickerView>(R.id.advancedStockFragment_change_tickerView)
            ?.setText(changeText, true)
        activity?.findViewById<TickerView>(R.id.advancedStockFragment_floatingScrollView_change_tickerView)
            ?.setText(changeText, true)
    }

    private fun updateBubbleCharts(interpretedColor: Int) {
        val actualEarningsSet = BubbleDataSet(rawEpsActualList, "").apply {
            isNormalizeSizeEnabled = false
            color = interpretedColor
        }
        val estimateEarningsSet = BubbleDataSet(rawEpsEstimateList, "").apply {
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
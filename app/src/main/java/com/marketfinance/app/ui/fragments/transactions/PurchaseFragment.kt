package com.marketfinance.app.ui.fragments.transactions

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.marketfinance.app.R
import com.marketfinance.app.ui.fragments.advancedStockFragment.ValidIntervals
import com.marketfinance.app.ui.fragments.advancedStockFragment.data.AdvancedStockIntentData
import com.marketfinance.app.ui.fragments.transactions.market.MarketOrderPurchaseFragment
import com.marketfinance.app.utils.Defaults
import com.marketfinance.app.utils.interfaces.Calculations
import com.marketfinance.app.utils.interfaces.FragmentTransactions
import com.marketfinance.app.utils.interfaces.MarketInterface
import com.marketfinance.app.utils.network.RequestSingleton
import com.marketfinance.app.utils.network.wrappers.APIWrapper
import com.marketfinance.app.utils.threads.ThreadManager

class PurchaseFragment : Fragment(), Calculations, FragmentTransactions, MarketInterface {

    private val TAG = "PurchaseFragment"

    private val gson = Gson()
    private val threadManager = ThreadManager()
    private val apiInterface = APIWrapper("", ValidIntervals.Spark.ONE_DAY)

    private var symbol = ""
    private var name = ""
    private var quoteType = ""
    private var jsonData = ""
    private var sparkRequestCount = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_purchase, container, false)

        jsonData = arguments?.getString("json")!!
        val advancedStockIntentData = gson.fromJson(jsonData, AdvancedStockIntentData::class.java)

        symbol = advancedStockIntentData.symbol
        name = advancedStockIntentData.name
        quoteType = advancedStockIntentData.quoteType

        apiInterface.symbol = symbol

        view.apply {

            findViewById<TextView>(R.id.purchase_header_name_textView).text = symbol

            initializeTicker(
                findViewById(R.id.purchase_header_details_tickerView),
                getString(R.string.Placeholder_SymbolPrice),
                resources.getFont(R.font.roboto_condensed),
                Defaults.tickerDefaultAnimation
            )


        }

        return view
    }

    override fun onActivityCreated(
        savedInstanceState: Bundle?
    ) {
        super.onActivityCreated(savedInstanceState)

        activity?.findViewById<ImageView>(R.id.purchase_header_back_imageView)
            ?.setOnClickListener {
                threadManager.killAllThreads()
                activity?.supportFragmentManager?.popBackStack()
            }

        activity?.apply {
            val fragmentAnimations = FragmentTransactions.FragmentAnimations(
                R.anim.fragment_child_enter, R.anim.fragment_parent_exit,
                R.anim.fragment_parent_enter, R.anim.fragment_child_exit
            )

            findViewById<ConstraintLayout>(R.id.purchase_marketOrder_constraintLayout).setOnClickListener {
                val marketOrderPurchaseFragment =
                    attachJSONtoFragment(MarketOrderPurchaseFragment(), jsonData)
                replaceFragment(
                    marketOrderPurchaseFragment,
                    supportFragmentManager.beginTransaction(),
                    fragmentAnimations,
                    false
                )
                threadManager.killAllThreads()
            }
        }

        val queue = context?.let { RequestSingleton.getInstance(it) }

        threadManager.createThread("SparkThread", Thread {
            val request = apiInterface.spark({ response ->
                sparkRequestCount++
                Log.d(TAG, "[REQUEST] Code 200. Request for $symbol Count: $sparkRequestCount.")

                /*
                val spark = try {
                    response.getJSONObject("spark")
                } catch (error: JSONException) {
                    JSONExceptionHandler.jsonObject("spark")
                }
                val result = try {
                    spark.getJSONArray("result")?.getJSONObject(0)
                } catch (error: JSONException) {
                    JSONExceptionHandler.jsonObject("result")
                }
                val mResponse = try {
                    result?.getJSONArray("response")?.getJSONObject(0)
                } catch (error: JSONException) {
                    JSONExceptionHandler.jsonObject("response")
                }
                val meta = try {
                    mResponse?.getJSONObject("meta")
                } catch (error: JSONException) {
                    JSONExceptionHandler.jsonObject("meta")
                }
                val currentPrice = try {
                    meta?.getDouble("regularMarketPrice")
                } catch (error: JSONException) {
                    JSONExceptionHandler.double("regularMarketPrice")
                }
                val previousClose = try {
                    meta?.getDouble("previousClose")
                } catch (error: JSONException) {
                    JSONExceptionHandler.double("previousClose")
                }

                activity?.findViewById<TickerView>(R.id.purchase_header_details_tickerView)
                    ?.setText(
                        "$name • ${formatDoubleDollar(currentPrice)}", true
                    )

                if (currentPrice != null && previousClose != null && context != null) {
                    val change = calculateChange(currentPrice, previousClose)
                    setPolarity(getRawColor(change))
                }

                 */

            }, { error ->

            })

            while (!Thread.interrupted()) {
                try {
                    queue?.addToRequestQueue(request)
                    Thread.sleep(Defaults.priceAPIFrequency)
                } catch (error: InterruptedException) {
                    Log.e(TAG, error.stackTraceToString())
                    break
                }
            }
        })

    }

    override fun onPause() {
        super.onPause()

        threadManager.killAllThreads()
    }

    override fun onResume() {
        super.onResume()

        threadManager.startAllThreads()
    }

    private fun setPolarity(colorID: Int) {

        val interpretedColor = context?.let { ContextCompat.getColor(it, colorID) }

        if (interpretedColor != null) {

        }


    }

}
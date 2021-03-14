package com.marketfinance.app.ui.fragments.transactions.market

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.marketfinance.app.R
import com.marketfinance.app.ui.fragments.advancedStockFragment.data.AdvancedStockIntentData
import com.marketfinance.app.ui.fragments.transactions.TransactionLayoutIDs
import com.marketfinance.app.utils.Defaults
import com.marketfinance.app.utils.MarketInterface
import com.marketfinance.app.utils.network.APIWrapper
import com.marketfinance.app.utils.network.RequestSingleton
import com.marketfinance.app.utils.threads.ThreadManager

class MarketOrderPurchaseFragment : Fragment(), MarketInterface {

    private val TAG = "MarketOrderPurchaseFragment"

    private val apiInterface = APIWrapper("", null)
    private val gson = Gson()
    private val threadManager = ThreadManager()

    private var symbol = ""
    private var name = ""
    private var quoteType = ""
    private var jsonData = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_market_order_purchase, container, false)

        jsonData = arguments?.getString("json")!!
        val advancedStockIntentData = gson.fromJson(jsonData, AdvancedStockIntentData::class.java)

        symbol = advancedStockIntentData.symbol
        name = advancedStockIntentData.name
        quoteType = advancedStockIntentData.quoteType

        view.apply {
            findViewById<EditText>(R.id.marketOrderPurchase_units_editText).apply {
                addTextChangedListener(MarketInterface.NumberTextWatcherForThousand(this))
            }

            for (id in TransactionLayoutIDs.Purchase.marketOrderPurchaseBidAskTickerViews) {
                initializeTicker(
                    findViewById(id),
                    getString(R.string.default_bidAsk),
                    resources.getFont(R.font.roboto_condensed),
                    Defaults.tickerDefaultAnimation
                )
            }
        }


        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val queue = context?.let { RequestSingleton.getInstance(it) }

        threadManager.createThread("OptionsThread", Thread {
            while (!Thread.interrupted()) {
                try {
                    queue?.addToRequestQueue(getOptionsRequest())
                    Thread.sleep(Defaults.optionsAPIFrequency)
                } catch (error: InterruptedException) {
                    Log.e(TAG, error.stackTraceToString())
                    break
                }
            }
        })
    }

    private fun getOptionsRequest() = apiInterface.getOptionsData({ response ->
        Log.d(TAG, "[REQUEST] OptionsRequest C200")

        /*
        val optionChain = try {
            response.getJSONObject("optionChain")
        } catch (error: JSONException) {
            JSONExceptionHandler.jsonObject("optionChain")
        }
        val result = try {
            optionChain.getJSONArray("result")?.getJSONObject(0)
        } catch (error: JSONException) {
            JSONExceptionHandler.jsonObject("result")
        }
        val quote = try {
            result?.getJSONObject("quote")
        } catch (error: JSONException) {
            JSONExceptionHandler.jsonObject("quote")
        }

        val oneDayRange = try {
            result?.getString("regularMarketDayRange")
        } catch (error: JSONException) {
            JSONExceptionHandler.string("regularMarketDayRange")
        }
        val fiftyTwoRange = try {
            result?.getString("fiftyTwoWeekRange")
        } catch (error: JSONException) {
            JSONExceptionHandler.string("fiftyTwoWeekRange")
        }

        val bidPrice = try {
            quote?.getDouble("bid")
        } catch (error: JSONException) {
            JSONExceptionHandler.double("bid")
        }
        val askPrice = try {
            quote?.getDouble("ask")
        } catch (error: JSONException) {
            JSONExceptionHandler.double("ask")
        }
        val bidSize = try {
            quote?.getInt("bidSize")
        } catch (error: JSONException) {
            JSONExceptionHandler.int("bidSize")
        }
*/

    }, { error ->

    })

}
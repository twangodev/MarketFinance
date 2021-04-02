package com.marketfinance.app.ui.fragments.transactions.market

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.marketfinance.app.R
import com.marketfinance.app.ui.fragments.advancedStockFragment.data.AdvancedStockIntentData
import com.marketfinance.app.ui.fragments.transactions.TransactionLayoutIDs
import com.marketfinance.app.utils.Defaults
import com.marketfinance.app.utils.interfaces.MarketInterface
import com.marketfinance.app.utils.network.RequestSingleton
import com.marketfinance.app.utils.network.wrappers.APIWrapper
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
                    getString(R.string.Placeholder_BidAsk),
                    resources.getFont(R.font.roboto_condensed),
                    Defaults.tickerDefaultAnimation
                )
            }

            findViewById<Button>(R.id.marketOrderPurchase_confirmPurchase_button).setOnClickListener {

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

                    Thread.sleep(Defaults.optionsAPIFrequency)
                } catch (error: InterruptedException) {
                    Log.e(TAG, error.stackTraceToString())
                    break
                }
            }
        })
    }



}
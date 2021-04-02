package com.marketfinance.app.ui.fragments.core.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.marketfinance.app.R
import com.marketfinance.app.ui.fragments.advancedStockFragment.MarketStatusReturn
import com.marketfinance.app.utils.Defaults
import com.marketfinance.app.utils.interfaces.MarketInterface
import com.marketfinance.app.utils.network.RequestSingleton
import com.marketfinance.app.utils.storage.PortfolioData
import com.marketfinance.app.utils.threads.ThreadManager
import com.marketfinance.app.utils.transactions.PortfolioManager

class DashboardFragment : Fragment(), MarketInterface, PortfolioManager {

    private val TAG = "DashboardFragment"

    private val gson = Gson()
    private val threadManager = ThreadManager()

    private var currentPortfolio: PortfolioData? = null
    private var marketStatusRequestCount = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        initializeTicker(
            view.findViewById(R.id.dashboard_papertrade_price_tickerView),
            getString(R.string.Placeholder_Price),
            resources.getFont(R.font.roboto_condensed),
            Defaults.tickerDefaultAnimation
        )
        initializeTicker(
            view.findViewById(
                R.id.dashboard_papertrade_realizedChange_tickerView
            ),
            getString(R.string.Placeholder_Change),
            resources.getFont(R.font.roboto_condensed),
            Defaults.tickerDefaultAnimation
        )
        initializeTicker(
            view.findViewById(
                R.id.dashboard_papertrade_unrealizedChange_tickerView
            ),
            getString(R.string.Placeholder_Change),
            resources.getFont(R.font.roboto_condensed),
            Defaults.tickerDefaultAnimation
        )

        view.findViewById<ConstraintLayout>(R.id.dashboard_portfolio_clickLayer_constraintLayout)
            .setOnClickListener {
                // TODO lead to PortfolioManageFragment
            }
        view.findViewById<ConstraintLayout>(R.id.dashboard_settings_clickLayer_constraintLayout)
            .setOnClickListener {
                // TODO lead to settings manage fragment
            }


        currentPortfolio = getActivePortfolio(context)
        Log.d(TAG, "Current Portfolio: $currentPortfolio")
        view.findViewById<TextView>(R.id.dashboard_currentPortfolio_textView).text = getString(
            R.string.Dynamic_CurrentPortfolio,
            currentPortfolio!!.portfolioName
        )

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.findViewById<BottomNavigationView>(R.id.main_bottomNavigationView)?.menu?.findItem(
            R.id.menu_bottomNavigationView_dashboard
        )?.isChecked = true

        val queue = context?.let { RequestSingleton.getInstance(it) }

        val request = JsonObjectRequest(
            Request.Method.GET,
            "https://finance.yahoo.com/_finance_doubledown/api/resource/finance.market-time",
            null,
            { response ->
                marketStatusRequestCount++
                Log.d(
                    TAG,
                    "[NETWORK] Code 200. Request for Market Status Count: $marketStatusRequestCount"
                )
                val status = response.getString("status")
                val message = response.getString("message")

                val textColor = when (status) {
                    MarketStatusReturn.OPEN -> ContextCompat.getColor(
                        requireContext(),
                        R.color.stock_positive
                    )
                    MarketStatusReturn.CLOSED -> ContextCompat.getColor(
                        requireContext(),
                        R.color.stock_negative
                    )
                    else -> ContextCompat.getColor(requireContext(), R.color.stock_offline)
                }

                activity?.findViewById<TextView>(R.id.dashboard_marketStatus_textView)
                    ?.apply {
                        text = message
                        setTextColor(textColor)
                    }


            },
            { e ->
                activity?.findViewById<TextView>(R.id.dashboard_marketStatus_textView)
                    ?.apply {
                        text = getString(R.string.Static_ConnectionError)
                        setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.stock_offline
                            )
                        )
                    }
                e.printStackTrace()
            }
        )

        threadManager.createThread("MarketStatusThread", Thread {
            while (!Thread.interrupted()) {
                try {
                    queue?.addToRequestQueue(request)
                    Thread.sleep(Defaults.marketStatusFrequency)
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

    override fun onStop() {
        super.onStop()

        threadManager.killAllThreads()
    }

}
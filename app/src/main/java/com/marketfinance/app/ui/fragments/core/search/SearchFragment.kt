package com.marketfinance.app.ui.fragments.core.search

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.marketfinance.app.R
import com.marketfinance.app.ui.fragments.advancedStockFragment.ValidIntervals
import com.marketfinance.app.utils.Calculations
import com.marketfinance.app.utils.Defaults
import com.marketfinance.app.utils.network.APIWrapper
import com.marketfinance.app.utils.network.RequestSingleton
import com.marketfinance.app.utils.network.SearchWrapper
import com.marketfinance.app.utils.network.parser.JSONObjectParser

class SearchFragment : Fragment(), Calculations, JSONObjectParser, SearchWrapper {

    private val TAG = "SearchFragment"

    private val searchResults = mutableListOf<SearchResultData>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        view.findViewById<SearchView>(R.id.search_searchBar_searchView).apply {
            (findViewById<View>(
                resources.getIdentifier("android:id/search_src_text", null, null)
            ) as TextView).apply {
                typeface = resources.getFont(R.font.roboto_condensed)
                textSize = 14F
                setTextColor(ContextCompat.getColor(context, R.color.theme_colorOnPrimary))
            }
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.findViewById<BottomNavigationView>(R.id.main_bottomNavigationView)?.menu?.findItem(
            R.id.menu_bottomNavigationView_search
        )?.isChecked = true

        searchResults.clear()
        searchResults.add(
            SearchResultData(
                null,
                "",
                "",
                0.00,
                0.00,
                0.00,
                getString(R.string.search_error_emptyQuery)
            )
        )
        activity?.findViewById<RecyclerView>(R.id.search_searchResults_recyclerView)?.adapter =
            SearchRecyclerViewAdapter(searchResults, activity)

        activity?.findViewById<SearchView>(R.id.search_searchBar_searchView)?.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?): Boolean {
                    Log.d(TAG, "SearchView Updated Query: $newText")
                    if (TextUtils.isEmpty(newText)) {
                        searchResults.clear()
                        searchResults.add(
                            SearchResultData(
                                null,
                                "",
                                "",
                                0.00,
                                0.00,
                                0.00,
                                getString(R.string.search_error_emptyQuery)
                            )
                        )
                        activity?.findViewById<RecyclerView>(R.id.search_searchResults_recyclerView)
                            ?.apply {
                                adapter?.notifyDataSetChanged()
                                scheduleLayoutAnimation()
                            }
                    } else {
                        if (newText != null) {
                            performSearchQuery(newText)
                        }
                    }
                    return true
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    // TODO
                    return true
                }
            })
        }
    }

    private data class SymbolDetails(
        val symbol: String,
        val type: String
    )

    /**
     * Handles and animates the [RecyclerView]
     */
    private fun layoutDataChanged() {
        activity?.findViewById<RecyclerView>(R.id.search_searchResults_recyclerView)?.apply {
            adapter?.notifyDataSetChanged()
            scheduleLayoutAnimation()
        }
    }

    /**
     * Handles Errors thrown from [JsonObjectRequest] relative to search
     *
     * @param message A custom message for the most likely reason
     * @param error The [Throwable] from the [JsonObjectRequest]
     */
    private fun produceError(message: String, error: Throwable) {
        Log.d(TAG, message, error)
        searchResults.add(
            SearchResultData(
                null, "", "", 0.00, 0.00, 0.00,
                message
            )
        )
        layoutDataChanged()
    }


    private fun performSearchQuery(query: String) {
        val queue = context?.let { RequestSingleton.getInstance(it) }
        searchResults.clear()
        context?.let { RequestSingleton.getInstance(it) }?.addToRequestQueue(
            createSearch(query, { searchResponse ->
                val items = searchResponse.parseSearchDataResponse()?.items
                items?.forEach { item ->
                    if (item?.symbol != null && item.typeDisp != null && item.name != null && item.typeDisp in Defaults.searchQuoteTypeFilters) {
                        queue?.addToRequestQueue(APIWrapper(item.symbol, ValidIntervals.Spark.ONE_DAY).spark({ sparkResponse ->
                            sparkResponse.parseSparkResponse()?.historicalData?.meta?.apply {
                                if (regularMarketPrice != null && chartPreviousClose != null) {
                                    val change = calculateChange(regularMarketPrice, chartPreviousClose)!!
                                    searchResults.add(
                                        SearchResultData(
                                            item.symbol, item.typeDisp, item.name, regularMarketPrice,
                                            change, calculatePercentage(change, regularMarketPrice)!!, null
                                        )
                                    )
                                    layoutDataChanged()
                                }
                            }
                        }, { error ->
                            produceError(getString(R.string.default_error_server), error)
                        }))
                    }
                }
                if (items?.size == 0) {
                    val errorMessage = getString(R.string.search_error_noResults)
                    produceError(errorMessage, Error(errorMessage))
                }

            }, { error ->
                produceError(getString(R.string.default_error_connection), error)
            })
        )


        /*
        val url = "https://query$serverNumber.finance.yahoo.com/v1/finance/search?q=$query"
        val request = JsonObjectRequest(Request.Method.GET, url, null, { response ->
            Log.d(TAG, "Search Query API responded $response")
            searchResults.clear()

            val quotes = response.getJSONArray("quotes")
            for (i in 0 until quotes.length()) {
                val element = quotes.getJSONObject(i)
                val quoteType = element.getString("quoteType")
                if (quoteType in Defaults.searchQuoteTypeFilters) {
                    try {
                        val symbol = element.getString("symbol")
                        val name = element.getString("shortname")
                        queue.addToRequestQueue(
                            APIWrapper( // TODO move as global var
                                symbol,
                                ValidIntervals.Spark.ONE_DAY
                            ).spark({ innerResponse -> // TODO wrap each get in try catch [JSONException]
                                val mResponse = (innerResponse.getJSONObject("spark")
                                    .getJSONArray("result")[0] as JSONObject).getJSONArray("response")[0] as JSONObject
                                val meta = mResponse.getJSONObject("meta")
                                val currentPrice = meta.getDouble("regularMarketPrice")
                                val previousClose = meta.getDouble("previousClose")
                                val change = calculateChange(currentPrice, previousClose)
                                val percentage = calculatePercentage(change, currentPrice)

                                if (change != null && percentage != null) {
                                    searchResults.add(
                                        SearchResultData(
                                            symbol,
                                            quoteType,
                                            name,
                                            currentPrice,
                                            change,
                                            percentage,
                                            null
                                        )
                                    )

                                }

                                activity?.findViewById<RecyclerView>(R.id.search_searchResults_recyclerView)
                                    ?.apply {
                                        adapter?.notifyDataSetChanged()
                                        scheduleLayoutAnimation()
                                    }

                            }, { error ->
                                searchResults.clear()
                                searchResults.add(
                                    SearchResultData(
                                        null,
                                        "",
                                        "",
                                        0.00,
                                        0.00,
                                        0.00,
                                        getString(R.string.default_error_server)
                                    )
                                )
                                activity?.findViewById<RecyclerView>(R.id.search_searchResults_recyclerView)
                                    ?.apply {
                                        adapter?.notifyDataSetChanged()
                                        scheduleLayoutAnimation()
                                    }
                                error.printStackTrace()
                            })
                        )
                    } catch (error: JSONException) {
                        error.printStackTrace()
                    }
                }
            }
            if (quotes.length() == 0) {
                searchResults.clear()
                searchResults.add(
                    SearchResultData(
                        null,
                        "",
                        "",
                        0.00,
                        0.00,
                        0.00,
                        getString(R.string.search_error_noResults)
                    )
                )
                activity?.findViewById<RecyclerView>(R.id.search_searchResults_recyclerView)
                    ?.apply {
                        adapter?.notifyDataSetChanged()
                        scheduleLayoutAnimation()
                    }
            }
        }, { error ->
            searchResults.clear()
            searchResults.add(
                SearchResultData(
                    null,
                    "",
                    "",
                    0.00,
                    0.00,
                    0.00,
                    getString(R.string.default_error_connection)
                )
            )
            activity?.findViewById<RecyclerView>(R.id.search_searchResults_recyclerView)?.apply {
                adapter?.notifyDataSetChanged()
                scheduleLayoutAnimation()
            }
            error.printStackTrace()
            serverNumber = 2
        })
        queue.addToRequestQueue(request)
*/
    }

}
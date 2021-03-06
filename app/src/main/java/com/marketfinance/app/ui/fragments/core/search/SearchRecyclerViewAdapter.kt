package com.marketfinance.app.ui.fragments.core.search

import android.annotation.SuppressLint
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.marketfinance.app.R
import com.marketfinance.app.ui.fragments.advancedStockFragment.AdvancedStockFragment
import com.marketfinance.app.ui.fragments.advancedStockFragment.data.AdvancedStockIntentData
import com.marketfinance.app.utils.FragmentTransactions
import com.marketfinance.app.utils.MarketInterface
import com.marketfinance.app.utils.objects.Defaults
import com.robinhood.ticker.TickerView
import kotlin.math.abs

class SearchRecyclerViewAdapter(
    private val searchResultData: MutableList<SearchResultData>,
    private val activity: FragmentActivity?
) : RecyclerView.Adapter<SearchRecyclerViewAdapter.ViewHolder>(), FragmentTransactions,
    MarketInterface {

    private val TAG = "SearchRecyclerViewAdapter"
    private val gson = Gson()

    init {
        Log.d(TAG, "Initial Given Data: $searchResultData")
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val clickLayer: ConstraintLayout =
            view.findViewById(R.id.searchResults_clickLayer_contraintLayout)
        val symbolTextView: TextView = view.findViewById(R.id.searchResults_symbol_textView)
        val detailsTextView: TextView = view.findViewById(R.id.searchResults_details_textView)
        val priceTickerView: TickerView = view.findViewById(R.id.searchResults_price_tickerView)
        val changeImageView: ImageView = view.findViewById(R.id.searchResults_change_imageView)
        val changeTickerView: TickerView = view.findViewById(R.id.searchResults_change_tickerView)
        val errorTextView: TextView = view.findViewById(R.id.searchResults_error_textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_searchresults, parent, false)

        activity?.resources?.apply {
            initializeTicker(
                view.findViewById(R.id.searchResults_price_tickerView),
                getString(R.string.default_price),
                getFont(R.font.roboto_condensed),
                Defaults.tickerDefaultAnimation
            )
            initializeTicker(
                view.findViewById(R.id.searchResults_change_tickerView),
                getString(R.string.default_change),
                getFont(R.font.roboto_condensed),
                Defaults.tickerDefaultAnimation
            )
        }

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n") // TODO remove
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val element = searchResultData[position]

        holder.apply {
            if (element.symbol != null) {

                clickLayer.setOnClickListener {
                    val advancedStockFragment = attachJSONtoFragment(
                        AdvancedStockFragment(),
                        gson.toJson(
                            AdvancedStockIntentData(
                                element.symbol,
                                element.name,
                                element.quoteType
                            )
                        )
                    )
                    val fragmentAnimations = FragmentTransactions.FragmentAnimations(
                        R.anim.fragment_child_enter, R.anim.fragment_parent_exit,
                        R.anim.fragment_parent_enter, R.anim.fragment_child_exit
                    )
                    activity?.supportFragmentManager?.beginTransaction()?.let { it1 ->
                        replaceFragment(
                            advancedStockFragment,
                            it1,
                            fragmentAnimations,
                            true
                        )
                    }
                }

                symbolTextView.apply {
                    visibility = View.VISIBLE
                    text = element.symbol
                }
                detailsTextView.apply {
                    visibility = View.VISIBLE
                    text = "${element.name} • ${element.quoteType}"
                }
                priceTickerView.apply {
                    visibility = View.VISIBLE
                    setText(formatNullableDoubleWithDollar(element.currentPrice), true)
                    textColor = getColor(context, element.change)
                }
                changeImageView.apply {
                    visibility = View.VISIBLE
                    setImageDrawable(getChangeDrawable(context, element.change))
                    (drawable as AnimatedVectorDrawable).start()
                }
                changeTickerView.apply {
                    visibility = View.VISIBLE
                    setText(
                        "${formatNullableDoubleWithDollar(abs(element.change))} (${
                            formatNullableDouble(
                                abs(element.percentage)
                            )
                        }%)", true
                    )
                    textColor = getColor(context, element.change)
                }
                errorTextView.apply {
                    visibility = View.GONE
                }
            } else {
                symbolTextView.visibility = View.GONE
                detailsTextView.visibility = View.GONE
                priceTickerView.visibility = View.GONE
                changeImageView.visibility = View.GONE
                changeTickerView.visibility = View.GONE

                errorTextView.apply {
                    visibility = View.VISIBLE
                    text = element.errorMessage
                }
            }
        }
    }

    override fun getItemCount(): Int = searchResultData.size

}
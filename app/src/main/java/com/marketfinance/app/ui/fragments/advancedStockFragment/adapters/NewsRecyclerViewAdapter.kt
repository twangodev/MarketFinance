package com.marketfinance.app.ui.fragments.advancedStockFragment.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.marketfinance.app.R
import com.marketfinance.app.utils.objects.Defaults

class NewsRecyclerViewAdapter(
    private val newsListData: MutableList<NewsResponseData?>
) : RecyclerView.Adapter<NewsRecyclerViewAdapter.ViewHolder>() {

    private val TAG = "NewsRecyclerViewAdapter"

    init {
        Log.d(TAG, "InitialDataGiven: $newsListData")
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout: ConstraintLayout = view.findViewById(R.id.news_contraintLayout)
        val title: TextView = view.findViewById(R.id.news_title_textView)
        val content: TextView = view.findViewById(R.id.news_content_textView)
        val timeStamp: TextView = view.findViewById(R.id.news_timeStamp_textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_news, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val element = newsListData[position]

        holder.apply {
            if (element != null) {
                title.text = element.title
                content.text = try {
                    element.content.substring(0, Defaults.newsSubstringLimit) + "…"
                } catch (error: StringIndexOutOfBoundsException) {
                    element.content
                }
                timeStamp.text = element.publishDate
            }
        }
    }

    override fun getItemCount() = newsListData.size

}
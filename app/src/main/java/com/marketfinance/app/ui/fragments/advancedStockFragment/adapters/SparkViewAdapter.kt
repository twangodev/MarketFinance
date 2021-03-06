package com.marketfinance.app.ui.fragments.advancedStockFragment.adapters

import com.robinhood.spark.SparkAdapter

class SparkViewAdapter(private val data: SparkViewData) : SparkAdapter() {

    override fun getCount() = data.yData.size

    override fun getItem(index: Int): Any = data.yData[index]

    override fun getY(index: Int) = data.yData[index].toFloat()

    override fun hasBaseLine() = true

    override fun getBaseLine(): Float {
        return data.chartPreviousClose.toFloat()
    }

}
package com.marketfinance.app.ui.fragments.advancedStockFragment

object BubbleConfiguration {

    const val size = 5F
    const val verticalPadding = 30F
    const val horizontalPadding = 8F

    fun bubbleX(index: Int) = ((index + 1) * 10).toFloat()

}
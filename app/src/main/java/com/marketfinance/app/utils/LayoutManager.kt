package com.marketfinance.app.utils

import android.view.View

/**
 * Simple Layout Manager
 */
interface LayoutManager {

    /**
     * Hides the [View]
     */
    fun View.hide() {
        this.visibility = View.GONE
    }


}
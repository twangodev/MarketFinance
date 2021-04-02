package com.marketfinance.app.utils.interfaces

import android.view.View

/**
 * Simple Layout Manager
 */
interface LayoutManager {

    /**
     * Hides the [View]
     * TODO make fade out
     */
    fun View.hide() {
        visibility = View.GONE
    }

    /**
     * Shows the [View]
     * TODO make fade in
     *
     */
    fun View.show() {
        visibility = View.VISIBLE
    }


}
package com.marketfinance.app.ui.fragments.advancedStockFragment

import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ScrollView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import com.marketfinance.app.R

class FloatingScrollView(
    private val activity: FragmentActivity?,
    private val scrollView: ScrollView?
) {

    private val scrollThreshold = 1325
    private var active = false
    private var firstTime = true

    init {
        getConstraintLayout()?.apply {
            visibility = View.INVISIBLE
            startAnimation(animationOut())
        }
    }

    private fun animationIn() = AnimationUtils.loadAnimation(activity, R.anim.top_enter).apply {
        fillBefore = true
        fillAfter = true
    }

    private fun animationOut() = AnimationUtils.loadAnimation(activity, R.anim.top_exit).apply {
        fillBefore = true
        fillAfter = true
    }

    private fun getConstraintLayout() =
        activity?.findViewById<ConstraintLayout>(R.id.advancedStockFragment_floatingScrollView_constraintLayout)


    val listener = ViewTreeObserver.OnScrollChangedListener {
        val currentPosition = scrollView?.scrollY
        if (currentPosition != null) {
            if (currentPosition > scrollThreshold) {
                getConstraintLayout()?.visibility = View.VISIBLE
                if (!active) {
                    active = true
                    animate(animationIn())
                }
            } else if (active) {
                active = false
                animate(animationOut())
            }
        }
    }

    private fun animate(animation: Animation) {
        getConstraintLayout()?.apply {
            startAnimation(animation)
        }
    }

}
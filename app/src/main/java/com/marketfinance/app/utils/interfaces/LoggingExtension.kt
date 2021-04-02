package com.marketfinance.app.utils.interfaces

import androidx.fragment.app.FragmentActivity

interface LoggingExtension {

    fun FragmentActivity.verbose() {

    }

    fun FragmentActivity.debug()

    fun FragmentActivity.info()

    fun FragmentActivity.warning()

    fun FragmentActivity.error() {

    }

    fun FragmentActivity.fatal() {

    }

}
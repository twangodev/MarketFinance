package com.marketfinance.app.utils.interfaces

interface MapExtensions {

    fun <K, V> Map<K, V?>.filterNotNullValues(): Map<K, V> =
        mapNotNull { (key, value) -> value?.let { key to it } }.toMap()

}
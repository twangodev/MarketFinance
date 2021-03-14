package com.marketfinance.app.utils.network

import android.util.Log
import com.marketfinance.app.utils.network.JSONArrayWrapper.Companion.toList
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Wrapper for handling a [JSONArray] with `null` processing and logging
 *
 * @author James Ding
 */
class JSONArrayWrapper {

    companion object {

        const val TAG = "JSONArrayWrapper"

        fun JSONArray.firstJSONObject() = this.getJSONObject(0)

        @Suppress("IMPLICIT_CAST_TO_ANY")
        /**
         * Retrieves [T] to with [index]
         *
         * @param T Supports [JSONObject], [JSONArray], [String], [Int], [Long], [Double]
         * @param index The index that points to [T] in the [JSONArray]
         * @return The [T] with the specified [index]
         * @author James Ding
         */
        inline fun <reified T> JSONArray.getElement(index: Int): T? {
            return try {
                when (T::class) {
                    JSONObject::class -> this.getJSONObject(index)
                    JSONArray::class -> this.getJSONArray(index)
                    String::class -> this.getString(index)
                    Int::class -> this.getInt(index)
                    Long::class -> this.getLong(index)
                    Double::class -> this.getDouble(index)
                    Float::class -> this.getDouble(index).toFloat()
                    else -> null
                } as T?
            } catch (error: JSONException) {
                Log.e(TAG, "Could not retrieve [$index] from $this", error)
                null
            } catch (error: ClassCastException) {
                Log.e(TAG, "Failed to cast [$index] from $this to type ${T::class}")
                null
            }
        }

        /**
         * Converts [JSONArray] to [List] by using [get]
         *
         * @param T Supports [JSONObject], [JSONArray], [String], [Int], [Long], [Double]
         * @author James Ding
         */
        inline fun <reified T> JSONArray.toList(): List<T?> {

            val mutableList = mutableListOf<T?>()
            for (i in 0 until length()) {
                mutableList.add(getElement(i))
            }

            return mutableList.toList()
        }

        /**
         * Coverts [List] from [toList] to [MutableList]
         */
        inline fun <reified T> JSONArray.toMutableList() = toList<T>().toMutableList()
    }

}
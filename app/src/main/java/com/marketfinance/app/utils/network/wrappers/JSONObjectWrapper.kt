package com.marketfinance.app.utils.network.wrappers

import android.util.Log
import com.marketfinance.app.utils.network.parser.RawFMT
import com.marketfinance.app.utils.network.parser.RawLongFMT
import com.marketfinance.app.utils.network.wrappers.JSONArrayWrapper.Companion.toList
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Wrapper for handling a [JSONObject] with `null` processing and logging
 *
 * @author James Ding
 */
class JSONObjectWrapper {

    companion object {
        const val TAG = "JSONObjectWrapper"

        @Suppress("IMPLICIT_CAST_TO_ANY")
        /**
         * Retrieves [T] to with [key]
         *
         * @param T Supports [JSONObject], [JSONArray], [String], [Int], [Long], [Double], [Boolean],[RawFMT], [RawLongFMT]
         * @param key The [key] that points to [T] in the [JSONArray]
         * @return The [T] with the specified [key]
         * @author James Ding
         */
        inline fun <reified T> JSONObject.getElement(key: String): T? {
            return try {
                when (T::class) {
                    JSONObject::class -> getJSONObject(key)
                    JSONArray::class -> getJSONArray(key)
                    String::class -> getString(key)
                    Int::class -> getInt(key)
                    Long::class -> getLong(key)
                    Double::class -> getDouble(key)
                    Float::class -> getDouble(key).toFloat()
                    Boolean::class -> getBoolean(key)
                    RawFMT::class -> RawFMT(
                        getJSONObject(key).getDouble("raw"),
                        getJSONObject(key).getString("fmt")
                    )
                    RawLongFMT::class -> RawLongFMT(
                        getJSONObject(key).getLong("raw"),
                        getJSONObject(key).getString("fmt"),
                        getJSONObject(key).getString("longFmt")
                    )
                    else -> null
                } as T?
            } catch (error: JSONException) {
                Log.e(TAG, "Could not retrieve from \"$key\" from $this", error)
                null
            } catch (error: ClassCastException) {
                Log.e(TAG, "Failed to cast \"$key\" from $this to type ${T::class}", error)
                null
            }
        }

        /**
         * Get first [T] from [JSONArray]
         *
         * @param T Supports [JSONObject], [JSONArray], [String], [Int], [Long], [Double]
         * @param key The [key] that points to the [T] in the [JSONArray]
         * @return The first [T] from the [JSONArray] with the specified [key]
         * @author James Ding
         */
        inline fun <reified T> JSONObject.first(key: String) =
            getElement<JSONArray>(key)?.toList<T>()?.first()
    }

}
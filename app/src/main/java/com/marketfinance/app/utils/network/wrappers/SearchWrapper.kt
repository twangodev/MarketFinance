package com.marketfinance.app.utils.network.wrappers

import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject

interface SearchWrapper {

    companion object {

        /**
         * Required crumb for interacting with the search API
         */
        private const val crumb = "kX5Fok7uhP5"

        private const val searchURL = "https://finance.yahoo.com/_finance_doubledown/api/resource?crumb=$crumb"

        private object Headers {

            /**
             * Cookie
             *
             *
             */
            const val cookie = "APID=UP8caf6a0c-6815-11eb-9229-029a7723d32d;" +
                    "T=af=JnRzPTE2MTU3MDgzNDImcHM9THFkY1Nadkw3VWZBRTM4Ymk1ckFpQS0t&d=bnMBeWFob28BZwE0VVdYWTVPTTROTk83NlFISktEUkdIR1JBRQFhYwFBRFp2NjF1dAFhbAFqYW1lc2RpbmczNjVAZ21haWwuY29tAXNjAW1icl9yZWdpc3RyYXRpb24BZnMBVEdCakZPSmdUY0MyAXp6ATJDY1RnQkE3RQFhAVFBRQFsYXQBMkNjVGdCAW51ATA-&sk=DAAY4RBFAP1hAp&ks=EAAwDrFHSUEbcDJ1oez4AgbVA--~G&kt=EAAQ.NG03YVP._CsUMcrvGt3w--~I&ku=FAABbxAAYPP9bRSVO6zkkmWfEW6aow0khMDyvqNDZ5YikPIv7M7exF6u3e1PPSpIFolwztUarNaUilBDTZTQAOX5i_zXPbvfUb1NLVG1zwx7d9O3kIvspt9Pyd6lu5g8ikAkWaYqQ3izPylgNn2ax0VFKxrY57Ua8odvPJbWbiNfyA-~D;" +
                    "Y=v=1&n=2go7hpat184vi&l=o3oa88cu2xnf6vwgvb2clibfng97dk71sb1gvh15/o&p=n30vvvv00000000&r=188&intl=us;" +
                    "A1=d=AQABBBN3_l8CECpXuJ_CPCKO81uqaHmm0k0FEgEAAgIFT2AkYdwr0iMA_SMAAAcIE3f-X3mm0k0ID4e4mJlkQn0NqYNGpvXKPwkBAAoBwA&S=AQAAApGqtBAoxx6D2306n0eNhn4"
        }
    }

    fun createSearch(
        query: String,
        callback: Response.Listener<JSONObject>,
        errorCallback: Response.ErrorListener
    ): JsonObjectRequest {
        val postJSONObject = JSONObject(
            "{\n" +
                    "\t\"requests\": {\n" +
                    "\t\t\"g0\": {\n" +
                    "\t\t\t\"resource\": \"searchassist\",\n" +
                    "\t\t\t\"operation\": \"read\",\n" +
                    "\t\t\t\"params\": {\n" +
                    "\t\t\t\t\"searchTerm\": \"$query\",\n" +
                    "\t\t\t\t\"gossipConfig\": {\n" +
                    "\t\t\t\t\t\"queryKey\": \"query\",\n" +
                    "\t\t\t\t\t\"resultAccessor\": \"ResultSet.Result\",\n" +
                    "\t\t\t\t\t\"suggestionTitleAccessor\": \"symbol\",\n" +
                    "\t\t\t\t\t\"suggestionMeta\": [\"symbol\"]\n" +
                    "\t\t\t\t}\n" +
                    "\t\t\t}\n" +
                    "\t\t}\n" +
                    "\t}\n" +
                    "}"
        )
        return object : JsonObjectRequest(Method.POST, searchURL, postJSONObject, callback, errorCallback) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = mutableMapOf<String, String>()
                headers["Content-Type"] = "application/json"
                headers["Cookie"] = Headers.cookie
                return headers
            }
        }

    }


}
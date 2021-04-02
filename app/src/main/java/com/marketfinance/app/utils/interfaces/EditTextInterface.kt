package com.marketfinance.app.utils.interfaces

import android.widget.EditText
import java.util.*

interface EditTextInterface {

    fun EditText.formatForThousand() {
        val value = text.toString()
        if (value != "") {
            if (value.startsWith(".")) setText("0.")
            if (value.startsWith("0") && !value.startsWith("0.")) setText("")
            val string = value.trimCommaOfString()
            setText(string.getDecimalFormatted())
            setSelection(value.length)
        }
    }


    fun String.getDecimalFormatted(): String {
        val tokenized = StringTokenizer(this, ".")
        var stringOne = this
        var stringTwo = ""

        if (tokenized.countTokens() > 1) {
            stringOne = tokenized.nextToken()
            stringTwo = tokenized.nextToken()
        }

        var stringThree = ""
        var indexOne = 0
        var indexTwo = -1 + stringOne.length

        if (stringOne[indexTwo] == '.') {
            indexTwo--
            stringThree = "."
        }

        var indexThree = indexTwo

        while (true) {
            if (indexThree < 0) {
                if (stringTwo.isNotEmpty()) stringThree = "$stringThree.$stringTwo"
                return stringThree
            }

            if (indexOne == 3) {
                stringThree = ",$stringThree"
                indexOne = 0
            }

            stringThree = stringOne[indexThree].toString() + stringThree
            indexOne++
            indexThree--
        }
    }

    fun String.trimCommaOfString(): String {
        return if (contains(",")) {
            replace(",", "")
        } else {
            this
        }
    }


}
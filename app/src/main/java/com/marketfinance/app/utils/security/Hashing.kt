package com.marketfinance.app.utils.security

import java.security.MessageDigest

interface Hashing {

    /**
     * 256-SHA Hashing algorithm. Code sourced from [https://stackoverflow.com/questions/5531455/how-to-hash-some-string-with-sha256-in-java]
     * @author Samuel Luis
     */
    fun sha256(input: String) : String{
        val charset = Charsets.UTF_8
        val byteArray = input.toByteArray(charset)
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(byteArray)

        return hash.fold("", { str, it -> str + "%02x".format(it)})
    }

}
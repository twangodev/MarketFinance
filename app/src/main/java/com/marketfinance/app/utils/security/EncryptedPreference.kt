package com.marketfinance.app.utils.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class EncryptedPreference(private val preferenceName: String) {

    val TAG = "EncryptedPreference"

    fun getPreference(context: Context): SharedPreferences {
        val spec = KeyGenParameterSpec.Builder(
            MasterKey.DEFAULT_MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        val masterKeyAlias = MasterKey.Builder(context)
            .setKeyGenParameterSpec(spec)
            .build()

        val decryptedPreference = EncryptedSharedPreferences.create(
            context,
            preferenceName,
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        Log.d(TAG, "Got Instance: $preferenceName. Data: ${decryptedPreference.all}")

        return decryptedPreference
    }


}
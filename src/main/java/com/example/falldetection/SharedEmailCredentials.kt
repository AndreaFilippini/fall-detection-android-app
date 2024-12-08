package com.example.falldetection

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SharedEmailCredentials(private val context: Context) {

    // create the key with which to store the credentials information in the shared preference obj
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // create a shared preference to stored encrypt credentials of the email sender
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context, "secure_prefs", masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // store and update the email and password of the sender
    fun storeCredentials(email: String, password: String) {
        with(sharedPreferences.edit()) {
            putString("email", email)
            putString("password", password)
            apply()
        }
    }

    // retrieve the email and password of the sender as a pair
    // if the credentials are empty, the method will return null
    fun getCredentials(): Pair<String, String>? {
        val email = sharedPreferences.getString("email", null)
        val password = sharedPreferences.getString("password", null)
        if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
            return Pair(email, password)
        }
        return null
    }

    // delete the stored credentials
    fun clearCredentials() {
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
    }
}

package com.ust.discovery.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit

class SessionManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        sharedPreferences.edit { putString(KEY_ID_TOKEN, token) }
    }

    fun getToken(): String? {
        return sharedPreferences.getString(KEY_ID_TOKEN, null)
    }

    fun clearSession() {
        sharedPreferences.edit { clear() }
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    companion object {
        private const val PREFS_NAME = "secure_prefs"
        private const val KEY_ID_TOKEN = "id_token"
    }
}

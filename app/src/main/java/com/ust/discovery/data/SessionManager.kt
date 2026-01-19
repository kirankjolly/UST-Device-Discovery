package com.ust.discovery.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit
import java.io.File

class SessionManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = createEncryptedPreferences(context, masterKey)

    private fun createEncryptedPreferences(context: Context, masterKey: MasterKey): SharedPreferences {
        return try {
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "EncryptedSharedPreferences corrupted, recreating...", e)

            // Delete corrupted preferences file
            val prefsFile = File(context.applicationInfo.dataDir + "/shared_prefs/$PREFS_NAME.xml")
            if (prefsFile.exists()) {
                prefsFile.delete()
                Log.d(TAG, "Deleted corrupted preferences file")
            }

            // Try creating again
            try {
                EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to recreate EncryptedSharedPreferences, falling back to regular SharedPreferences", e)
                // Fallback to regular SharedPreferences (only as last resort)
                context.getSharedPreferences(PREFS_NAME + "_fallback", Context.MODE_PRIVATE)
            }
        }
    }

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
        private const val TAG = "SessionManager"
        private const val PREFS_NAME = "secure_prefs"
        private const val KEY_ID_TOKEN = "id_token"
    }
}

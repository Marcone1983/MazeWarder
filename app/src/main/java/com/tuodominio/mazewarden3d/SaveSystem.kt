package com.marcone1983.mazewarden3d

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaveSystem @Inject constructor() {

    private companion object {
        const val PREF_NAME = "MazeWardenSave"
        const val KEY_ALIAS = "MazeWardenMasterKey"
    }
    
    private var encryptedPrefs: SharedPreferences? = null

    private fun getEncryptedPrefs(context: Context): SharedPreferences {
        if (encryptedPrefs == null) {
            try {
                val masterKey = MasterKey.Builder(context, KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                encryptedPrefs = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e: Exception) {
                // Fallback to regular SharedPreferences if encryption fails
                encryptedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            }
        }
        return encryptedPrefs!!
    }

    fun saveProgress(context: Context, key: String, value: String) {
        try {
            getEncryptedPrefs(context).edit().putString(key, value).apply()
        } catch (e: Exception) {
            // Log error but don't crash
        }
    }

    fun loadProgress(context: Context, key: String): String? {
        return try {
            getEncryptedPrefs(context).getString(key, null)
        } catch (e: Exception) {
            null
        }
    }

    fun resetProgress(context: Context) {
        try {
            getEncryptedPrefs(context).edit().clear().apply()
        } catch (e: Exception) {
            // Log error but don't crash
        }
    }
    
    fun saveInt(context: Context, key: String, value: Int) {
        try {
            getEncryptedPrefs(context).edit().putInt(key, value).apply()
        } catch (e: Exception) { }
    }
    
    fun loadInt(context: Context, key: String, defaultValue: Int = 0): Int {
        return try {
            getEncryptedPrefs(context).getInt(key, defaultValue)
        } catch (e: Exception) {
            defaultValue
        }
    }
    
    fun saveBoolean(context: Context, key: String, value: Boolean) {
        try {
            getEncryptedPrefs(context).edit().putBoolean(key, value).apply()
        } catch (e: Exception) { }
    }
    
    fun loadBoolean(context: Context, key: String, defaultValue: Boolean = false): Boolean {
        return try {
            getEncryptedPrefs(context).getBoolean(key, defaultValue)
        } catch (e: Exception) {
            defaultValue
        }
    }
}
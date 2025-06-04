package com.tuodominio.mazewarden3d

import android.content.Context
import android.content.SharedPreferences

object SaveSystem {

    private const val PREF_NAME = "MazeWardenSave"

    fun saveProgress(context: Context, key: String, value: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(key, value).apply()
    }

    fun loadProgress(context: Context, key: String): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(key, null)
    }

    fun resetProgress(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
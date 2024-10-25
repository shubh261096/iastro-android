package com.iffelse.iastro.utils

import android.content.Context
import android.content.SharedPreferences

object KeyStorePref {

    private const val PREFERENCES_NAME = "com.iffelse.iastro"
    private lateinit var sharedPreferences: SharedPreferences

    fun initialize(context: Context) {
        sharedPreferences = context.applicationContext.getSharedPreferences(PREFERENCES_NAME, 0)
    }

    fun getBoolean(key: String?): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    fun putBoolean(key: String?, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun putString(key: String?, value: String?) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String?): String? {
        return sharedPreferences.getString(key, "")
    }

    fun putLong(key: String?, value: Long) {
        val editor = sharedPreferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getLong(key: String?): Long {
        return sharedPreferences.getLong(key, 0)
    }

    fun clearAllPrefs() {
        sharedPreferences.edit().clear().apply()
    }
}
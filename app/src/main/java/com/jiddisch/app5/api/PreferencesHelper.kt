package com.jiddisch.app5.api

import android.content.Context
import android.content.SharedPreferences

class PreferencesHelper(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("ApiCache", Context.MODE_PRIVATE)

    fun save(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun get(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    fun isCacheValid(key: String, cacheTime: Long): Boolean {
        val lastSaved = sharedPreferences.getLong("$key-time", 0)
        return (System.currentTimeMillis() - lastSaved) <= cacheTime
    }

    fun saveTime(key: String) {
        val editor = sharedPreferences.edit()
        editor.putLong("$key-time", System.currentTimeMillis())
        editor.apply()
    }
}

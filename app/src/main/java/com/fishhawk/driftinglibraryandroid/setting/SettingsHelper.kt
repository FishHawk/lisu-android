package com.fishhawk.driftinglibraryandroid.setting

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object SettingsHelper {
    private lateinit var sharedPreferences: SharedPreferences

    fun initialize(context: Context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    private fun setPreference(key: String?, value: Int) {
        sharedPreferences.edit().putString(key, value.toString()).apply()
    }
    private fun setPreference(key: String?, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    private fun getPreference(key: String, default: Int): Int {
        return sharedPreferences.getString(key, null)?.toIntOrNull() ?: default
    }
    private fun getPreference(key: String, default: String): String {
        return sharedPreferences.getString(key, null)?: default
    }

    private const val KEY_LIBRARY_ADDRESS = "library_address"
    private const val DEFAULT_LIBRARY_ADDRESS: String = "192.168.0.101:8080"

    fun getLibraryAddress(): String = getPreference(KEY_LIBRARY_ADDRESS, DEFAULT_LIBRARY_ADDRESS)

    const val READING_DIRECTION_LEFT_TO_RIGHT: Int = 0
    const val READING_DIRECTION_RIGHT_TO_LEFT: Int = 1
    const val READING_DIRECTION_VERTICAL: Int = 2

    private const val KEY_READING_DIRECTION = "reading_direction"
    private const val DEFAULT_READING_DIRECTION: Int = READING_DIRECTION_RIGHT_TO_LEFT

    fun getReadingDirection(): Int = getPreference(KEY_READING_DIRECTION, DEFAULT_READING_DIRECTION)
    fun setReadingDirection(value: Int) = setPreference(KEY_READING_DIRECTION, value)
}
package com.fishhawk.driftinglibraryandroid.setting

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object SettingsHelper {
    //    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences

    fun initialize(context: Context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    private fun putIntToStr(key: String?, value: Int) {
        sharedPreferences.edit().putString(key, value.toString()).apply()
    }

    fun getIntFromStr(key: String, default: Int): Int {
        return sharedPreferences.getString(key, null)?.toIntOrNull() ?: default
    }

    private const val KEY_READING_DIRECTION = "reading_direction"
    private val DEFAULT_READING_DIRECTION: Int = 0
    //    private val DEFAULT_READING_DIRECTION: Int = GalleryView.LAYOUT_RIGHT_TO_LEFT

    fun getReadingDirection(): Int = getIntFromStr(KEY_READING_DIRECTION, DEFAULT_READING_DIRECTION)
    fun setReadingDirection(value: Int) = putIntToStr(KEY_READING_DIRECTION, value)
}
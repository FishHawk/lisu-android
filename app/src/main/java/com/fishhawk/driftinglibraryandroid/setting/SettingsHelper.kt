package com.fishhawk.driftinglibraryandroid.setting

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object SettingsHelper {
    private lateinit var sharedPreferences: SharedPreferences

    fun initialize(context: Context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    private const val KEY_LIBRARY_ADDRESS = "library_address"
    private const val DEFAULT_LIBRARY_ADDRESS: String = "192.168.0.101:8080"

    val libraryAddress by lazy {
        PreferenceStringLiveData(
            sharedPreferences,
            KEY_LIBRARY_ADDRESS,
            DEFAULT_LIBRARY_ADDRESS
        )
    }

    const val READING_DIRECTION_LEFT_TO_RIGHT: String = "0"
    const val READING_DIRECTION_RIGHT_TO_LEFT: String = "1"
    const val READING_DIRECTION_VERTICAL: String = "2"

    private const val KEY_READING_DIRECTION = "reading_direction"
    private const val DEFAULT_READING_DIRECTION: String = READING_DIRECTION_RIGHT_TO_LEFT

    val readingDirection by lazy {
        PreferenceStringLiveData(
            sharedPreferences,
            KEY_READING_DIRECTION,
            DEFAULT_READING_DIRECTION
        )
    }

    const val THEME_LIGHT: String = "0"
    const val THEME_DARK: String = "1"

    private const val KEY_THEME = "theme"
    private const val DEFAULT_THEME: String = THEME_LIGHT

    val theme by lazy {
        PreferenceStringLiveData(
            sharedPreferences,
            KEY_THEME,
            DEFAULT_THEME
        )
    }
}
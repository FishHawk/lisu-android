package com.fishhawk.driftinglibraryandroid.setting

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object SettingsHelper {
    private lateinit var sharedPreferences: SharedPreferences

    fun initialize(context: Context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    private const val KEY_SELECTED_LIBRARY = "selected_library"
    private const val DEFAULT_SELECTED_LIBRARY: Int = 1

    val selectedServer by lazy {
        PreferenceIntLiveData(
            sharedPreferences,
            KEY_SELECTED_LIBRARY,
            DEFAULT_SELECTED_LIBRARY
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

    const val DISPLAY_MODE_GRID: String = "0"
    const val DISPLAY_MODE_LINEAR: String = "1"

    private const val KEY_DISPLAY_MODE = "display_mode"
    private const val DEFAULT_DISPLAY_MODE: String = DISPLAY_MODE_GRID

    val displayMode by lazy {
        PreferenceStringLiveData(
            sharedPreferences,
            KEY_DISPLAY_MODE,
            DEFAULT_DISPLAY_MODE
        )
    }

    const val HISTORY_FILTER_ALL: String = "0"
    const val HISTORY_FILTER_FROM_LIBRARY: String = "1"
    const val HISTORY_FILTER_FROM_SOURCES: String = "2"

    private const val KEY_HISTORY_FILTER = "history_filter"
    private const val DEFAULT_HISTORY_FILTER: String = HISTORY_FILTER_FROM_LIBRARY

    val historyFilter by lazy {
        PreferenceStringLiveData(
            sharedPreferences,
            KEY_HISTORY_FILTER,
            DEFAULT_HISTORY_FILTER
        )
    }

    const val CHAPTER_DISPLAY_MODE_GRID: String = "0"
    const val CHAPTER_DISPLAY_MODE_LINEAR: String = "1"

    private const val KEY_CHAPTER_DISPLAY_MODE = "display_mode"
    private const val DEFAULT_CHAPTER_DISPLAY_MODE: String = CHAPTER_DISPLAY_MODE_GRID

    val chapterDisplayMode by lazy {
        PreferenceStringLiveData(
            sharedPreferences,
            KEY_CHAPTER_DISPLAY_MODE,
            DEFAULT_CHAPTER_DISPLAY_MODE
        )
    }

    const val CHAPTER_DISPLAY_ORDER_ASCEND: String = "0"
    const val CHAPTER_DISPLAY_ORDER_DESCEND: String = "1"

    private const val KEY_CHAPTER_DISPLAY_ORDER = "display_mode"
    private const val DEFAULT_CHAPTER_DISPLAY_ORDER: String = CHAPTER_DISPLAY_ORDER_ASCEND

    val chapterDisplayOrder by lazy {
        PreferenceStringLiveData(
            sharedPreferences,
            KEY_CHAPTER_DISPLAY_ORDER,
            DEFAULT_CHAPTER_DISPLAY_ORDER
        )
    }
}
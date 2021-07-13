package com.fishhawk.driftinglibraryandroid.data.preference

import android.content.Context
import androidx.preference.PreferenceManager
import com.tfcporciuncula.flow.FlowSharedPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
object GlobalPreference {
    private lateinit var flowPrefs: FlowSharedPreferences

    fun initialize(context: Context) {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        flowPrefs = FlowSharedPreferences(sharedPrefs)
    }


    // Helper
    private fun get(key: String, defaultValue: Int) =
        lazy { flowPrefs.getInt(key, defaultValue) }

    private fun get(key: String, defaultValue: Boolean) =
        lazy { flowPrefs.getBoolean(key, defaultValue) }

    private fun get(key: String, defaultValue: String) =
        lazy { flowPrefs.getString(key, defaultValue) }

    private inline fun <reified T : Enum<T>> get(key: String, defaultValue: T) =
        lazy { flowPrefs.getEnum(key, defaultValue) }


    // Settings internal
    val selectedServer by get("selected_library", 1)
    val lastUsedProvider by get("last_used_provider", "")


    // Settings general
    enum class StartScreen { LIBRARY, HISTORY, EXPLORE }

    val startScreen by get("start_screen", StartScreen.LIBRARY)

    enum class Theme { LIGHT, DARK }

    val theme by get("theme", Theme.LIGHT)

    enum class HistoryFilter { ALL, FROM_LIBRARY, FROM_SOURCES }

    val historyFilter by get("history_filter", HistoryFilter.FROM_LIBRARY)

    enum class ChapterDisplayMode { GRID, LINEAR }
    enum class ChapterDisplayOrder { ASCEND, DESCEND }

    val chapterDisplayMode by get("chapter_display_mode", ChapterDisplayMode.GRID)
    val chapterDisplayOrder by get("chapter_display_order", ChapterDisplayOrder.ASCEND)


    // Settings reading
    enum class ReadingDirection { LTR, RTL, VERTICAL, CONTINUOUS }
    enum class ScreenOrientation { DEFAULT, LOCK, PORTRAIT, LANDSCAPE }

    val readingDirection by get("reading_direction", ReadingDirection.LTR)
    val screenOrientation by get("screen_orientation", ScreenOrientation.DEFAULT)
    val isPageIntervalEnabled by get("is_page_interval_enabled", false)
    val showInfoBar by get("show_info_bar", true)
    val keepScreenOn by get("keep_screen_on", false)
    val useVolumeKey by get("use_volume_key", false)
    val invertVolumeKey by get("invert_volume_key", false)
    val isLongTapDialogEnabled by get("is_long_tap_dialog_enabled", true)
    val isAreaInterpolationEnabled by get("is_area_interpolation_enabled", false)


    enum class ColorFilterMode { DEFAULT, MULTIPLY, SCREEN, OVERLAY, LIGHTEN, DARKEN }

    val colorFilter by get("color_filter", false)
    val colorFilterH by get("color_filter_h", 0)
    val colorFilterS by get("color_filter_s", 50)
    val colorFilterL by get("color_filter_l", 50)
    val colorFilterA by get("color_filter_a", 127)
    val colorFilterMode by get("color_filter_mode", ColorFilterMode.DEFAULT)

    val customBrightness by get("custom_brightness", false)
    val customBrightnessValue by get("custom_brightness_value", 10)


    // Settings advanced
    val secureMode by get("secure_mode", false)
}
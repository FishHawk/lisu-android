package com.fishhawk.lisu.data.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

enum class StartScreen { Library, History, Explore }
enum class Theme { Light, Dark }

enum class ChapterDisplayMode { Grid, Linear }
enum class ChapterDisplayOrder { Ascend, Descend }

enum class ReaderMode { Ltr, Rtl, Continuous }
enum class ReaderOrientation { Portrait, Landscape }

enum class ColorFilterMode { Default, Multiply, Screen, Overlay, Lighten, Darken }
enum class ScaleType { FitScreen, FitWidth, FitHeight, OriginalSize }

class PreferenceRepository(context: Context) {
    private val Context.store by preferencesDataStore(name = "preference")
    private val store = context.store

    val serverAddress by store.get("serverAddress", "192.168.1.100:8080")
    val lastAppCheckTime by store.get("lastAppCheckTime", 0L)

    val lastUsedProvider by store.get("last_used_provider", "")

    val enableAutoUpdates by store.get("enable_auto_updates", true)

    val theme by store.get("theme", Theme.Light)
    val secureMode by store.get("secure_mode", false)


    val startScreen by store.get("start_screen", StartScreen.Library)
    val chapterDisplayMode by store.get("chapter_display_mode", ChapterDisplayMode.Grid)
    val chapterDisplayOrder by store.get("chapter_display_order", ChapterDisplayOrder.Ascend)
    val isConfirmExitEnabled by store.get("is_confirm_exit_enabled", false)
    val isRandomButtonEnabled by store.get("is_random_button_enabled", false)


    val readerMode by store.get("reader_mode", ReaderMode.Ltr)
    val readerOrientation by store.get("reader_orientation", ReaderOrientation.Portrait)

    val scaleType by store.get("scale_type", ScaleType.FitScreen)

    val isPageIntervalEnabled by store.get("is_page_interval_enabled", false)
    val showInfoBar by store.get("show_info_bar", true)
    val isLongTapDialogEnabled by store.get("is_long_tap_dialog_enabled", true)
    val isAreaInterpolationEnabled by store.get("is_area_interpolation_enabled", false)

    val keepScreenOn by store.get("keep_screen_on", false)
    val useVolumeKey by store.get("use_volume_key", false)
    val invertVolumeKey by store.get("invert_volume_key", false)


    val enabledColorFilter by store.get("enableColorFilter", false)
    val colorFilterMode by store.get("colorFilterMode", ColorFilterMode.Default)
    val colorFilterH by store.get("colorFilterH", 0f)
    val colorFilterS by store.get("colorFilterS", 0.5f)
    val colorFilterL by store.get("colorFilterL", 0.5f)
    val colorFilterA by store.get("colorFilterA", 0.5f)

    val enableCustomBrightness by store.get("enableCustomBrightness", false)
    val customBrightness by store.get("customBrightness", 0.1f)
}
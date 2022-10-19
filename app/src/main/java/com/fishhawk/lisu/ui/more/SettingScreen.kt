package com.fishhawk.lisu.ui.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import com.fishhawk.lisu.BuildConfig
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.datastore.*
import com.fishhawk.lisu.ui.main.MainViewModel
import com.fishhawk.lisu.ui.main.navToOpenSourceLicense
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.util.copyToClipboard
import com.fishhawk.lisu.util.openWebPage
import com.fishhawk.lisu.util.toast
import com.fishhawk.lisu.widget.LisuScaffold
import com.fishhawk.lisu.widget.LisuToolBar
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingScreen(
    title: String,
    navController: NavHostController,
    content: @Composable (NavHostController) -> Unit,
) {
    LisuScaffold(
        topBar = {
            LisuToolBar(
                title = title,
                onNavUp = { navController.navigateUp() }
            )
        },
        content = { paddingValues ->
            LisuTransition {
                val state = rememberScrollState()
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxWidth()
                        .verticalScroll(state),
                ) {
                    content(navController)
                }
            }
        }
    )
}

@Composable
fun SettingGeneralScreen(
    navController: NavHostController,
) = SettingScreen(
    title = stringResource(R.string.label_settings_general),
    navController = navController,
) {
    ListPreference(
        title = stringResource(R.string.settings_start_screen),
        preference = PR.startScreen
    ) {
        when (it) {
            StartScreen.Library -> R.string.settings_start_screen_library
            StartScreen.History -> R.string.settings_start_screen_history
            StartScreen.Explore -> R.string.settings_start_screen_explore
        }
    }

    ListPreference(
        title = stringResource(R.string.settings_theme),
        preference = PR.theme
    ) {
        when (it) {
            Theme.Light -> R.string.settings_theme_light
            Theme.Dark -> R.string.settings_theme_dark
        }
    }

    ListPreference(
        title = stringResource(R.string.settings_chapter_display_mode),
        preference = PR.chapterDisplayMode
    ) {
        when (it) {
            ChapterDisplayMode.Grid -> R.string.settings_display_mode_gird
            ChapterDisplayMode.Linear -> R.string.settings_display_mode_linear
        }
    }

    ListPreference(
        title = stringResource(R.string.settings_chapter_display_order),
        preference = PR.chapterDisplayOrder
    ) {
        when (it) {
            ChapterDisplayOrder.Ascend -> R.string.settings_display_order_ascend
            ChapterDisplayOrder.Descend -> R.string.settings_display_order_descend
        }
    }

    SwitchPreference(
        title = stringResource(R.string.settings_enable_confirm_exit),
        preference = PR.isConfirmExitEnabled
    )

    SwitchPreference(
        title = stringResource(R.string.settings_enable_random_button),
        preference = PR.isRandomButtonEnabled
    )
}

@Composable
fun SettingReaderScreen(
    navController: NavHostController,
) = SettingScreen(
    title = stringResource(R.string.label_settings_reader),
    navController = navController,
) {
    ListPreference(
        title = stringResource(R.string.settings_reader_mode),
        preference = PR.readerMode
    ) {
        when (it) {
            ReaderMode.Ltr -> R.string.settings_reader_mode_left_to_right
            ReaderMode.Rtl -> R.string.settings_reader_mode_right_to_Left
            ReaderMode.Continuous -> R.string.settings_reader_mode_continuous
        }
    }

    ListPreference(
        title = stringResource(R.string.settings_reader_orientation),
        preference = PR.readerOrientation
    ) {
        when (it) {
            ReaderOrientation.Portrait -> R.string.settings_reader_orientation_portrait
            ReaderOrientation.Landscape -> R.string.settings_reader_orientation_landscape
        }
    }

    ListPreference(
        title = stringResource(R.string.settings_scale_type),
        preference = PR.scaleType
    ) {
        when (it) {
            ScaleType.FitScreen -> R.string.settings_scale_type_fit_screen
            ScaleType.FitWidth -> R.string.settings_scale_type_fit_width
            ScaleType.FitHeight -> R.string.settings_scale_type_fit_height
            ScaleType.OriginalSize -> R.string.settings_scale_type_original_size
        }
    }

    SwitchPreference(
        title = stringResource(R.string.settings_is_page_interval_enabled),
        preference = PR.isPageIntervalEnabled
    )
    SwitchPreference(
        title = stringResource(R.string.settings_show_info_bar),
        preference = PR.showInfoBar
    )
    SwitchPreference(
        title = stringResource(R.string.settings_is_long_tap_dialog_enabled),
        preference = PR.isLongTapDialogEnabled
    )
    SwitchPreference(
        title = stringResource(R.string.settings_is_area_interpolation_enabled),
        summary = stringResource(R.string.settings_is_area_interpolation_enabled_summary),
        preference = PR.isAreaInterpolationEnabled
    )

    SwitchPreference(
        title = stringResource(R.string.settings_keep_screen_on),
        preference = PR.keepScreenOn
    )
    SwitchPreference(
        title = stringResource(R.string.settings_use_volume_key),
        preference = PR.useVolumeKey
    )
    SwitchPreference(
        title = stringResource(R.string.settings_invert_volume_key),
        preference = PR.invertVolumeKey
    )
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun SettingAdvancedScreen(
    navController: NavHostController,
) = SettingScreen(
    title = stringResource(R.string.label_settings_advanced),
    navController = navController,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    TextPreference(
        title = stringResource(R.string.settings_clear_image_cache),
        summary = stringResource(R.string.settings_clear_image_cache_summary)
    ) {
        scope.launch {
            context.imageLoader.memoryCache?.clear()
            context.imageLoader.diskCache?.clear()
            context.toast(R.string.cache_cleared)
        }
    }

    SwitchPreference(
        title = stringResource(R.string.settings_secure_mode),
        summary = stringResource(R.string.settings_secure_mode_summary),
        preference = PR.secureMode
    )
}

@Composable
fun AboutScreen(
    navController: NavHostController,
    viewModel: MainViewModel = koinViewModel(),
) = SettingScreen(
    title = stringResource(R.string.label_about),
    navController = navController,
) {
    val context = LocalContext.current
    val versionPrefix = if (BuildConfig.DEBUG) "Preview" else "Stable"
    val version = "$versionPrefix ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
    TextPreference(
        title = stringResource(R.string.about_version),
        summary = version
    ) { context.copyToClipboard(version, R.string.toast_version_copied) }

    val githubUrl = "https://github.com/FishHawk/lisu-android"
    TextPreference(
        title = stringResource(R.string.about_github),
        summary = githubUrl
    ) { context.openWebPage(githubUrl) }

    SwitchPreference(
        title = stringResource(R.string.about_enable_auto_updates),
        preference = PR.enableAutoUpdates,
    )

    TextPreference(
        title = stringResource(R.string.about_check_for_updates)
    ) {
        context.toast(context.getString(R.string.update_check_look_for_updates))
        viewModel.checkForUpdate(true)
    }

    val releaseUrl = "$githubUrl/releases/tag/v${BuildConfig.VERSION_NAME}"
    TextPreference(
        title = stringResource(R.string.about_whats_new)
    ) { context.openWebPage(releaseUrl) }

    TextPreference(
        title = stringResource(R.string.about_open_source_licenses)
    ) { navController.navToOpenSourceLicense() }
}

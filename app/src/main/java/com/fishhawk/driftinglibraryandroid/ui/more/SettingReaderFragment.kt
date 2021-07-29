package com.fishhawk.driftinglibraryandroid.ui.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.PR
import com.fishhawk.driftinglibraryandroid.data.datastore.ReaderMode
import com.fishhawk.driftinglibraryandroid.data.datastore.ReaderOrientation
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationToolBar
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTransition

@Composable
fun SettingReaderScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            ApplicationToolBar(
                stringResource(R.string.label_settings_reader),
                navController
            )
        },
        content = { ApplicationTransition { Content() } }
    )
}

@Composable
private fun Content() {
    Column(modifier = Modifier.fillMaxWidth()) {
        ListPreference(
            title = stringResource(R.string.settings_reader_mode),
            preference = PR.readerMode
        ) {
            when (it) {
                ReaderMode.Ltr -> R.string.settings_reader_mode_left_to_right
                ReaderMode.Rtl -> R.string.settings_reader_mode_right_to_Left
                ReaderMode.Vertical -> R.string.settings_reader_mode_vertical
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
}
package com.fishhawk.driftinglibraryandroid.ui.reader

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.datastore.PR
import com.fishhawk.driftinglibraryandroid.ui.more.SwitchPreference

@Composable
fun ReaderSettingsSheet() {
    Column(modifier = Modifier.padding(8.dp)) {
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
package com.fishhawk.lisu.ui.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.datastore.ChapterDisplayMode
import com.fishhawk.lisu.data.datastore.ChapterDisplayOrder
import com.fishhawk.lisu.data.datastore.StartScreen
import com.fishhawk.lisu.data.datastore.Theme
import com.fishhawk.lisu.ui.theme.LisuToolBar
import com.fishhawk.lisu.ui.theme.LisuTransition

@Composable
fun SettingGeneralScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            LisuToolBar(
                title = stringResource(R.string.label_settings_general),
                onNavUp = { navController.navigateUp() }
            )
        },
        content = { LisuTransition { Content() } }
    )
}

@Composable
private fun Content() {
    Column(modifier = Modifier.fillMaxWidth()) {
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
            title = stringResource(R.string.settings_enable_random_button),
            preference = PR.isRandomButtonEnabled
        )
    }
}
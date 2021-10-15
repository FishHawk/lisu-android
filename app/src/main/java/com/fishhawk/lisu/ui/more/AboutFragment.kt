package com.fishhawk.lisu.ui.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.fishhawk.lisu.BuildConfig
import com.fishhawk.lisu.R
import com.fishhawk.lisu.ui.base.copyToClipboard
import com.fishhawk.lisu.ui.theme.LisuToolBar
import com.fishhawk.lisu.ui.theme.LisuTransition

@Composable
fun AboutScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            LisuToolBar(
                title = stringResource(R.string.label_about),
                navController = navController
            )
        },
        content = { LisuTransition { Content() } }
    )
}

@Composable
private fun Content() {
    Column(modifier = Modifier.fillMaxWidth()) {
        val context = LocalContext.current
        val versionPrefix = if (BuildConfig.DEBUG) "Preview" else "Stable"
        val version = "$versionPrefix ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        TextPreference(
            title = stringResource(R.string.about_version),
            summary = version
        ) { context.copyToClipboard(version, R.string.toast_version_copied) }

        TextPreference(
            title = stringResource(R.string.about_check_for_updates)
        )

        TextPreference(
            title = stringResource(R.string.about_github),
            summary = "https://github.com/FishHawk/lisu-android"
        )
    }
}
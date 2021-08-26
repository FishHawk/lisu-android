package com.fishhawk.driftinglibraryandroid.ui.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import coil.imageLoader
import coil.util.CoilUtils
import com.fishhawk.driftinglibraryandroid.PR
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.ui.base.toast
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationToolBar
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTransition
import kotlinx.coroutines.launch

@Composable
fun SettingAdvancedScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            ApplicationToolBar(
                title = stringResource(R.string.label_settings_advanced),
                navController = navController
            )
        },
        content = { ApplicationTransition { Content() } }
    )
}

@Composable
private fun Content() {
    Column(modifier = Modifier.fillMaxWidth()) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        TextPreference(
            title = stringResource(R.string.settings_clear_image_cache),
            summary = stringResource(R.string.settings_clear_image_cache_summary)
        ) {
            scope.launch {
                context.imageLoader.memoryCache.clear()
                CoilUtils.createDefaultCache(context).directory.deleteRecursively()
                context.toast(R.string.toast_cache_cleared)
            }
        }

        SwitchPreference(
            title = stringResource(R.string.settings_secure_mode),
            summary = stringResource(R.string.settings_secure_mode_summary),
            preference = PR.secureMode
        )
    }
}



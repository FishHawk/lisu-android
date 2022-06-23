package com.fishhawk.lisu.ui.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.R
import com.fishhawk.lisu.util.toast
import com.fishhawk.lisu.widget.LisuToolBar
import com.fishhawk.lisu.ui.theme.LisuTransition
import kotlinx.coroutines.launch

@Composable
fun SettingAdvancedScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            LisuToolBar(
                title = stringResource(R.string.label_settings_advanced),
                onNavUp = { navController.navigateUp() }
            )
        },
        content = { paddingValues ->
            LisuTransition {
                Content(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxWidth()
                )
            }
        }
    )
}

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun Content(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
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
}



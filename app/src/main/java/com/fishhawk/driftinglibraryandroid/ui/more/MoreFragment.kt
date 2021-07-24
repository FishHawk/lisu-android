package com.fishhawk.driftinglibraryandroid.ui.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationToolBar
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTransition

@Composable
fun MoreScreen(navController: NavHostController) {
    Scaffold(
        topBar = { ApplicationToolBar(stringResource(R.string.label_more)) },
        content = { ApplicationTransition { Content(navController) } }
    )
}

@Composable
private fun Content(navController: NavHostController) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TextPreference(
            icon = Icons.Filled.Storage,
            title = stringResource(R.string.more_server)
        ) {
            navController.navigate("server")
        }
        Divider()
        TextPreference(
            icon = Icons.Filled.Tune,
            title = stringResource(R.string.more_setting_general)
        ) {
            navController.navigate("setting-general")
        }
        TextPreference(
            icon = Icons.Filled.Book,
            title = stringResource(R.string.more_setting_reader)
        ) {
            navController.navigate("setting-reader")
        }
        TextPreference(
            icon = Icons.Filled.Code,
            title = stringResource(R.string.more_setting_advanced)
        ) {
            navController.navigate("setting-advanced")
        }
        Divider()
        TextPreference(
            icon = Icons.Filled.Error,
            title = stringResource(R.string.more_about)
        ) {
            navController.navigate("about")
        }
    }
}
package com.fishhawk.lisu.ui.more

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.fishhawk.lisu.R
import com.fishhawk.lisu.ui.widget.LisuToolBar
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer

@Composable
fun OpenSourceLicenseScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            LisuToolBar(
                title = stringResource(R.string.label_open_source_license),
                onNavUp = { navController.navigateUp() }
            )
        },
        content = {
            LibrariesContainer(modifier = Modifier.fillMaxSize())
        }
    )
}
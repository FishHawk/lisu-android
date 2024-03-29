package com.fishhawk.lisu.ui.more

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.fishhawk.lisu.R
import com.fishhawk.lisu.widget.LisuScaffold
import com.fishhawk.lisu.widget.LisuToolBar
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenSourceLicenseScreen(navController: NavHostController) {
    LisuScaffold(
        topBar = {
            LisuToolBar(
                title = stringResource(R.string.label_open_source_license),
                onNavUp = { navController.navigateUp() }
            )
        },
        content = { paddingValues ->
            LibrariesContainer(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            )
        }
    )
}
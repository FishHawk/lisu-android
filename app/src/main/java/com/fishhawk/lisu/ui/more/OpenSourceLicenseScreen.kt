package com.fishhawk.lisu.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fishhawk.lisu.R
import com.fishhawk.lisu.ui.base.openWebPage
import com.fishhawk.lisu.ui.theme.LisuToolBar
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library

@Composable
fun OpenSourceLicenseScreen(navController: NavHostController) {
    val context = LocalContext.current
    val libraries = Libs(context).libraries.filter { !it.isInternal }

    Scaffold(
        topBar = {
            LisuToolBar(
                title = stringResource(R.string.label_open_source_license),
                onNavUp = { navController.navigateUp() }
            )
        },
        content = {
            LisuTransition {
                LazyColumn {
                    items(libraries) { LibraryCard(it) }
                }
            }
        }
    )
}

@Composable
internal fun LibraryCard(lib: Library) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { context.openWebPage(lib.libraryWebsite) }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = lib.libraryName, style = MaterialTheme.typography.subtitle2)
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(text = lib.libraryArtifactId, style = MaterialTheme.typography.body2)
            lib.licenses?.joinToString { it.licenseName }?.let {
                Text(text = it, style = MaterialTheme.typography.body2)
            }
        }
    }
}


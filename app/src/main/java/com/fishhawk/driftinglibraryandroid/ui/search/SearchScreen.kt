package com.fishhawk.driftinglibraryandroid.ui.search

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.ui.base.MangaDisplayModeButton
import com.fishhawk.driftinglibraryandroid.ui.base.RefreshableMangaList
import com.fishhawk.driftinglibraryandroid.ui.base.navToReaderActivity
import com.fishhawk.driftinglibraryandroid.ui.provider.ProviderActionSheet
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar

@Composable
fun SearchScreen(navController: NavHostController) {
    navController.previousBackStackEntry!!.arguments!!.getString("keywords").let {
        navController.currentBackStackEntry!!.arguments!!.putString("keywords", it)
    }
    navController.previousBackStackEntry!!.arguments!!.getParcelable<ProviderInfo>("provider").let {
        navController.currentBackStackEntry!!.arguments!!.putParcelable("provider", it)
    }

    Scaffold(
        topBar = { ToolBar() },
        content = { Content(navController) }
    )
}

@Composable
private fun ToolBar() {
    TopAppBar(
        backgroundColor = MaterialTheme.colors.surface,
        contentPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.statusBars),
        title = { Text(stringResource(R.string.label_search)) },
        actions = {
            // queryHint = getString(R.string.menu_search_hint)
            // setQuery(viewModel.keywords.value, false)
            IconButton(onClick = { }) {
                Icon(Icons.Filled.Search, contentDescription = "search")
            }
            MangaDisplayModeButton()
        }
    )
}

@Composable
private fun Content(navController: NavHostController) {
    val viewModel = hiltViewModel<SearchViewModel>()
    val context = LocalContext.current
    RefreshableMangaList(
        mangaList = viewModel.mangaList.collectAsLazyPagingItems(),
        onCardClick = {
            navController.currentBackStackEntry?.arguments =
                bundleOf(
                    "outline" to it,
                    "provider" to viewModel.provider
                )
            navController.navigate("gallery/${it.id}")
        },
        onCardLongClick = {
            ProviderActionSheet(
                context,
                it,
                viewModel.provider.id,
                object : ProviderActionSheet.Listener {
                    override fun onReadClick(outline: MangaOutline, provider: String) {
                        context.navToReaderActivity(outline.id, viewModel.provider.id, 0, 0, 0)
                    }

                    override fun onLibraryAddClick(outline: MangaOutline, provider: String) {
                        viewModel.addToLibrary(outline.id, outline.title)
                    }
                }
            ).show()
        }
    )
}

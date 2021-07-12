package com.fishhawk.driftinglibraryandroid.ui.globalsearch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.ui.base.ErrorItem
import com.fishhawk.driftinglibraryandroid.ui.base.MangaCardGrid
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTransition
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar

@Composable
fun GlobalSearchScreen(navController: NavHostController) {
    navController.previousBackStackEntry!!.arguments!!.getString("keywords").let {
        navController.currentBackStackEntry!!.arguments!!.putString("keywords", it)
    }

    Scaffold(
        topBar = { ToolBar(navController) },
        content = { ApplicationTransition { Content(navController) } }
    )
}

@Composable
private fun ToolBar(navController: NavHostController) {
    TopAppBar(
        backgroundColor = MaterialTheme.colors.surface,
        contentPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.statusBars),
        title = { Text(stringResource(R.string.label_global_search)) },
        actions = {
            // queryHint = getString(R.string.menu_search_global_hint)
            // setQuery(viewModel.keywords.value, false)
            IconButton(onClick = { }) {
                Icon(Icons.Filled.Search, contentDescription = "search")
            }
        },
        navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.Filled.NavigateBefore, "back")
            }
        }
    )
}

@Composable
private fun Content(navController: NavHostController) {
    val viewModel = hiltViewModel<GlobalSearchViewModel>()
    val searchGroupList by viewModel.searchGroupList.observeAsState(listOf())
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(searchGroupList) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(it.provider.title)
                    Spacer(modifier = Modifier.weight(1f, fill = true))
                    IconButton(onClick = {
                        navController.currentBackStackEntry?.arguments =
                            bundleOf(
                                "keywords" to viewModel.keywords.value,
                                "provider" to it.provider
                            )
                        navController.navigate("search/${it.provider.id}")
                    }) { Icon(Icons.Filled.NavigateNext, "Forward") }
                }
                val provider = it.provider
                it.result?.fold({
                    LazyRow(
                        modifier = Modifier.height(140.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(it) {
                            Box(
                                modifier = Modifier.weight(1f, fill = true),
                                propagateMinConstraints = true
                            ) {
                                MangaCardGrid(it, onCardClick = {
                                    navController.currentBackStackEntry?.arguments =
                                        bundleOf(
                                            "outline" to it,
                                            "provider" to provider
                                        )
                                    navController.navigate("gallery/${it.id}")
                                })
                            }
                        }
                    }
                }, { ErrorItem(message = it.message ?: "", onClickRetry = {}) }
                )
            }
        }
    }
}

package com.fishhawk.driftinglibraryandroid.ui.globalsearch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.ui.base.ErrorItem
import com.fishhawk.driftinglibraryandroid.ui.base.MangaListCard
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationToolBar
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTransition

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
    ApplicationToolBar(stringResource(R.string.label_global_search), navController) {
        // queryHint = getString(R.string.menu_search_global_hint)
        // setQuery(viewModel.keywords.value, false)
        IconButton(onClick = { }) {
            Icon(Icons.Filled.Search, contentDescription = "search")
        }
    }
}

@Composable
private fun Content(navController: NavHostController) {
    val viewModel = hiltViewModel<GlobalSearchViewModel>()
    val searchGroupList by viewModel.searchGroupList.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(searchGroupList) {
            Column {
                val searchGroup by it.collectAsState()

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(searchGroup.provider.title)
                    Spacer(modifier = Modifier.weight(1f, fill = true))
                    IconButton(onClick = {
                        navController.currentBackStackEntry?.arguments =
                            bundleOf(
                                "keywords" to viewModel.keywords.value,
                                "provider" to searchGroup.provider
                            )
                        navController.navigate("search/${searchGroup.provider.id}")
                    }) { Icon(Icons.Filled.NavigateNext, "Forward") }
                }

                searchGroup.result?.getOrNull()?.let {
                    LazyRow(
                        modifier = Modifier.height(140.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(it) {
                            Box(
                                modifier = Modifier.weight(1f, fill = true),
                                propagateMinConstraints = true
                            ) {
                                MangaListCard(it, onCardClick = {
                                    navController.currentBackStackEntry?.arguments =
                                        bundleOf(
                                            "outline" to it,
                                            "provider" to searchGroup.provider
                                        )
                                    navController.navigate("gallery/${it.id}")
                                })
                            }
                        }
                    }
                }
                searchGroup.result?.exceptionOrNull()?.let {
                    ErrorItem(it) {}
                }
            }
        }
    }
}

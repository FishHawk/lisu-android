package com.fishhawk.lisu.ui.globalsearch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.ui.base.MangaListCard
import com.fishhawk.lisu.ui.main.navToGallery
import com.fishhawk.lisu.ui.main.navToProviderSearch
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.ui.widget.*
import kotlinx.coroutines.flow.StateFlow
import org.koin.androidx.compose.viewModel
import org.koin.core.parameter.parametersOf

private typealias GlobalSearchActionHandler = (GlobalSearchAction) -> Unit

private sealed interface GlobalSearchAction {
    object NavUp : GlobalSearchAction
    data class NavToGallery(val manga: MangaDto) : GlobalSearchAction
    data class NavToProviderSearch(val providerId: String) : GlobalSearchAction
    data class Search(val keywords: String) : GlobalSearchAction
}

@Composable
fun GlobalSearchScreen(navController: NavHostController) {
    val viewModel by viewModel<GlobalSearchViewModel> {
        parametersOf(navController.currentBackStackEntry!!.arguments!!)
    }
    val keywords by viewModel.keywords.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val searchResultList by viewModel.searchResultList.collectAsState()

    val onAction: GlobalSearchActionHandler = { action ->
        when (action) {
            GlobalSearchAction.NavUp -> navController.navigateUp()
            is GlobalSearchAction.NavToGallery ->
                navController.navToGallery(action.manga)
            is GlobalSearchAction.NavToProviderSearch ->
                navController.navToProviderSearch(action.providerId, keywords)
            is GlobalSearchAction.Search -> viewModel.search(action.keywords)
        }
    }

    var editing by remember { mutableStateOf(viewModel.keywords.value.isBlank()) }
    var editingKeywords by remember { mutableStateOf(viewModel.keywords.value) }

    Scaffold(
        topBar = {
            LisuToolBar(
                title = keywords,
                onNavUp = { onAction(GlobalSearchAction.NavUp) },
            ) {
                IconButton(onClick = { editing = true }) {
                    Icon(Icons.Default.Search, stringResource(R.string.action_search))
                }
            }
            LisuSearchToolBar(
                visible = editing,
                value = editingKeywords,
                onValueChange = { editingKeywords = it },
                onSearch = {
                    onAction(GlobalSearchAction.Search(it))
                    editing = false
                },
                onDismiss = {
                    if (keywords.isBlank()) onAction(GlobalSearchAction.NavUp)
                    else editing = false
                },
                placeholder = { Text(stringResource(R.string.search_global_hint)) }
            )
        },
        content = {
            LisuTransition {
                SearchResultList(
                    searchResultList = searchResultList,
                    onAction = onAction
                )
                SuggestionList(
                    editing = editing,
                    keywords = editingKeywords,
                    suggestions = suggestions,
                    onSuggestionSelected = { editingKeywords = it }
                )
            }
        }
    )
}

@Composable
private fun SearchResultList(
    searchResultList: List<StateFlow<SearchResult>>,
    onAction: GlobalSearchActionHandler
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(searchResultList) {
            val searchResult by it.collectAsState()
            SearchResultItem(searchResult, onAction)
        }
    }
}

@Composable
private fun SearchResultItem(
    searchResult: SearchResult,
    onAction: GlobalSearchActionHandler
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = searchResult.provider.run { "$id($lang)" },
                style = MaterialTheme.typography.body2
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {
                onAction(GlobalSearchAction.NavToProviderSearch(searchResult.provider.id))
            }) {
                Icon(Icons.Filled.ArrowForward, null)
            }
        }

        when (searchResult.viewState) {
            ViewState.Loading -> LoadingItem()
            is ViewState.Failure -> NoResultFound()
            ViewState.Loaded -> {
                if (searchResult.mangas.isEmpty()) NoResultFound()
                else LazyRow(
                    modifier = Modifier.height(140.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(searchResult.mangas) {
                        MangaListCard(it, onCardClick = { manga ->
                            onAction(GlobalSearchAction.NavToGallery(manga))
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun NoResultFound() {
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
        Text(
            text = "No result found.",
            style = MaterialTheme.typography.body2
        )
    }
}

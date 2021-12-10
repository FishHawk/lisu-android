package com.fishhawk.lisu.ui.globalsearch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.ui.base.MangaListCard
import com.fishhawk.lisu.ui.navToGallery
import com.fishhawk.lisu.ui.navToProviderSearch
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.ui.widget.LisuSearchToolBar
import com.fishhawk.lisu.ui.widget.LoadingItem
import com.fishhawk.lisu.ui.widget.SuggestionList
import com.fishhawk.lisu.ui.widget.ViewState
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
    val suggestions by viewModel.suggestions.collectAsState()
    val searchResultList by viewModel.searchResultList.collectAsState()

    var editingKeywords by remember { mutableStateOf(viewModel.keywords.value ?: "") }
    var editing by remember { mutableStateOf(viewModel.keywords.value == null) }

    val onAction: GlobalSearchActionHandler = { action ->
        when (action) {
            GlobalSearchAction.NavUp -> navController.navigateUp()
            is GlobalSearchAction.NavToGallery ->
                navController.navToGallery(action.manga)
            is GlobalSearchAction.NavToProviderSearch ->
                navController.navToProviderSearch(action.providerId, editingKeywords)
            is GlobalSearchAction.Search -> viewModel.search(action.keywords)
        }
    }

    Scaffold(
        topBar = {
            LisuSearchToolBar(
                onSearch = { onAction(GlobalSearchAction.Search(editingKeywords)) },
                value = editingKeywords,
                onValueChange = { editingKeywords = it },
                editing = editing,
                onEditingChange = { editing = it },
                placeholder = { Text(stringResource(R.string.menu_search_global_hint)) },
                onNavUp = { onAction(GlobalSearchAction.NavUp) }
            )
        },
        content = {
            LisuTransition {
                SearchResultList(searchResultList, onAction)
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
            }) { Icon(Icons.Filled.ArrowForward, "Forward") }
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

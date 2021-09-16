package com.fishhawk.driftinglibraryandroid.ui.globalsearch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.Provider
import com.fishhawk.driftinglibraryandroid.ui.activity.setString
import com.fishhawk.driftinglibraryandroid.ui.base.LoadingItem
import com.fishhawk.driftinglibraryandroid.ui.base.MangaListCard
import com.fishhawk.driftinglibraryandroid.ui.base.ViewState
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationToolBar
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTransition
import kotlinx.coroutines.flow.StateFlow

private typealias GlobalSearchActionHandler = (GlobalSearchAction) -> Unit

private sealed interface GlobalSearchAction {
    object NavUp : GlobalSearchAction

    data class NavToGallery(
        val provider: Provider,
        val outline: MangaOutline
    ) : GlobalSearchAction

    data class NavToProviderSearch(
        val provider: Provider
    ) : GlobalSearchAction

    data class Search(
        val keywords: String
    ) : GlobalSearchAction
}

@Composable
fun GlobalSearchScreen(navController: NavHostController) {
    navController.setString("keywords")

    val viewModel = hiltViewModel<GlobalSearchViewModel>()
    val initKeywords = viewModel.keywords.value
    val searchResultList by viewModel.searchResultList.collectAsState()
    val onAction: GlobalSearchActionHandler = { action ->
        when (action) {
            GlobalSearchAction.NavUp -> navController.navigateUp()
            is GlobalSearchAction.NavToGallery -> navController.apply {
                currentBackStackEntry?.arguments =
                    bundleOf(
                        "outline" to action.outline,
                        "provider" to action.provider
                    )
                navigate("gallery/${action.outline.id}")
            }
            is GlobalSearchAction.NavToProviderSearch -> navController.apply {
                currentBackStackEntry?.arguments =
                    bundleOf(
                        "keywords" to viewModel.keywords.value,
                        "provider" to action.provider
                    )
                navigate("search/${action.provider.name}")
            }
            is GlobalSearchAction.Search -> viewModel.search(action.keywords)
        }
    }

    Scaffold(
        topBar = { ToolBar(initKeywords, onAction) },
        content = { ApplicationTransition { SearchResultList(searchResultList, onAction) } }
    )
}

@Composable
private fun ToolBar(
    initKeywords: String?,
    onAction: GlobalSearchActionHandler
) {
    ApplicationToolBar(onNavigationIconClick = { onAction(GlobalSearchAction.NavUp) }) {
        val focusManager = LocalFocusManager.current
        val focusRequester = remember { FocusRequester() }
        var keywords by remember { mutableStateOf(TextFieldValue(initKeywords ?: "")) }
        TextField(
            modifier = Modifier.focusRequester(focusRequester),
            value = keywords,
            onValueChange = { keywords = it },
            singleLine = true,
            placeholder = { Text(stringResource(R.string.menu_search_global_hint)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                onAction(GlobalSearchAction.Search(keywords.text))
                focusManager.clearFocus()
            }),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        DisposableEffect(Unit) {
            if (initKeywords == null) focusRequester.requestFocus()
            onDispose { }
        }
    }
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
                text = searchResult.provider.run { "$name($lang)" },
                style = MaterialTheme.typography.body2
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {
                onAction(GlobalSearchAction.NavToProviderSearch(provider = searchResult.provider))
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
                        MangaListCard(it, onCardClick = { outline ->
                            onAction(
                                GlobalSearchAction.NavToGallery(
                                    provider = searchResult.provider,
                                    outline = outline
                                )
                            )
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

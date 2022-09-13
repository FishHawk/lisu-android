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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.network.model.BoardId
import com.fishhawk.lisu.data.network.model.MangaDto
import com.fishhawk.lisu.ui.main.navToGallery
import com.fishhawk.lisu.ui.main.navToProvider
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.widget.*
import org.koin.androidx.compose.viewModel
import org.koin.core.parameter.parametersOf

private typealias GlobalSearchActionHandler = (GlobalSearchAction) -> Unit

private sealed interface GlobalSearchAction {
    object NavUp : GlobalSearchAction
    data class NavToGallery(val manga: MangaDto) : GlobalSearchAction
    data class NavToProvider(val providerId: String, val boardId: BoardId) : GlobalSearchAction
    data class Search(val keywords: String) : GlobalSearchAction
    data class Reload(val providerId: String) : GlobalSearchAction
    object ReloadProviders : GlobalSearchAction
}

@Composable
fun GlobalSearchScreen(navController: NavHostController) {
    val viewModel by viewModel<GlobalSearchViewModel> {
        parametersOf(navController.currentBackStackEntry!!.arguments!!)
    }
    val keywords by viewModel.keywords.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val searchRecordsResult by viewModel.searchRecordsResult.collectAsState()

    val onAction: GlobalSearchActionHandler = { action ->
        when (action) {
            GlobalSearchAction.NavUp -> navController.navigateUp()
            is GlobalSearchAction.NavToGallery ->
                navController.navToGallery(action.manga)
            is GlobalSearchAction.NavToProvider ->
                navController.navToProvider(action.providerId, action.boardId, keywords)
            is GlobalSearchAction.Search ->
                viewModel.search(action.keywords)
            is GlobalSearchAction.Reload ->
                viewModel.reload(action.providerId)
            GlobalSearchAction.ReloadProviders ->
                viewModel.reloadProviders()
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
                    if (it.isNotBlank()) {
                        onAction(GlobalSearchAction.Search(it))
                        editing = false
                    }
                },
                onDismiss = {
                    if (keywords.isBlank()) onAction(GlobalSearchAction.NavUp)
                    else editing = false
                },
                placeholder = { Text(stringResource(R.string.search_global_hint)) }
            )
        },
        content = { paddingValues ->
            LisuTransition {
                SearchRecordList(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    searchRecordsResult = searchRecordsResult,
                    onAction = onAction
                )
                SuggestionList(
                    visible = editing,
                    onDismiss = {
                        editing = false
                        if (keywords.isBlank()) onAction(GlobalSearchAction.NavUp)
                    },
                    keywords = editingKeywords,
                    suggestions = suggestions,
                    onSuggestionSelected = { editingKeywords = it }
                )
            }
        }
    )
}

@Composable
private fun SearchRecordList(
    searchRecordsResult: Result<List<SearchRecord>>?,
    onAction: GlobalSearchActionHandler,
    modifier: Modifier = Modifier,
) {
    StateView(
        modifier = modifier,
        result = searchRecordsResult,
        onRetry = { onAction(GlobalSearchAction.ReloadProviders) },
    ) { searchRecords ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(searchRecords) {
                SearchRecord(it, onAction)
            }
        }
    }
}

@Composable
private fun SearchRecord(
    searchRecord: SearchRecord,
    onAction: GlobalSearchActionHandler,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = searchRecord.provider.run { "$id($lang)" },
                style = MaterialTheme.typography.body2
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {
                onAction(
                    GlobalSearchAction.NavToProvider(
                        searchRecord.provider.id,
                        searchRecord.provider.searchBoardId!!,
                    )
                )
            }) {
                Icon(Icons.Filled.ArrowForward, null)
            }
        }

        searchRecord.remoteList.value
            ?.onSuccess { mangaList ->
                if (mangaList.list.isEmpty()) NoResultFound()
                else LazyRow(
                    modifier = Modifier.height(140.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(mangaList.list) { manga ->
                        MangaCard(
                            manga = manga,
                            onClick = { onAction(GlobalSearchAction.NavToGallery(it)) },
                        )
                    }
                }
            }
            ?.onFailure { throwable ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            Text(
                                text = throwable.localizedMessage
                                    ?: stringResource(R.string.unknown_error),
                                style = MaterialTheme.typography.caption,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    TextButton(onClick = { onAction(GlobalSearchAction.Reload(searchRecord.provider.id)) }) {
                        Text(text = stringResource(R.string.action_retry))
                    }
                }
            }
            ?: CircularProgressIndicator(modifier = Modifier.size(24.dp))
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

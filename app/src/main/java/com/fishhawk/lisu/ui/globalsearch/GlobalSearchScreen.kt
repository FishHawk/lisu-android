package com.fishhawk.lisu.ui.globalsearch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
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
import com.fishhawk.lisu.ui.theme.MediumEmphasis
import com.fishhawk.lisu.widget.*
import org.koin.androidx.compose.viewModel
import org.koin.core.parameter.parametersOf

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

    val onAction: (GlobalSearchAction) -> Unit = { action ->
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

    GlobalSearchScaffold(
        keywords = keywords,
        suggestions = suggestions,
        searchRecordsResult = searchRecordsResult,
        onAction = onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlobalSearchScaffold(
    keywords: String,
    suggestions: List<String>,
    searchRecordsResult: Result<List<SearchRecord>>?,
    onAction: (GlobalSearchAction) -> Unit,
) {
    val searchAndWaitInput = keywords.isBlank()
    var editing by remember { mutableStateOf(searchAndWaitInput) }
    var editingKeywords by remember { mutableStateOf(keywords) }

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
                    if (searchAndWaitInput) onAction(GlobalSearchAction.NavUp)
                    else editing = false
                },
                placeholder = { Text(stringResource(R.string.search_global_hint)) }
            )
        },
        content = { paddingValues ->
            LisuTransition {
                Box(modifier = Modifier.padding(paddingValues)) {
                    if (!searchAndWaitInput) {
                        StateView(
                            result = searchRecordsResult,
                            onRetry = { onAction(GlobalSearchAction.ReloadProviders) },
                            modifier = Modifier.fillMaxSize(),
                        ) { searchRecords, modifier ->
                            SearchRecordList(
                                searchRecords = searchRecords,
                                onAction = onAction,
                                modifier = modifier,
                            )
                        }
                    }
                    SuggestionList(
                        visible = editing,
                        onDismiss = {
                            editing = false
                            if (searchAndWaitInput) onAction(GlobalSearchAction.NavUp)
                        },
                        keywords = editingKeywords,
                        suggestions = suggestions,
                        onSuggestionSelected = { editingKeywords = it }
                    )
                }
            }
        }
    )
}

@Composable
private fun SearchRecordList(
    searchRecords: List<SearchRecord>,
    onAction: (GlobalSearchAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(searchRecords) {
            SearchRecord(it, onAction)
        }
    }
}

@Composable
private fun SearchRecord(
    searchRecord: SearchRecord,
    onAction: (GlobalSearchAction) -> Unit,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = searchRecord.provider.run { "$id($lang)" },
                style = MaterialTheme.typography.bodyMedium,
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

        searchRecord.remoteList.value?.onSuccess { mangaList ->
            if (mangaList.list.isEmpty()) {
                MediumEmphasis {
                    Text(
                        text = "No result found.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(mangaList.list) { manga ->
                        MangaCard(
                            manga = manga,
                            modifier = Modifier
                                .width(104.dp)
                                .clickable { onAction(GlobalSearchAction.NavToGallery(manga)) },
                        )
                    }
                }
            }
        }?.onFailure { throwable ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    MediumEmphasis {
                        Text(
                            text = throwable.localizedMessage
                                ?: stringResource(R.string.unknown_error),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                TextButton(onClick = {
                    onAction(GlobalSearchAction.Reload(searchRecord.provider.id))
                }) {
                    Text(text = stringResource(R.string.action_retry))
                }
            }
        } ?: CircularProgressIndicator(modifier = Modifier.size(24.dp))
    }
}
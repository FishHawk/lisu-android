package com.fishhawk.lisu.ui.provider

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.data.remote.model.MangaState
import com.fishhawk.lisu.ui.base.MangaBadge
import com.fishhawk.lisu.ui.base.RefreshableMangaList
import com.fishhawk.lisu.ui.main.navToGallery
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.ui.widget.LisuDialog
import com.fishhawk.lisu.ui.widget.LisuSearchToolBar
import com.fishhawk.lisu.ui.widget.LisuToolBar
import com.fishhawk.lisu.ui.widget.SuggestionList
import org.koin.androidx.compose.viewModel
import org.koin.core.parameter.parametersOf

private typealias SearchActionHandler = (SearchAction) -> Unit

private sealed interface SearchAction {
    object NavUp : SearchAction
    data class NavToGallery(val manga: MangaDto) : SearchAction
    data class Search(val keywords: String) : SearchAction
    data class DeleteSuggestion(val keywords: String) : SearchAction
    data class AddToLibrary(val manga: MangaDto) : SearchAction
    data class RemoveFromLibrary(val manga: MangaDto) : SearchAction
}

@Composable
fun ProviderSearchScreen(navController: NavHostController) {
    val viewModel by viewModel<ProviderSearchViewModel> {
        parametersOf(navController.currentBackStackEntry!!.arguments!!)
    }
    val keywords by viewModel.keywords.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val mangaList = viewModel.mangaList.collectAsLazyPagingItems()
    var addDialogManga by remember { mutableStateOf<MangaDto?>(null) }
    var removeDialogManga by remember { mutableStateOf<MangaDto?>(null) }

    val onAction: SearchActionHandler = { action ->
        when (action) {
            SearchAction.NavUp -> navController.navigateUp()
            is SearchAction.NavToGallery -> navController.navToGallery(action.manga)
            is SearchAction.Search -> viewModel.search(action.keywords)
            is SearchAction.DeleteSuggestion -> viewModel.deleteSuggestion(action.keywords)
            is SearchAction.AddToLibrary -> viewModel.addToLibrary(action.manga)
            is SearchAction.RemoveFromLibrary -> viewModel.removeFromLibrary(action.manga)
        }
    }

    var editingKeywords by remember { mutableStateOf(viewModel.keywords.value) }
    var editing by remember { mutableStateOf(viewModel.keywords.value.isBlank()) }

    Scaffold(
        topBar = {
            LisuToolBar(
                title = keywords,
                onNavUp = { onAction(SearchAction.NavUp) },
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
                        onAction(SearchAction.Search(it))
                        editing = false
                    }
                },
                onDismiss = {
                    if (keywords.isBlank()) onAction(SearchAction.NavUp)
                    else editing = false
                },
                placeholder = { Text(stringResource(R.string.search_hint)) }
            )
        },
        content = { paddingValues ->
            LisuTransition {
                RefreshableMangaList(
                    modifier = Modifier.padding(paddingValues),
                    mangaList = mangaList,
                    decorator = {
                        if (it != null && it.state == MangaState.RemoteInLibrary) {
                            MangaBadge(text = "in library")
                        }
                    },
                    onCardClick = { onAction(SearchAction.NavToGallery(it)) },
                    onCardLongClick = {
                        if (it.state == MangaState.RemoteInLibrary) removeDialogManga = it
                        else addDialogManga = it
                    }
                )
                SuggestionList(
                    visible = editing,
                    keywords = editingKeywords,
                    suggestions = suggestions,
                    onSuggestionSelected = { editingKeywords = it },
                    onSuggestionDeleted = { onAction(SearchAction.DeleteSuggestion(it)) }
                )
            }
        }
    )

    addDialogManga?.let {
        LisuDialog(
            title = it.titleOrId,
            confirmText = "Add to library",
            onConfirm = { onAction(SearchAction.AddToLibrary(it)) },
            onDismiss = { addDialogManga = null },
        )
    }

    removeDialogManga?.let {
        LisuDialog(
            title = it.titleOrId,
            confirmText = "Remove from library",
            onConfirm = { onAction(SearchAction.RemoveFromLibrary(it)) },
            onDismiss = { removeDialogManga = null },
        )
    }
}

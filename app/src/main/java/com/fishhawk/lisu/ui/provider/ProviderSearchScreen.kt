package com.fishhawk.lisu.ui.provider

import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.ui.base.RefreshableMangaList
import com.fishhawk.lisu.ui.main.navToGallery
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.ui.widget.LisuSearchToolBar
import com.fishhawk.lisu.ui.widget.SuggestionList
import org.koin.androidx.compose.viewModel
import org.koin.core.parameter.parametersOf

private typealias SearchActionHandler = (SearchAction) -> Unit

private sealed interface SearchAction {
    object NavUp : SearchAction
    data class NavToGallery(val manga: MangaDto) : SearchAction
    data class Search(val keywords: String) : SearchAction
    data class DeleteSuggestion(val keywords: String) : SearchAction
    data class OpenSheet(val manga: MangaDto) : SearchAction
}

@Composable
fun ProviderSearchScreen(navController: NavHostController) {
    val viewModel by viewModel<ProviderSearchViewModel> {
        parametersOf(navController.currentBackStackEntry!!.arguments!!)
    }
    val suggestions by viewModel.suggestions.collectAsState()
    val mangaList = viewModel.mangaList.collectAsLazyPagingItems()

    var editingKeywords by remember { mutableStateOf(viewModel.keywords.value ?: "") }
    var editing by remember { mutableStateOf(viewModel.keywords.value == null) }

    val onAction: SearchActionHandler = { action ->
        when (action) {
            SearchAction.NavUp -> navController.navigateUp()
            is SearchAction.NavToGallery -> navController.navToGallery(action.manga)
            is SearchAction.Search -> viewModel.search(action.keywords)
            is SearchAction.DeleteSuggestion -> viewModel.deleteSuggestion(action.keywords)
            is SearchAction.OpenSheet -> Unit
        }
    }

    Scaffold(
        topBar = {
            LisuSearchToolBar(
                onSearch = { onAction(SearchAction.Search(editingKeywords)) },
                value = editingKeywords,
                onValueChange = { editingKeywords = it },
                editing = editing,
                onEditingChange = { editing = it },
                placeholder = { Text(stringResource(R.string.menu_search_global_hint)) },
                onNavUp = { onAction(SearchAction.NavUp) }
            )
        },
        content = {
            LisuTransition {
                MangaList(mangaList, onAction)
                SuggestionList(
                    editing = editing,
                    keywords = editingKeywords,
                    suggestions = suggestions,
                    onSuggestionSelected = { editingKeywords = it },
                    onSuggestionDeleted = {
                        onAction(SearchAction.DeleteSuggestion(it))
                    }
                )
            }
        }
    )
}

@Composable
private fun MangaList(
    mangaList: LazyPagingItems<MangaDto>,
    onAction: SearchActionHandler
) {
    RefreshableMangaList(
        mangaList = mangaList,
        onCardClick = { onAction(SearchAction.NavToGallery(it)) },
        onCardLongClick = { onAction(SearchAction.OpenSheet(it)) }
    )
}

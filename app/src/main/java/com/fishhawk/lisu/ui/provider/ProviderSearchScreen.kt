package com.fishhawk.lisu.ui.provider

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.ui.base.RefreshableMangaList
import com.fishhawk.lisu.ui.navToGallery
import com.fishhawk.lisu.ui.theme.LisuToolBar
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.ui.widget.TextFieldWithSuggestions
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
    val keywords by viewModel.keywords.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val mangaList = viewModel.mangaList.collectAsLazyPagingItems()

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
        topBar = { ToolBar(keywords, suggestions, onAction) },
        content = { LisuTransition { MangaList(mangaList, onAction) } }
    )
}

@Composable
private fun ToolBar(
    initKeywords: String?,
    suggestions: List<String>,
    onAction: SearchActionHandler
) {
    LisuToolBar(onNavUp = { onAction(SearchAction.NavUp) }) {
        var keywords by remember { mutableStateOf(initKeywords ?: "") }
        val focusManager = LocalFocusManager.current
        val focusRequester = remember { FocusRequester() }

        TextFieldWithSuggestions(
            value = keywords,
            onValueChange = { keywords = it },
            suggestions = suggestions,
            modifier = Modifier.focusRequester(focusRequester),
            placeholder = { Text(stringResource(R.string.menu_search_hint)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions {
                onAction(SearchAction.Search(keywords))
                focusManager.clearFocus()
            },
            onSuggestionDeleted = {
                onAction(SearchAction.DeleteSuggestion(it))
            }
        )
        DisposableEffect(Unit) {
            if (initKeywords == null) focusRequester.requestFocus()
            onDispose { }
        }
    }
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

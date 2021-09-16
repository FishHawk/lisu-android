package com.fishhawk.driftinglibraryandroid.ui.search

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.Provider
import com.fishhawk.driftinglibraryandroid.ui.activity.setArgument
import com.fishhawk.driftinglibraryandroid.ui.activity.setString
import com.fishhawk.driftinglibraryandroid.ui.base.RefreshableMangaList
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationToolBar
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTransition

private typealias SearchActionHandler = (SearchAction) -> Unit

private sealed interface SearchAction {
    object NavUp : SearchAction
    data class NavToGallery(val outline: MangaOutline) : SearchAction
    data class Search(val keywords: String) : SearchAction
    data class OpenSheet(val outline: MangaOutline) : SearchAction
}

@Composable
fun SearchScreen(navController: NavHostController) {
    navController.setString("keywords")
    navController.setArgument<Provider>("provider")

    val viewModel = hiltViewModel<SearchViewModel>()
    val initKeywords = viewModel.keywords.value
    val mangaList = viewModel.mangaList.collectAsLazyPagingItems()
    val onAction: SearchActionHandler = { action ->
        when (action) {
            SearchAction.NavUp -> navController.navigateUp()
            is SearchAction.NavToGallery -> navController.apply {
                currentBackStackEntry?.arguments =
                    bundleOf(
                        "outline" to action.outline,
                        "provider" to viewModel.provider
                    )
                navigate("gallery/${action.outline.id}")
            }
            is SearchAction.Search -> viewModel.search(action.keywords)
            is SearchAction.OpenSheet -> Unit
//override fun onReadClick(outline: MangaOutline, provider: String) {
//  context.navToReaderActivity(outline.id, viewModel.provider.id, 0, 0, 0)
//}
//
//override fun onLibraryAddClick(outline: MangaOutline, provider: String) {
//  viewModel.addToLibrary(outline.id, outline.title)
//}
        }
    }

    Scaffold(
        topBar = { ToolBar(initKeywords, onAction) },
        content = { ApplicationTransition { MangaList(mangaList, onAction) } }
    )
}

@Composable
private fun ToolBar(
    initKeywords: String?,
    onAction: SearchActionHandler
) {
    ApplicationToolBar(onNavigationIconClick = { onAction(SearchAction.NavUp) }) {
        val focusManager = LocalFocusManager.current
        val focusRequester = remember { FocusRequester() }
        var keywords by remember { mutableStateOf(TextFieldValue(initKeywords ?: "")) }
        TextField(
            modifier = Modifier.focusRequester(focusRequester),
            value = keywords,
            onValueChange = { keywords = it },
            singleLine = true,
            placeholder = { Text(stringResource(R.string.menu_search_hint)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                onAction(SearchAction.Search(keywords.text))
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
private fun MangaList(
    mangaList: LazyPagingItems<MangaOutline>,
    onAction: SearchActionHandler
) {
    RefreshableMangaList(
        mangaList = mangaList,
        onCardClick = { onAction(SearchAction.NavToGallery(it)) },
        onCardLongClick = { onAction(SearchAction.OpenSheet(it)) }
    )
}

package com.fishhawk.driftinglibraryandroid.ui.library

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.ui.base.MangaDisplayModeButton
import com.fishhawk.driftinglibraryandroid.ui.base.RefreshableMangaList
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationToolBar
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTransition

@Composable
fun LibraryScreen(navController: NavHostController) {
    // TODO : search
    //  queryHint = getString(R.string.menu_search_hint)
    //  setQuery(viewModel.keywords.value, false)
    Scaffold(
        topBar = { ToolBar() },
        content = { ApplicationTransition { Content(navController) } }
    )
}

@Composable
private fun ToolBar() {
    ApplicationToolBar(stringResource(R.string.label_library)) {
        IconButton(onClick = { }) {
            Icon(Icons.Filled.Search, contentDescription = "search")
        }
        MangaDisplayModeButton()
    }
}

@Composable
private fun Content(navController: NavHostController) {
    val viewModel = hiltViewModel<LibraryViewModel>()
    val isOpen = remember { mutableStateOf(false) }
    val id = remember { mutableStateOf("") }
    RefreshableMangaList(
        mangaList = viewModel.mangas.collectAsLazyPagingItems(),
        onCardClick = {
            navController.currentBackStackEntry?.arguments =
                bundleOf("outline" to it)
            navController.navigate("gallery/${it.id}")
        },
        onCardLongClick = {
            id.value = it.id
            isOpen.value = true
        }
    )
    DeleteMangaDialog(isOpen, id.value)
}

@Composable
private fun DeleteMangaDialog(isOpen: MutableState<Boolean>, id: String) {
    val viewModel = hiltViewModel<LibraryViewModel>()
    if (isOpen.value) {
        AlertDialog(
            modifier = Modifier.fillMaxWidth(0.8f),
            onDismissRequest = { isOpen.value = false },
            title = { Text(text = "Confirm to delete manga?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteManga(id)
                        isOpen.value = false
                    }) {
                    Text("ok")
                }
            },
            dismissButton = {
                TextButton(onClick = { isOpen.value = false }) {
                    Text("cancel")
                }
            }
        )
    }
}
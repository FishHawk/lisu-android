package com.fishhawk.lisu.ui.library

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.ui.base.RefreshableMangaList
import com.fishhawk.lisu.ui.theme.LisuToolBar
import com.fishhawk.lisu.ui.theme.LisuTransition

@Composable
fun LibraryScreen(navController: NavHostController) {
    // TODO : search
    //  queryHint = getString(R.string.menu_search_hint)
    //  setQuery(viewModel.keywords.value, false)
    Scaffold(
        topBar = { ToolBar() },
        content = { LisuTransition { Content(navController) } }
    )
}

@Composable
private fun ToolBar() {
    LisuToolBar(title = stringResource(R.string.label_library)) {
        IconButton(onClick = { }) {
            Icon(Icons.Filled.Search, contentDescription = "search")
        }
    }
}

@Composable
private fun Content(navController: NavHostController) {
    val viewModel = hiltViewModel<LibraryViewModel>()
    val isOpen = remember { mutableStateOf(false) }
    val mangaWaitToDelete = remember { mutableStateOf<MangaDto?>(null) }
    RefreshableMangaList(
        mangaList = viewModel.mangaList.collectAsLazyPagingItems(),
        onCardClick = {
            navController.currentBackStackEntry?.arguments =
                bundleOf("manga" to it)
            navController.navigate("gallery/${it.id}")
        },
        onCardLongClick = {
            mangaWaitToDelete.value = it
            isOpen.value = true
        }
    )
    mangaWaitToDelete.value?.let { DeleteMangaDialog(isOpen, it) }
}

@Composable
private fun DeleteMangaDialog(isOpen: MutableState<Boolean>, manga: MangaDto) {
    val viewModel = hiltViewModel<LibraryViewModel>()
    if (isOpen.value) {
        AlertDialog(
            onDismissRequest = { isOpen.value = false },
            title = { Text(text = "Confirm to delete manga?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteManga(manga)
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
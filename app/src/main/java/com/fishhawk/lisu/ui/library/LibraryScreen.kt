package com.fishhawk.lisu.ui.library

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.ui.base.RefreshableMangaList
import com.fishhawk.lisu.ui.theme.LisuToolBar
import com.fishhawk.lisu.ui.theme.LisuTransition
import kotlinx.coroutines.launch

private typealias LibraryActionHandler = (LibraryAction) -> Unit

private sealed interface LibraryAction {
    data class NavToGallery(val manga: MangaDto) : LibraryAction

    data class Search(val keywords: String) : LibraryAction
    object Random : LibraryAction
}

@Composable
fun LibraryScreen(navController: NavHostController) {
    val viewModel = hiltViewModel<LibraryViewModel>()
    val onAction: LibraryActionHandler = { action ->
        when (action) {
            is LibraryAction.NavToGallery -> {
                navController.currentBackStackEntry?.arguments =
                    bundleOf("manga" to action.manga)
                navController.navigate("gallery/${action.manga.id}")
            }

            is LibraryAction.Search -> {
            }
            LibraryAction.Random -> {
                viewModel.viewModelScope.launch {
                    viewModel.getRandomManga().onSuccess {
                        navController.currentBackStackEntry?.arguments =
                            bundleOf("manga" to it)
                        navController.navigate("gallery/${it.id}")
                    }
                }
            }
        }

    }
    Scaffold(
        topBar = { ToolBar(onAction) },
        content = { LisuTransition { Content(onAction) } }
    )
}

@Composable
private fun ToolBar(onAction: LibraryActionHandler) {
    LisuToolBar(title = stringResource(R.string.label_library)) {
        IconButton(onClick = { onAction(LibraryAction.Random) }) {
            Icon(Icons.Default.Casino, contentDescription = "random-pick")
        }
        IconButton(onClick = { }) {
            Icon(Icons.Default.Search, contentDescription = "search")
        }
    }
}

@Composable
private fun Content(onAction: LibraryActionHandler) {
    val viewModel = hiltViewModel<LibraryViewModel>()
    val isOpen = remember { mutableStateOf(false) }
    val mangaWaitToDelete = remember { mutableStateOf<MangaDto?>(null) }
    RefreshableMangaList(
        mangaList = viewModel.mangaList.collectAsLazyPagingItems(),
        onCardClick = { onAction(LibraryAction.NavToGallery(it)) },
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
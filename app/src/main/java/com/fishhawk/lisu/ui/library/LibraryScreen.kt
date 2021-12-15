package com.fishhawk.lisu.ui.library

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.material.*
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.datastore.collectAsState
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.ui.base.RefreshableMangaList
import com.fishhawk.lisu.ui.main.navToGallery
import com.fishhawk.lisu.ui.main.navToLibrarySearch
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.ui.widget.LisuToolBar
import com.fishhawk.lisu.util.toast
import kotlinx.coroutines.flow.collect
import org.koin.androidx.compose.viewModel

private typealias LibraryActionHandler = (LibraryAction) -> Unit

private sealed interface LibraryAction {
    object NavToSearch : LibraryAction
    data class NavToGallery(val manga: MangaDto) : LibraryAction
    data class RemoveFromLibrary(val mangaList: List<String>) : LibraryAction
    object Random : LibraryAction
}

@Composable
fun LibraryScreen(navController: NavHostController) {
    val context = LocalContext.current

    val viewModel by viewModel<LibraryViewModel>()
    val mangaList = viewModel.mangaList.collectAsLazyPagingItems()

    val onAction: LibraryActionHandler = { action ->
        when (action) {
            LibraryAction.NavToSearch -> navController.navToLibrarySearch()
            is LibraryAction.NavToGallery -> navController.navToGallery(action.manga)
            is LibraryAction.RemoveFromLibrary -> viewModel.deleteManga(action.mangaList)
            LibraryAction.Random -> viewModel.getRandomManga()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LibraryEffect.NavToGallery -> navController.navToGallery(effect.manga)
                is LibraryEffect.Toast -> context.toast(effect.message)
            }
        }
    }

    val selectedMangaList = remember { mutableStateListOf<String>() }
    Scaffold(
        topBar = {
            ToolBar(onAction)
            SelectingToolBar(selectedMangaList, onAction)
        },
        content = {
            LisuTransition {
                RefreshableMangaList(
                    mangaList = mangaList,
                    selectedMangaList = selectedMangaList,
                    onCardClick = {
                        if (selectedMangaList.isEmpty()) {
                            onAction(LibraryAction.NavToGallery(it))
                        } else {
                            if (selectedMangaList.contains(it.id)) {
                                selectedMangaList.remove(it.id)
                            } else {
                                selectedMangaList.add(it.id)
                            }
                        }
                    },
                    onCardLongClick = { manga ->
                        if (selectedMangaList.isEmpty()) {
                            selectedMangaList.add(manga.id)
                        } else {
                            val list = mangaList.itemSnapshotList.mapNotNull { it?.id }
                            val start = list.indexOf(selectedMangaList.last())
                            val end = list.indexOf(manga.id)
                            val pendingList = if (start > end) {
                                list.slice(end..start).reversed()
                            } else {
                                list.slice(start..end)
                            }
                            selectedMangaList.removeIf { pendingList.contains(it) }
                            selectedMangaList.addAll(pendingList)
                        }
                    }
                )
                BackHandler(selectedMangaList.isNotEmpty()) {
                    selectedMangaList.clear()
                }
            }
        }
    )
}

@Composable
private fun ToolBar(onAction: LibraryActionHandler) {
    LisuToolBar(title = stringResource(R.string.label_library)) {
        val isRandomButtonEnabled by PR.isRandomButtonEnabled.collectAsState()
        if (isRandomButtonEnabled) {
            IconButton(onClick = { onAction(LibraryAction.Random) }) {
                Icon(LisuIcons.Casino, stringResource(R.string.action_random_pick))
            }
        }
        IconButton(onClick = { onAction(LibraryAction.NavToSearch) }) {
            Icon(LisuIcons.Search, stringResource(R.string.action_search))
        }
    }
}

@Composable
private fun SelectingToolBar(
    selectedMangaList: SnapshotStateList<String>,
    onAction: LibraryActionHandler
) {
    var isOpen by remember { mutableStateOf(false) }
    if (isOpen) {
        DeleteMangaDialog(
            size = selectedMangaList.size,
            onDismiss = { isOpen = false },
            onConfirm = { onAction(LibraryAction.RemoveFromLibrary(selectedMangaList.toList())) }
        )
    }

    AnimatedVisibility(
        visible = selectedMangaList.isNotEmpty(),
        enter = fadeIn() + slideInVertically(),
        exit = slideOutVertically() + fadeOut(),
    ) {
        LisuToolBar(
            title = stringResource(R.string.library_n_mangas_selected).format(selectedMangaList.size),
            onNavUp = { selectedMangaList.clear() }
        ) {
            IconButton(onClick = { isOpen = true }) {
                Icon(LisuIcons.Delete, stringResource(R.string.action_remove_from_library))
            }
        }
    }
}

@Composable
private fun DeleteMangaDialog(
    size: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.library_remove_n_mangas_from_library).format(size)) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text(stringResource(R.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
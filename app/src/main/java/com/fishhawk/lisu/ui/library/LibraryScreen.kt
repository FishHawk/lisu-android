package com.fishhawk.lisu.ui.library

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.datastore.collectAsState
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.data.remote.model.MangaKeyDto
import com.fishhawk.lisu.ui.base.OnEvent
import com.fishhawk.lisu.ui.base.RefreshableMangaList
import com.fishhawk.lisu.ui.main.navToGallery
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.ui.widget.LisuSearchToolBar
import com.fishhawk.lisu.ui.widget.LisuToolBar
import com.fishhawk.lisu.ui.widget.SuggestionList
import com.fishhawk.lisu.util.toast
import org.koin.androidx.compose.viewModel

private typealias LibraryActionHandler = (LibraryAction) -> Unit

private sealed interface LibraryAction {
    data class NavToGallery(val manga: MangaDto) : LibraryAction
    data class Search(val keywords: String) : LibraryAction
    data class RemoveFromLibrary(val mangaList: List<MangaKeyDto>) : LibraryAction
    object Random : LibraryAction
}

@Composable
fun LibraryScreen(navController: NavHostController) {
    val viewModel by viewModel<LibraryViewModel>()
    val keywords by viewModel.keywords.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val mangaList = viewModel.mangaList.collectAsLazyPagingItems()

    val onAction: LibraryActionHandler = { action ->
        when (action) {
            is LibraryAction.NavToGallery -> navController.navToGallery(action.manga)
            is LibraryAction.Search -> viewModel.search(action.keywords)
            is LibraryAction.RemoveFromLibrary -> viewModel.deleteMultipleManga(action.mangaList)
            LibraryAction.Random -> viewModel.getRandomManga()
        }
    }

    val context = LocalContext.current
    OnEvent(viewModel.event) {
        when (it) {
            is LibraryEvent.GetRandomSuccess ->
                navController.navToGallery(it.manga)
            is LibraryEvent.GetRandomFailure ->
                context.toast(it.exception.localizedMessage ?: "")
            is LibraryEvent.DeleteMultipleFailure ->
                context.toast(it.exception.localizedMessage ?: "")
        }
    }

    var editingKeywords by remember { mutableStateOf(keywords) }
    var editing by remember { mutableStateOf(false) }
    val selectedMangaList = remember { mutableStateListOf<MangaKeyDto>() }

    Scaffold(
        topBar = {
            LisuToolBar(title = stringResource(R.string.label_library).let {
                if (keywords.isBlank()) it else "$it - $keywords"
            }) {
                val isRandomButtonEnabled by PR.isRandomButtonEnabled.collectAsState()
                if (isRandomButtonEnabled) {
                    IconButton(onClick = { onAction(LibraryAction.Random) }) {
                        Icon(LisuIcons.Casino, stringResource(R.string.action_random_pick))
                    }
                }
                IconButton(onClick = { editing = true }) {
                    Icon(LisuIcons.Search, stringResource(R.string.action_search))
                }
            }
            LisuSearchToolBar(
                visible = editing,
                value = editingKeywords,
                onValueChange = { editingKeywords = it },
                onSearch = {
                    onAction(LibraryAction.Search(it))
                    editing = false
                },
                onDismiss = { editing = false },
                placeholder = { Text(stringResource(R.string.search_library_hint)) }
            )
            SelectingToolBar(selectedMangaList, onAction)
        },
        content = { paddingValues ->
            LisuTransition {
                RefreshableMangaList(
                    modifier = Modifier.padding(paddingValues),
                    mangaList = mangaList,
                    selectedMangaList = selectedMangaList,
                    onCardClick = {
                        if (selectedMangaList.isEmpty()) {
                            onAction(LibraryAction.NavToGallery(it))
                        } else {
                            val key = it.key
                            if (selectedMangaList.contains(key)) {
                                selectedMangaList.remove(key)
                            } else {
                                selectedMangaList.add(key)
                            }
                        }
                    },
                    onCardLongClick = { manga ->
                        if (selectedMangaList.isEmpty()) {
                            selectedMangaList.add(manga.key)
                        } else {
                            val list = mangaList.itemSnapshotList.mapNotNull { it?.key }
                            val start = list.indexOf(selectedMangaList.last())
                            val end = list.indexOf(manga.key)
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
                SuggestionList(
                    visible = editing,
                    keywords = editingKeywords,
                    suggestions = suggestions,
                    additionalBottom = (-56).dp,
                    onSuggestionSelected = { editingKeywords = it }
                )
            }
        }
    )
}

@Composable
private fun SelectingToolBar(
    selectedMangaList: SnapshotStateList<MangaKeyDto>,
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
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        LisuToolBar(
            title = stringResource(R.string.library_n_selected).format(selectedMangaList.size),
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
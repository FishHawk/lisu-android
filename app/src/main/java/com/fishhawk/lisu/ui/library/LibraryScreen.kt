package com.fishhawk.lisu.ui.library

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.datastore.collectAsState
import com.fishhawk.lisu.data.network.base.PagedList
import com.fishhawk.lisu.data.network.model.MangaDto
import com.fishhawk.lisu.data.network.model.MangaKeyDto
import com.fishhawk.lisu.ui.base.OnEvent
import com.fishhawk.lisu.ui.main.navToGallery
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.util.toast
import com.fishhawk.lisu.widget.*
import org.koin.androidx.compose.koinViewModel

private sealed interface LibraryAction {
    data class NavToGallery(val manga: MangaDto) : LibraryAction
    data class Search(val keywords: String) : LibraryAction
    data class RemoveFromLibrary(val mangaList: List<MangaKeyDto>) : LibraryAction
    object Random : LibraryAction
    object Reload : LibraryAction
    object Refresh : LibraryAction
    object RequestNextPage : LibraryAction
}

@Composable
fun LibraryScreen(
    navController: NavHostController,
    viewModel: LibraryViewModel = koinViewModel(),
) {
    val keywords by viewModel.keywords.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val mangaListResult by viewModel.mangas.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val onAction: (LibraryAction) -> Unit = { action ->
        when (action) {
            is LibraryAction.NavToGallery -> navController.navToGallery(action.manga)
            is LibraryAction.Search -> viewModel.search(action.keywords)
            is LibraryAction.RemoveFromLibrary -> viewModel.deleteMultipleManga(action.mangaList)
            LibraryAction.Random -> viewModel.getRandomManga()
            LibraryAction.Reload -> viewModel.reload()
            LibraryAction.Refresh -> viewModel.refresh()
            LibraryAction.RequestNextPage -> viewModel.requestNextPage()
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
            is LibraryEvent.RefreshFailure ->
                context.toast(it.exception.localizedMessage ?: "")
        }
    }

    LibraryScaffold(
        keywords = keywords,
        suggestions = suggestions,
        isRefreshing = isRefreshing,
        mangaListResult = mangaListResult,
        onAction = onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryScaffold(
    keywords: String,
    suggestions: List<String>,
    isRefreshing: Boolean,
    mangaListResult: Result<PagedList<MangaDto>>?,
    onAction: (LibraryAction) -> Unit,
) {
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
                StateView(
                    result = mangaListResult,
                    onRetry = { onAction(LibraryAction.Reload) },
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                ) { mangaList, modifier ->
                    Box(modifier = modifier) {
                        RefreshableMangaList(
                            mangaList = mangaList,
                            isRefreshing = isRefreshing,
                            onRefresh = { onAction(LibraryAction.Refresh) },
                            onRequestNextPage = { onAction(LibraryAction.RequestNextPage) },
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
                                    val list = mangaList.list.map { it.key }
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
                            },
                            aboveCover = { manga ->
                                if (selectedMangaList.contains(manga.key)) {
                                    val color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    Canvas(modifier = Modifier.matchParentSize()) {
                                        drawRoundRect(
                                            color = color,
                                            cornerRadius = CornerRadius(4.dp.toPx()),
                                        )
                                    }
                                }
                            },
                            behindCover = { manga ->
                                if (selectedMangaList.contains(manga.key)) {
                                    val color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    val boardSize = 8f
                                    Canvas(modifier = Modifier.matchParentSize()) {
                                        drawRoundRect(
                                            color = color,
                                            topLeft = Offset(-boardSize, -boardSize),
                                            size = Size(
                                                size.width + 2 * boardSize,
                                                size.height + 2 * boardSize,
                                            ),
                                            cornerRadius = CornerRadius(4.dp.toPx()),
                                        )
                                    }
                                }
                            }
                        )
                    }
                    BackHandler(selectedMangaList.isNotEmpty()) {
                        selectedMangaList.clear()
                    }
                    SuggestionList(
                        visible = editing,
                        onDismiss = { editing = false },
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
private fun SelectingToolBar(
    selectedMangaList: SnapshotStateList<MangaKeyDto>,
    onAction: (LibraryAction) -> Unit,
) {
    var isOpen by remember { mutableStateOf(false) }
    if (isOpen) {
        LisuDialog(
            title = stringResource(R.string.library_remove_n_mangas_from_library)
                .format(selectedMangaList.size),
            confirmText = stringResource(R.string.action_ok),
            dismissText = stringResource(R.string.action_cancel),
            onConfirm = { onAction(LibraryAction.RemoveFromLibrary(selectedMangaList.toList())) },
            onDismiss = { isOpen = false },
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
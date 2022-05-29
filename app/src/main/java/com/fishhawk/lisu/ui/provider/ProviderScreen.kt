package com.fishhawk.lisu.ui.provider

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.datastore.BoardFilter
import com.fishhawk.lisu.data.datastore.getBlocking
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.data.remote.model.MangaState
import com.fishhawk.lisu.ui.base.MangaBadge
import com.fishhawk.lisu.ui.base.RefreshableMangaList
import com.fishhawk.lisu.ui.main.navToGallery
import com.fishhawk.lisu.ui.main.navToProviderSearch
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.ui.widget.LisuDialog
import com.fishhawk.lisu.ui.widget.LisuToolBar
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.viewModel
import org.koin.core.parameter.parametersOf

internal typealias ProviderActionHandler = (ProviderAction) -> Unit

internal sealed interface ProviderAction {
    object NavUp : ProviderAction
    object NavToSearch : ProviderAction
    data class NavToGallery(val manga: MangaDto) : ProviderAction

    data class SelectFilter(
        val boardId: String,
        val name: String,
        val selected: Int
    ) : ProviderAction

    data class AddToLibrary(val manga: MangaDto) : ProviderAction
    data class RemoveFromLibrary(val manga: MangaDto) : ProviderAction
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ProviderScreen(navController: NavHostController) {
    val viewModel by viewModel<ProviderViewModel> {
        parametersOf(navController.currentBackStackEntry!!.arguments!!)
    }
    val boards = viewModel.boards
    val boardHistory = viewModel.pageHistory.getBlocking()
    var addDialogManga by remember { mutableStateOf<MangaDto?>(null) }
    var removeDialogManga by remember { mutableStateOf<MangaDto?>(null) }

    val onAction: ProviderActionHandler = { action ->
        when (action) {
            ProviderAction.NavUp -> navController.navigateUp()
            ProviderAction.NavToSearch -> navController.navToProviderSearch(viewModel.provider.id)
            is ProviderAction.NavToGallery -> navController.navToGallery(action.manga)

            is ProviderAction.SelectFilter -> with(action) {
                viewModel.updateFilterHistory(boardId, name, selected)
            }
            is ProviderAction.AddToLibrary -> viewModel.addToLibrary(action.manga)
            is ProviderAction.RemoveFromLibrary -> viewModel.removeFromLibrary(action.manga)
        }
    }

    val pagerState = rememberPagerState(
        initialPage = boards.indexOf(boardHistory).coerceAtLeast(0)
    )
    LaunchedEffect(pagerState.currentPage) {
        viewModel.pageHistory.set(boards[pagerState.currentPage])
    }

    Scaffold(
        topBar = { ToolBar(viewModel.provider.id, boards, pagerState, onAction) },
        content = { paddingValues ->
            LisuTransition {
                HorizontalPager(
                    modifier = Modifier.padding(paddingValues),
                    count = boards.size,
                    state = pagerState
                ) { page ->
                    val boardId = boards[page]
                    val filterList by viewModel.boardFilters[boardId]!!.collectAsState()
                    val mangaList = viewModel.boardMangaLists[boardId]!!.collectAsLazyPagingItems()
                    ProviderBoard(
                        boardId = boardId,
                        filterList = filterList,
                        mangaList = mangaList,
                        onAction = onAction,
                        onCardLongClick = {
                            when (it.state) {
                                MangaState.Local -> Log.w(null, "Manga state should be local here")
                                MangaState.Remote -> addDialogManga = it
                                MangaState.RemoteInLibrary -> removeDialogManga = it
                            }
                        }
                    )
                }
            }
        }
    )

    addDialogManga?.let {
        LisuDialog(
            title = it.titleOrId,
            confirmText = "Add to library",
            onConfirm = { onAction(ProviderAction.AddToLibrary(it)) },
            onDismiss = { addDialogManga = null },
        )
    }

    removeDialogManga?.let {
        LisuDialog(
            title = it.titleOrId,
            confirmText = "Remove from library",
            onConfirm = { onAction(ProviderAction.RemoveFromLibrary(it)) },
            onDismiss = { removeDialogManga = null },
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun ToolBar(
    providerId: String,
    boards: List<String>,
    pagerState: PagerState,
    onAction: ProviderActionHandler
) {
    Surface(elevation = AppBarDefaults.TopAppBarElevation) {
        Column {
            LisuToolBar(
                title = providerId,
                onNavUp = { onAction(ProviderAction.NavUp) },
                transparent = true,
            ) {
                IconButton(onClick = { onAction(ProviderAction.NavToSearch) }) {
                    Icon(Icons.Filled.Search, stringResource(R.string.action_search))
                }
            }

            val scope = rememberCoroutineScope()
            if (boards.size > 1) {
                TabRow(
                    modifier = Modifier.zIndex(2f),
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                        )
                    },
                    selectedTabIndex = pagerState.currentPage,
                    backgroundColor = MaterialTheme.colors.surface
                ) {
                    boards.forEachIndexed { index, title ->
                        Tab(
                            text = { Text(title) },
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.scrollToPage(index) } },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderBoard(
    boardId: String,
    filterList: List<BoardFilter>?,
    mangaList: LazyPagingItems<MangaDto>,
    onAction: ProviderActionHandler,
    onCardLongClick: (manga: MangaDto) -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        filterList?.forEach { BoardFilter(boardId, it, onAction) }
        RefreshableMangaList(
            mangaList = mangaList,
            decorator = {
                if (it != null && it.state == MangaState.RemoteInLibrary) {
                    MangaBadge(text = "in library")
                }
            },
            onCardClick = { onAction(ProviderAction.NavToGallery(it)) },
            onCardLongClick = onCardLongClick
        )
    }
}

@Composable
private fun BoardFilter(
    boardId: String,
    filter: BoardFilter,
    onAction: ProviderActionHandler,
) {
    FlowRow(mainAxisSpacing = 4.dp, crossAxisSpacing = 2.dp) {
        filter.options.mapIndexed { index, text ->
            Text(
                modifier = Modifier.clickable {
                    onAction(ProviderAction.SelectFilter(boardId, filter.name, index))
                },
                text = text,
                style = TextStyle(fontSize = 12.sp).merge(),
                color = MaterialTheme.colors.run {
                    if (index != filter.selected) onSurface else primary
                }
            )
        }
    }
}
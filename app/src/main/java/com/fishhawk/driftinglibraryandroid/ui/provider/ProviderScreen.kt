package com.fishhawk.driftinglibraryandroid.ui.provider

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.fishhawk.driftinglibraryandroid.data.datastore.BoardFilter
import com.fishhawk.driftinglibraryandroid.data.datastore.get
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaDto
import com.fishhawk.driftinglibraryandroid.data.remote.model.Provider
import com.fishhawk.driftinglibraryandroid.ui.activity.setArgument
import com.fishhawk.driftinglibraryandroid.ui.base.RefreshableMangaList
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationToolBar
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTransition
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.pager.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ProviderScreen(navController: NavHostController) {
    navController.setArgument<Provider>("provider")

    val viewModel = hiltViewModel<ProviderViewModel>()
    val boards = viewModel.boards
    val boardHistory = remember { runBlocking { viewModel.pageHistory.get() } }

    val pagerState = rememberPagerState(
        pageCount = viewModel.boards.size,
        initialPage = boards.indexOf(boardHistory).coerceAtLeast(0)
    )
    LaunchedEffect(pagerState.currentPage) {
        viewModel.pageHistory.set(boards[pagerState.currentPage])
    }

    Scaffold(
        topBar = { ToolBar(viewModel.provider.id, boards, pagerState, navController) },
        content = {
            ApplicationTransition {
                HorizontalPager(state = pagerState) { page ->
                    val boardId = boards[page]
                    ProviderPanel(
                        navController,
                        boardId,
                        viewModel.boardMangaLists[boardId]!!.collectAsLazyPagingItems(),
                        viewModel.boardFilters[boardId]!!
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun ToolBar(
    providerId: String,
    boards: List<String>,
    pagerState: PagerState,
    navController: NavHostController
) {
    Surface(elevation = AppBarDefaults.TopAppBarElevation) {
        Column {
            ApplicationToolBar(
                title = providerId,
                navController = navController,
                elevation = 0.dp
            ) {
                IconButton(onClick = {
                    navController.currentBackStackEntry?.arguments =
                        bundleOf("providerId" to providerId)
                    navController.navigate("search/${providerId}")
                }) { Icon(Icons.Filled.Search, contentDescription = "search") }
            }

            val scope = rememberCoroutineScope()
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ProviderPanel(
    navController: NavHostController,
    boardId: String,
    mangaList: LazyPagingItems<MangaDto>,
    optionModelFlow: StateFlow<List<BoardFilter>?>
) {
    Column {
        val optionGroup by optionModelFlow.collectAsState()
        if (!optionGroup.isNullOrEmpty())
            OptionGroupList(boardId, optionGroup = optionGroup!!)

        RefreshableMangaList(
            mangaList = mangaList,
            onCardClick = {
                navController.currentBackStackEntry?.arguments =
                    bundleOf("manga" to it)
                navController.navigate("gallery/${it.id}")
            },
            onCardLongClick = {
//                ProviderActionSheet(
//                    context,
//                    it,
//                    viewModel.provider.id,
//                    object : ProviderActionSheet.Listener {
//                        override fun onReadClick(outline: MangaOutline, provider: String) {
//                            context.navToReaderActivity(outline.id, viewModel.provider.id, 0, 0, 0)
//                        }
//
//                        override fun onLibraryAddClick(outline: MangaOutline, provider: String) {
//                            viewModel.addToLibrary(outline.id, outline.title)
//                        }
//                    }
//                ).show()
            }
        )
    }
}

@Composable
private fun OptionGroupList(
    boardId: String,
    optionGroup: List<BoardFilter>
) {
    val viewModel = hiltViewModel<ProviderViewModel>()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        optionGroup.forEach { (name, options, selected) ->
            FlowRow(mainAxisSpacing = 4.dp, crossAxisSpacing = 2.dp) {
                options.mapIndexed { index, option ->
                    Text(
                        modifier = Modifier.clickable {
                            viewModel.updateFilterHistory(boardId, name, index)
                        },
                        style = TextStyle(fontSize = 12.sp).merge(),
                        text = option,
                        color = MaterialTheme.colors.run { if (index != selected) onSurface else primary }
                    )
                }
            }
        }
    }
}

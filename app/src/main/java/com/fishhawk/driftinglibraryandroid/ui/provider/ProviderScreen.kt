package com.fishhawk.driftinglibraryandroid.ui.provider

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.datastore.OptionGroup
import com.fishhawk.driftinglibraryandroid.data.datastore.get
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
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
    navController.setArgument<ProviderInfo>("provider")

    val viewModel = hiltViewModel<ProviderViewModel>()
    val initialPage = remember { runBlocking { viewModel.pageHistory.get().coerceIn(0, 2) } }
    val pagerState = rememberPagerState(pageCount = 3, initialPage = initialPage)
    LaunchedEffect(pagerState.currentPage) {
        viewModel.pageHistory.set(pagerState.currentPage)
    }

    Scaffold(
        topBar = { ToolBar(pagerState, navController) },
        content = {
            ApplicationTransition {
                Content(pagerState, navController)
            }
        }
    )
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun Content(
    pagerState: PagerState,
    navController: NavHostController
) {
    val viewModel = hiltViewModel<ProviderViewModel>()

    HorizontalPager(state = pagerState) { page ->
        when (page) {
            0 -> ProviderPanel(
                navController,
                0,
                viewModel.popularMangaList.collectAsLazyPagingItems(),
                viewModel.popularOptionModel
            )
            1 -> ProviderPanel(
                navController,
                1,
                viewModel.latestMangaList.collectAsLazyPagingItems(),
                viewModel.latestOptionModel
            )
            2 -> ProviderPanel(
                navController,
                2,
                viewModel.categoryMangaList.collectAsLazyPagingItems(),
                viewModel.categoryOptionModel
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun ToolBar(pagerState: PagerState, navController: NavHostController) {
    val viewModel = hiltViewModel<ProviderViewModel>()

    Surface(elevation = AppBarDefaults.TopAppBarElevation) {
        Column {
            ApplicationToolBar(
                title = viewModel.provider.title,
                navController = navController,
                elevation = 0.dp
            ) {
                IconButton(onClick = {
                    navController.currentBackStackEntry?.arguments =
                        bundleOf(
                            "provider" to viewModel.provider
                        )
                    navController.navigate("search/${viewModel.provider.id}")
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
                listOf(
                    R.string.label_popular,
                    R.string.label_latest,
                    R.string.label_category
                ).forEachIndexed { index, title ->
                    Tab(
                        text = { Text(stringResource(title)) },
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
    page: Int,
    mangaList: LazyPagingItems<MangaOutline>,
    optionModelFlow: StateFlow<List<OptionGroup>?>
) {
    val viewModel = hiltViewModel<ProviderViewModel>()

    Column {
        val optionGroup by optionModelFlow.collectAsState()
        if (!optionGroup.isNullOrEmpty())
            OptionGroupList(page = page, optionGroup = optionGroup!!)

        val context = LocalContext.current
        RefreshableMangaList(
            mangaList = mangaList,
            onCardClick = {
                navController.currentBackStackEntry?.arguments =
                    bundleOf(
                        "outline" to it,
                        "provider" to viewModel.provider
                    )
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
    page: Int,
    optionGroup: List<OptionGroup>
) {
    val viewModel = hiltViewModel<ProviderViewModel>()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        optionGroup.forEach { (name, options, selected) ->
            FlowRow(mainAxisSpacing = 4.dp, crossAxisSpacing = 2.dp) {
                options.mapIndexed { index, option ->
                    Text(
                        modifier = Modifier.clickable {
                            viewModel.updateOptionHistory(page, name, index)
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

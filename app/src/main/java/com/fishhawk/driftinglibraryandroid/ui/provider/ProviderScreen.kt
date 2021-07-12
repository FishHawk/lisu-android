package com.fishhawk.driftinglibraryandroid.ui.provider

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LiveData
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.preference.ProviderBrowseHistory
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.OptionModel
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.ui.base.MangaDisplayModeButton
import com.fishhawk.driftinglibraryandroid.ui.base.RefreshableMangaList
import com.fishhawk.driftinglibraryandroid.ui.base.navToReaderActivity
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTransition
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar
import com.google.accompanist.pager.*

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ProviderScreen(navController: NavHostController) {
    val providerInfo =
        navController.previousBackStackEntry!!.arguments!!.getParcelable<ProviderInfo>("provider")
    navController.currentBackStackEntry!!.arguments!!.putParcelable("provider", providerInfo)

    val viewModel = hiltViewModel<ProviderViewModel>()
    val browseHistory = ProviderBrowseHistory(LocalContext.current)

    val pagerState = rememberPagerState(
        pageCount = 3,
        initialPage = browseHistory.getPageHistory(viewModel.provider.id).coerceIn(0, 2)
    )
    browseHistory.setPageHistory(
        viewModel.provider.id,
        pagerState.currentPage
    )
    Scaffold(
        topBar = { ToolBar(pagerState) },
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
                viewModel.popularMangaList,
                viewModel.popularOptionModel
            )
            1 -> ProviderPanel(
                navController,
                1,
                viewModel.latestMangaList,
                viewModel.latestOptionModel
            )
            2 -> ProviderPanel(
                navController,
                2,
                viewModel.categoryMangaList,
                viewModel.categoryOptionModel
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun ToolBar(pagerState: PagerState) {
    val viewModel = hiltViewModel<ProviderViewModel>()

    Surface(elevation = AppBarDefaults.TopAppBarElevation) {
        Column {
            TopAppBar(
                elevation = 0.dp,
                backgroundColor = MaterialTheme.colors.surface,
                contentPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.statusBars),
                title = { Text(viewModel.provider.title) },
                actions = {
                    IconButton(onClick = {
//            queryHint = getString(R.string.menu_search_hint)
//                    findNavController().navigate(
//                        R.id.action_to_search,
//                        bundleOf(
//                            "provider" to viewModel.provider,
//                            "keywords" to query
//                        )
//                    )
                    }) {
                        Icon(Icons.Filled.Search, contentDescription = "search")
                    }
                    MangaDisplayModeButton()
                }
            )

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
                        onClick = { },
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
    mangaList: ProviderMangaList,
    optionModelLiveData: LiveData<OptionModel>
) {
    val viewModel = hiltViewModel<ProviderViewModel>()

    val optionModel by optionModelLiveData.observeAsState(mapOf())
    Column {
        OptionGroupList(
            page = page,
            mangaList = mangaList,
            optionModel = optionModel
        )

        val context = LocalContext.current
        RefreshableMangaList(
            mangaList = mangaList.list.collectAsLazyPagingItems(),
            onCardClick = {
                navController.currentBackStackEntry?.arguments =
                    bundleOf(
                        "outline" to it,
                        "provider" to viewModel.provider
                    )
                navController.navigate("gallery/${it.id}")
            },
            onCardLongClick = {
                ProviderActionSheet(
                    context,
                    it,
                    viewModel.provider.id,
                    object : ProviderActionSheet.Listener {
                        override fun onReadClick(outline: MangaOutline, provider: String) {
                            context.navToReaderActivity(outline.id, viewModel.provider.id, 0, 0, 0)
                        }

                        override fun onLibraryAddClick(outline: MangaOutline, provider: String) {
                            viewModel.addToLibrary(outline.id, outline.title)
                        }
                    }
                ).show()
            }
        )
    }
}

@Composable
private fun OptionGroupList(
    page: Int,
    mangaList: ProviderMangaList,
    optionModel: OptionModel
) {
    val viewModel = hiltViewModel<ProviderViewModel>()
    val browseHistory = ProviderBrowseHistory(LocalContext.current)

    val option: Option = mutableMapOf()
    optionModel.map { (name, options) ->
        val selectedIndex = browseHistory.getOptionHistory(
            viewModel.provider.id, page, name
        )
        option[name] = selectedIndex
        FlowRow(
            modifier = Modifier.padding(bottom = 8.dp),
            mainAxisSpacing = 4.dp,
            crossAxisSpacing = 4.dp
        ) {
            options.mapIndexed { index, option ->
                Text(
                    modifier = Modifier.clickable {
                        browseHistory.setOptionHistory(
                            viewModel.provider.id,
                            page, name, index
                        )
                        mangaList.selectOption(name, index)
                    },
                    style = TextStyle(fontSize = 12.sp).merge(),
                    text = option,
                    color = if (index != selectedIndex) MaterialTheme.colors.onSurface
                    else MaterialTheme.colors.primary
                )
            }
        }
    }
    if (optionModel.isNotEmpty()) mangaList.selectOption(option)
}

package com.fishhawk.driftinglibraryandroid.ui.provider

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.preference.ProviderBrowseHistory
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.OptionModel
import com.fishhawk.driftinglibraryandroid.ui.MainViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.base.MangaDisplayModeButton
import com.fishhawk.driftinglibraryandroid.ui.base.RefreshableMangaList
import com.fishhawk.driftinglibraryandroid.ui.base.navToReaderActivity
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTheme
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalPagerApi::class)
class ProviderPagerFragment : Fragment() {
    private val viewModel: ProviderViewModel by viewModels {
        MainViewModelFactory(this)
    }

    private val providerBrowseHistory: ProviderBrowseHistory by lazy {
        ProviderBrowseHistory(requireContext())
    }

    private val actionAdapter = object : ProviderActionSheet.Listener {
        override fun onReadClick(outline: MangaOutline, provider: String) {
            navToReaderActivity(outline.id, viewModel.provider.id, 0, 0, 0)
        }

        override fun onLibraryAddClick(outline: MangaOutline, provider: String) {
            viewModel.addToLibrary(outline.id, outline.title)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = ComposeView(requireContext())
        view.setContent {
            ApplicationTheme {
                ProvideWindowInsets {
                    val pagerState = rememberPagerState(
                        pageCount = 3,
                        initialPage = providerBrowseHistory
                            .getPageHistory(viewModel.provider.id)
                            .coerceIn(0, 2)
                    )
                    providerBrowseHistory.setPageHistory(
                        viewModel.provider.id,
                        pagerState.currentPage
                    )
                    Scaffold(
                        topBar = { ToolBar(pagerState) },
                        content = { Content(pagerState) }
                    )
                }
            }
        }
        return view
    }

    @Composable
    private fun ToolBar(pagerState: PagerState) {
        Column {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.secondary,
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
                selectedTabIndex = pagerState.currentPage,
                backgroundColor = MaterialTheme.colors.secondary
            ) {
                listOf("Popular", "Latest", "Category").forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = pagerState.currentPage == index,
                        onClick = { },
                    )
                }
            }
        }
    }

    @Composable
    private fun Content(pagerState: PagerState) {
        HorizontalPager(state = pagerState) { page ->
            when (page) {
                0 -> ProviderPanel(0, viewModel.popularMangaList, viewModel.popularOptionModel)
                1 -> ProviderPanel(1, viewModel.latestMangaList, viewModel.latestOptionModel)
                2 -> ProviderPanel(2, viewModel.categoryMangaList, viewModel.categoryOptionModel)
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun ProviderPanel(
        page: Int,
        mangaList: ProviderMangaList,
        optionModelLiveData: LiveData<OptionModel>
    ) {
        val optionModel by optionModelLiveData.observeAsState(mapOf())
        Column {
            OptionGroupList(
                page = page,
                mangaList = mangaList,
                optionModel = optionModel
            )

            RefreshableMangaList(
                mangaList = mangaList.list.collectAsLazyPagingItems(),
                onCardClick = {
                    findNavController().navigate(
                        R.id.action_to_gallery_detail,
                        bundleOf(
                            "outline" to it,
                            "provider" to viewModel.provider
                        )
                    )
                },
                onCardLongClick = {
                    ProviderActionSheet(
                        requireContext(),
                        it,
                        viewModel.provider.id,
                        actionAdapter
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
        val option: Option = mutableMapOf()
        optionModel.map { (name, options) ->
            val selectedIndex = providerBrowseHistory.getOptionHistory(
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
                            providerBrowseHistory.setOptionHistory(
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
}
package com.fishhawk.driftinglibraryandroid.ui.provider

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.fishhawk.driftinglibraryandroid.ui.base.RefreshableMangaList
import com.fishhawk.driftinglibraryandroid.ui.base.navToReaderActivity
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTheme
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.insets.ProvideWindowInsets

abstract class ProviderBaseFragment : Fragment() {
    protected val viewModel: ProviderViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    ) { MainViewModelFactory(this) }

    private val providerBrowseHistory: ProviderBrowseHistory by lazy {
        ProviderBrowseHistory(requireContext())
    }

    abstract val mangaList: ProviderMangaList
    abstract val optionModel: LiveData<OptionModel>
    abstract val page: Int

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
    ): View? {
        val view = ComposeView(requireContext())
        view.setContent {
            ApplicationTheme {
                ProvideWindowInsets {
                    Scaffold(
                        content = { Content() }
                    )
                }
            }
        }
        return view
    }

    @Composable
    private fun Content() {
        Column {
            OptionGroupList()
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
    private fun OptionGroupList() {
        val model by optionModel.observeAsState(mapOf())
        val option: Option = mutableMapOf()
        model.map { (name, options) ->
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
        if (model.isNotEmpty()) mangaList.selectOption(option)
    }
}

class PopularFragment : ProviderBaseFragment() {
    override val mangaList by lazy { viewModel.popularMangaList }
    override val optionModel by lazy { viewModel.popularOptionModel }
    override val page: Int = 0
}

class LatestFragment : ProviderBaseFragment() {
    override val mangaList by lazy { viewModel.latestMangaList }
    override val optionModel by lazy { viewModel.latestOptionModel }
    override val page: Int = 1
}

class CategoryFragment : ProviderBaseFragment() {
    override val mangaList by lazy { viewModel.categoryMangaList }
    override val optionModel by lazy { viewModel.categoryOptionModel }
    override val page: Int = 2
}

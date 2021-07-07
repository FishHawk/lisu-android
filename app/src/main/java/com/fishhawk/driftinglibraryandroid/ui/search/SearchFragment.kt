package com.fishhawk.driftinglibraryandroid.ui.search

import android.os.Bundle
import android.view.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.ui.MainViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.base.*
import com.fishhawk.driftinglibraryandroid.ui.provider.ProviderActionSheet
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTheme
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar

class SearchFragment : Fragment() {
    private val viewModel: SearchViewModel by viewModels {
        MainViewModelFactory(this)
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
                    Scaffold(
                        topBar = { ToolBar() },
                        content = { Content() }
                    )
                }
            }
        }
        return view
    }

    @Composable
    private fun ToolBar() {
        TopAppBar(
            contentPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.statusBars),
            title = { Text(stringResource(R.string.label_search)) },
            actions = {
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.Search, contentDescription = "search")
                }
                MangaDisplayModeButton()
            }
        )
    }

    @Composable
    private fun Content() {
        RefreshableMangaList(
            mangaList = viewModel.mangaList.collectAsLazyPagingItems(),
            onCardClick = {
                findNavController().navigate(
                    R.id.action_to_gallery_detail,
                    bundleOf(
                        "outline" to it,
                        "provider" to viewModel.provider.id
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
//            queryHint = getString(R.string.menu_search_hint)
//            setQuery(viewModel.keywords.value, false)
//            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//                override fun onQueryTextSubmit(query: String): Boolean {
//                    viewModel.keywords.value = query
//                    closeInputMethod()
//                    return true
//                }
//                override fun onQueryTextChange(query: String?): Boolean = true
//            })
}

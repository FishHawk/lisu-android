package com.fishhawk.driftinglibraryandroid.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.ui.MainViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.base.MangaDisplayModeButton
import com.fishhawk.driftinglibraryandroid.ui.base.MangaList
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTheme
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

class LibraryFragment : Fragment() {
    private val viewModel: LibraryViewModel by viewModels {
        MainViewModelFactory(this)
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
            title = { Text("Library") },
            actions = {
                MangaDisplayModeButton()
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.Search, contentDescription = "search")
                }
            }
        )
    }

    @Composable
    private fun Content() {
        val mangas = viewModel.mangas.collectAsLazyPagingItems()
        val isRefreshing = mangas.loadState.refresh is LoadState.Loading
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { mangas.refresh() },
        ) {
            MangaList(
                mangas,
                onCardClick = {
                    findNavController().navigate(
                        R.id.action_to_gallery,
                        bundleOf("outline" to it)
                    )
                },
                onCardLongClick = {
                    AlertDialog.Builder(requireActivity())
                        .setTitle("Confirm to delete manga?")
                        .setPositiveButton("OK") { _, _ -> viewModel.deleteManga(it.id) }
                        .setNegativeButton("cancel") { _, _ -> }
                        .show()
                }
            )
        }
    }
//            queryHint = getString(R.string.menu_search_hint)
//            maxWidth = Int.MAX_VALUE
//            setQuery(viewModel.keywords.value, false)
//            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//                override fun onQueryTextSubmit(query: String): Boolean {
//                    viewModel.keywords.value = query
//                    closeInputMethod()
//                    return true
//                }
//
//                override fun onQueryTextChange(query: String?): Boolean = false
//            })
//            val closeButton: ImageView = findViewById(R.id.search_close_btn)
//            closeButton.setOnClickListener {
//                setQuery(null, false)
//                isIconified = true
//                viewModel.keywords.value = ""
//            }
}
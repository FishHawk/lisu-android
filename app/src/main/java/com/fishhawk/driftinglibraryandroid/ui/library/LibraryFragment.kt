package com.fishhawk.driftinglibraryandroid.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.ui.MainViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.base.MangaDisplayModeButton
import com.fishhawk.driftinglibraryandroid.ui.base.RefreshableMangaList
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTheme
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LibraryFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = ComposeView(requireContext())
        view.setContent {
            ApplicationTheme {
                ProvideWindowInsets {
                    LibraryScreen()
                }
            }
        }
        return view
    }

}

@Composable
fun LibraryScreen() {
    // TODO : search
    //  queryHint = getString(R.string.menu_search_hint)
    //  setQuery(viewModel.keywords.value, false)

    val viewModel: LibraryViewModel = viewModel()
    Scaffold(
        topBar = { ToolBar() },
        content = { Content(viewModel) }
    )
}

@Composable
private fun ToolBar() {
    TopAppBar(
        backgroundColor = MaterialTheme.colors.secondary,
        contentPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.statusBars),
        title = { Text(stringResource(R.string.label_library)) },
        actions = {
            IconButton(onClick = { }) {
                Icon(Icons.Filled.Search, contentDescription = "search")
            }
            MangaDisplayModeButton()
        }
    )
}

@Composable
private fun Content(viewModel: LibraryViewModel) {
    RefreshableMangaList(
        mangaList = viewModel.mangas.collectAsLazyPagingItems(),
        onCardClick = {
//            findNavController().navigate(
//                R.id.action_to_gallery,
//                bundleOf("outline" to it)
//            )
        },
        onCardLongClick = {
//            AlertDialog.Builder(requireActivity())
//                .setTitle("Confirm to delete manga?")
//                .setPositiveButton("OK") { _, _ -> viewModel.deleteManga(it.id) }
//                .setNegativeButton("cancel") { _, _ -> }
//                .show()
        }
    )
}

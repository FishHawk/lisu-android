package com.fishhawk.driftinglibraryandroid.ui.globalsearch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.Result
import com.fishhawk.driftinglibraryandroid.ui.MainViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.base.ErrorItem
import com.fishhawk.driftinglibraryandroid.ui.base.LoadingItem
import com.fishhawk.driftinglibraryandroid.ui.base.MangaCardGrid
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTheme
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar

class GlobalSearchFragment : Fragment() {
    private val viewModel: GlobalSearchViewModel by viewModels {
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
            backgroundColor = MaterialTheme.colors.secondary,
            contentPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.statusBars),
            title = { Text(stringResource(R.string.label_global_search)) },
            actions = {
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.Search, contentDescription = "search")
                }
            },
            navigationIcon = {
                IconButton(onClick = { findNavController().navigateUp() }) {
                    Icon(Icons.Filled.NavigateBefore, "back")
                }
            }
        )
    }

    @Composable
    private fun Content() {
        val searchGroupList by viewModel.searchGroupList.observeAsState(listOf())
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(searchGroupList) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(it.provider.title)
                        Spacer(modifier = Modifier.weight(1f, fill = true))
                        IconButton(onClick = {
                            findNavController().navigate(
                                R.id.action_to_provider_search,
                                bundleOf(
                                    "keywords" to viewModel.keywords.value,
                                    "provider" to it.provider
                                )
                            )
                        }) {
                            Icon(Icons.Filled.NavigateNext, "Forward")
                        }
                    }
                    val provider = it.provider
                    when (val result = it.result) {
                        is Result.Success -> LazyRow(
                            modifier = Modifier.height(140.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(result.data) {
                                Box(
                                    modifier = Modifier.weight(1f, fill = true),
                                    propagateMinConstraints = true
                                ) {
                                    MangaCardGrid(it,
                                        onCardClick = {
                                            findNavController().navigate(
                                                R.id.action_to_gallery_detail,
                                                bundleOf(
                                                    "outline" to it,
                                                    "provider" to provider
                                                )
                                            )
                                        })
                                }
                            }

                        }
                        is Result.Error -> ErrorItem(
                            message = result.exception.message ?: "",
                            onClickRetry = {}
                        )
                        null -> LoadingItem()
                    }
                }
            }
        }
    }
//            queryHint = getString(R.string.menu_search_global_hint)
//            setQuery(viewModel.keywords.value, false)
//            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//                override fun onQueryTextSubmit(query: String): Boolean {
//                    viewModel.keywords.value = query
//                    closeInputMethod()
//                    return true
//                }
}
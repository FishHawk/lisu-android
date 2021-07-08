package com.fishhawk.driftinglibraryandroid.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.compose.collectAsLazyPagingItems
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.ui.MainViewModelFactory
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTheme
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar

class ExploreFragment : Fragment() {
    private val viewModel: ExploreViewModel by viewModels {
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
            title = { Text(stringResource(R.string.label_explore)) },
            actions = {
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.Search, contentDescription = "search")
                }
            }
        )
    }

    @Composable
    private fun Content() {
        val providers by viewModel.providerList.observeAsState(listOf())
        val lastUsedProvider by GlobalPreference.lastUsedProvider.asFlow().collectAsState(null)

        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            providers.find { it.id == lastUsedProvider }?.let {
                Text(text = "Last used", style = MaterialTheme.typography.subtitle1)
                ProviderCard(it)
            }
            val providerMap = providers.groupBy { it.lang }
            providerMap.map { (lang, list) ->
                Text(text = lang, style = MaterialTheme.typography.subtitle1)
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    list.map { ProviderCard(it) }
                }
            }
        }
    }

    @Composable
    private fun ProviderCard(provider: ProviderInfo) {
        Card {
            Row(
                modifier = Modifier.clickable { openProvider(provider) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val request = ImageRequest.Builder(LocalContext.current)
                    .data(provider.icon)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
                Image(
                    modifier = Modifier
                        .size(48.dp, 48.dp)
                        .padding(8.dp),
                    painter = rememberCoilPainter(request, fadeIn = true),
                    contentDescription = "icon",
                    contentScale = ContentScale.Crop
                )

                Text(
                    modifier = Modifier.weight(1f, fill = true),
                    text = provider.name,
                    style = MaterialTheme.typography.subtitle1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                TextButton(
                    onClick = { openProvider(provider) }) {
                    Text(text = stringResource(R.string.explore_card_browse))
                }
            }
        }
    }

    private fun openProvider(provider: ProviderInfo) {
        findNavController().navigate(
            R.id.action_explore_to_provider_pager,
            bundleOf("provider" to provider)
        )
        GlobalPreference.lastUsedProvider.set(provider.id)
    }

//        with(menu.findItem(R.id.action_search).actionView as SearchView) {
//            queryHint = getString(R.string.menu_search_global_hint)
//            maxWidth = Int.MAX_VALUE
//            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//                override fun onQueryTextSubmit(query: String): Boolean {
//                    isIconified = true
//                    isIconified = true
//                    binding.root.findNavController().navigate(
//                        R.id.action_explore_to_global_search,
//                        bundleOf("keywords" to query)
//                    )
//                    return true
//                }
//
//                override fun onQueryTextChange(query: String): Boolean = true
//            })
//            setOnQueryTextFocusChangeListener { _, b ->
//                if (!b && query.isNullOrBlank()) {
//                    isIconified = true
//                    isIconified = true
//                }
//            }
//        }
}
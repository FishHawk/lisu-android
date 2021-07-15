package com.fishhawk.driftinglibraryandroid.ui.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.preference.P
import com.fishhawk.driftinglibraryandroid.data.preference.collectAsState
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.ui.base.EmptyView
import com.fishhawk.driftinglibraryandroid.ui.base.ErrorView
import com.fishhawk.driftinglibraryandroid.ui.base.LoadingView
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationToolBar
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTransition
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun ExploreScreen(navHostController: NavHostController) {
    Scaffold(
        topBar = { ToolBar() },
        content = { ApplicationTransition { Content(navHostController) } }
    )
}

@Composable
private fun ToolBar() {
    ApplicationToolBar(stringResource(R.string.label_explore)) {
        //  queryHint = getString(R.string.menu_search_global_hint)
        //  binding.root.findNavController().navigate(
        //      R.id.action_explore_to_global_search,
        //      bundleOf("keywords" to query)
        //  )
        IconButton(onClick = { }) {
            Icon(Icons.Filled.Search, contentDescription = "search")
        }
    }
}

@Composable
private fun Content(navHostController: NavHostController) {
    val viewModel = hiltViewModel<ExploreViewModel>()
    val providerList by viewModel.providerList.flow.collectAsState()

    providerList?.getOrNull()?.let {
        val isRefreshing by viewModel.providerList.isRefreshing.collectAsState()
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { viewModel.providerList.refresh() },
        ) {
            if (it.isEmpty()) EmptyView()
            else ProviderList(it, navHostController)
        }
    }
    providerList?.exceptionOrNull()?.let {
        ErrorView(message = it.message ?: "") { viewModel.providerList.reload() }
    }
    providerList ?: LoadingView()
}

@Composable
private fun ProviderList(
    providers: List<ProviderInfo>,
    navHostController: NavHostController
) {
    val lastUsedProvider by P.lastUsedProvider.collectAsState()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        providers.find { it.id == lastUsedProvider }?.let {
            item {
                Text(
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                    text = "Last used",
                    style = MaterialTheme.typography.subtitle1
                )
            }
            item { ProviderCard(navHostController, it) }
        }
        val providerMap = providers.groupBy { it.lang }
        providerMap.map { (lang, list) ->
            item {
                Text(
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                    text = lang,
                    style = MaterialTheme.typography.subtitle1
                )
            }
            items(list) { ProviderCard(navHostController, it) }
        }
    }
}

@Composable
private fun ProviderCard(navController: NavHostController, provider: ProviderInfo) {
    Card(
        modifier = Modifier.clickable {
            P.lastUsedProvider.set(provider.id)
            navController.currentBackStackEntry?.arguments =
                bundleOf("provider" to provider)
            navController.navigate("provider/${provider.id}")
        },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                modifier = Modifier
                    .size(48.dp, 48.dp)
                    .padding(8.dp),
                painter = rememberImagePainter(provider.icon) { crossfade(true) },
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

            Text(
                modifier = Modifier.weight(1f, fill = true),
                text = provider.name,
                style = MaterialTheme.typography.subtitle1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

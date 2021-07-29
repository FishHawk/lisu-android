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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.PR
import com.fishhawk.driftinglibraryandroid.data.datastore.collectAsState
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderInfo
import com.fishhawk.driftinglibraryandroid.ui.base.EmptyView
import com.fishhawk.driftinglibraryandroid.ui.base.ErrorView
import com.fishhawk.driftinglibraryandroid.ui.base.LoadingView
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationToolBar
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTransition
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import java.util.*

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
        ErrorView(it) { viewModel.providerList.reload() }
    }
    providerList ?: LoadingView()
}

@Composable
private fun ProviderList(
    providers: List<ProviderInfo>,
    navHostController: NavHostController
) {
    val lastUsedProvider by PR.lastUsedProvider.collectAsState()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        providers.find { it.id == lastUsedProvider }?.let {
            item { ProviderListHeader(stringResource(R.string.explore_last_used)) }
            item { ProviderCard(navHostController, it) }
        }
        val providerMap = providers.groupBy { it.lang }
        providerMap.map { (lang, list) ->
            item { ProviderListHeader(Locale(lang).displayLanguage) }
            items(list) { ProviderCard(navHostController, it) }
        }
    }
}

@Composable
private fun ProviderListHeader(label: String) {
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
        Text(
            text = label,
            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp),
            style = MaterialTheme.typography.subtitle2
        )
    }
}

@Composable
private fun ProviderCard(navController: NavHostController, provider: ProviderInfo) {
    val viewModel = hiltViewModel<ExploreViewModel>()
    Card(modifier = Modifier.clickable {
        viewModel.viewModelScope.launch { navController.navToProvider(provider) }
    }) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                modifier = Modifier.size(32.dp),
                painter = rememberImagePainter(provider.icon) { crossfade(true) },
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
            Text(
                modifier = Modifier.weight(1f),
                text = provider.name,
                style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private suspend fun NavHostController.navToProvider(provider: ProviderInfo) {
    PR.lastUsedProvider.set(provider.id)
    currentBackStackEntry?.arguments =
        bundleOf("provider" to provider)
    navigate("provider/${provider.id}")
}

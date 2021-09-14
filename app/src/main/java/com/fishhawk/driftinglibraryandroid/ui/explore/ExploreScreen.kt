package com.fishhawk.driftinglibraryandroid.ui.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.fishhawk.driftinglibraryandroid.PR
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.datastore.collectAsState
import com.fishhawk.driftinglibraryandroid.data.remote.model.Provider
import com.fishhawk.driftinglibraryandroid.ui.base.EmptyView
import com.fishhawk.driftinglibraryandroid.ui.base.StateView
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationToolBar
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTransition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun ExploreScreen(navController: NavHostController) {
    Scaffold(
        topBar = { ToolBar(navController) },
        content = { ApplicationTransition { Content(navController) } }
    )
}

@Composable
private fun ToolBar(navController: NavHostController) {
    ApplicationToolBar(stringResource(R.string.label_explore)) {
        IconButton(onClick = { navController.navToGlobalSearch() }) {
            Icon(Icons.Filled.Search, contentDescription = "search")
        }
    }
}

@Composable
private fun Content(navController: NavHostController) {
    val viewModel = hiltViewModel<ExploreViewModel>()
    val viewState by viewModel.viewState.collectAsState()
    StateView(
        modifier = Modifier.fillMaxSize(),
        viewState = viewState,
        onRetry = { viewModel.reload() }
    ) {
        val providerList by viewModel.providerList.collectAsState()
        if (providerList.isEmpty()) EmptyView()
        else ProviderList(providerList, navController)
    }
}

@Composable
private fun ProviderList(
    providers: List<Provider>,
    navHostController: NavHostController
) {
    val lastUsedProvider by PR.lastUsedProvider.collectAsState()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        providers.find { it.name == lastUsedProvider }?.let {
            item { ProviderListHeader(stringResource(R.string.explore_last_used)) }
            item { ProviderItem(navHostController, it) }
        }
        val providerMap = providers.groupBy { it.lang }
        providerMap.map { (lang, list) ->
            item { ProviderListHeader(Locale(lang).displayLanguage) }
            items(list) { ProviderItem(navHostController, it) }
        }
    }
}

@Composable
private fun ProviderListHeader(label: String) {
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
        Text(
            text = label,
            modifier = Modifier.padding(vertical = 8.dp),
            style = MaterialTheme.typography.subtitle2
        )
    }
}

@Composable
private fun ProviderItem(navController: NavHostController, provider: Provider) {
    val scope = rememberCoroutineScope()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { navController.navToProvider(scope, provider) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            modifier = Modifier
                .height(32.dp)
                .aspectRatio(1f),
            painter = rememberImagePainter(provider.icon) { crossfade(true) },
            contentDescription = null
        )
        Text(
            text = provider.name,
            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun NavHostController.navToGlobalSearch() {
    navigate("global-search")
}

private fun NavHostController.navToProvider(scope: CoroutineScope, provider: Provider) {
    scope.launch { PR.lastUsedProvider.set(provider.name) }
    currentBackStackEntry?.arguments =
        bundleOf("provider" to provider)
    navigate("provider/${provider.name}")
}

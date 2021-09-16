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
import com.fishhawk.driftinglibraryandroid.data.remote.model.Provider
import com.fishhawk.driftinglibraryandroid.ui.base.EmptyView
import com.fishhawk.driftinglibraryandroid.ui.base.StateView
import com.fishhawk.driftinglibraryandroid.ui.base.ViewState
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationToolBar
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTransition
import kotlinx.coroutines.launch
import java.util.*

private typealias ExploreActionHandler = (ExploreAction) -> Unit

private sealed interface ExploreAction {
    object NavToGlobalSearch : ExploreAction
    data class NavToProvider(val provider: Provider) : ExploreAction

    object Reload : ExploreAction
}

@Composable
fun ExploreScreen(navController: NavHostController) {
    val viewModel = hiltViewModel<ExploreViewModel>()
    val viewState by viewModel.viewState.collectAsState()
    val providers by viewModel.providers.collectAsState()
    val lastUsedProvider by viewModel.lastUsedProvider.collectAsState()

    val scope = rememberCoroutineScope()
    val onAction: ExploreActionHandler = { action ->
        when (action) {
            ExploreAction.NavToGlobalSearch -> navController.navigate("global-search")
            is ExploreAction.NavToProvider -> {
                val provider = action.provider
                scope.launch { PR.lastUsedProvider.set(provider.name) }
                navController.currentBackStackEntry?.arguments =
                    bundleOf("provider" to provider)
                navController.navigate("provider/${provider.name}")
            }
            ExploreAction.Reload -> viewModel.reload()
        }
    }

    Scaffold(
        topBar = { ToolBar(onAction) },
        content = {
            ApplicationTransition {
                ProviderList(viewState, providers, lastUsedProvider, onAction)
            }
        }
    )
}

@Composable
private fun ToolBar(onAction: ExploreActionHandler) {
    ApplicationToolBar(stringResource(R.string.label_explore)) {
        IconButton(onClick = { onAction(ExploreAction.NavToGlobalSearch) }) {
            Icon(Icons.Filled.Search, contentDescription = "search")
        }
    }
}

@Composable
private fun ProviderList(
    viewState: ViewState,
    providers: Map<String, List<Provider>>,
    lastUsedProvider: Provider?,
    onAction: ExploreActionHandler
) {
    StateView(
        modifier = Modifier.fillMaxSize(),
        viewState = viewState,
        onRetry = { onAction(ExploreAction.Reload) }
    ) {
        if (providers.isEmpty()) EmptyView()
        else {
            LazyColumn {
                lastUsedProvider?.let {
                    item { ProviderListHeader(stringResource(R.string.explore_last_used)) }
                    item { ProviderListItem(it, onAction) }
                }
                providers.forEach { (lang, list) ->
                    item { ProviderListHeader(Locale(lang).displayLanguage) }
                    items(list) { ProviderListItem(it, onAction) }
                }
            }
        }
    }
}

@Composable
private fun ProviderListHeader(label: String) {
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            style = MaterialTheme.typography.subtitle2
        )
    }
}

@Composable
private fun ProviderListItem(
    provider: Provider,
    onAction: ExploreActionHandler
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAction(ExploreAction.NavToProvider(provider)) }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            modifier = Modifier.size(32.dp),
            painter = rememberImagePainter(provider.icon) { crossfade(true) },
            contentDescription = null
        )
        Text(
            text = provider.name,
            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
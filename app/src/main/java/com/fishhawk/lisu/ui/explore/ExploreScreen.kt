package com.fishhawk.lisu.ui.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.OriginalSize
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.datastore.setBlocking
import com.fishhawk.lisu.data.remote.model.ProviderDto
import com.fishhawk.lisu.ui.navToGlobalSearch
import com.fishhawk.lisu.ui.navToProvider
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.widget.LisuToolBar
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.ui.widget.EmptyView
import com.fishhawk.lisu.ui.widget.StateView
import com.fishhawk.lisu.ui.widget.ViewState
import org.koin.androidx.compose.viewModel
import java.util.*

private typealias ExploreActionHandler = (ExploreAction) -> Unit

private sealed interface ExploreAction {
    object NavToGlobalSearch : ExploreAction
    data class NavToProvider(val provider: ProviderDto) : ExploreAction

    object Reload : ExploreAction
}

@Composable
fun ExploreScreen(navController: NavHostController) {
    val viewModel by viewModel<ExploreViewModel>()
    val viewState by viewModel.viewState.collectAsState()
    val providers by viewModel.providers.collectAsState()
    val lastUsedProvider by viewModel.lastUsedProvider.collectAsState()

    val onAction: ExploreActionHandler = { action ->
        when (action) {
            ExploreAction.NavToGlobalSearch -> navController.navToGlobalSearch()
            is ExploreAction.NavToProvider -> navController.navToProvider(action.provider.id)
            ExploreAction.Reload -> viewModel.reload()
        }
    }

    Scaffold(
        topBar = { ToolBar(onAction) },
        content = {
            LisuTransition {
                ProviderList(viewState, providers, lastUsedProvider, onAction)
            }
        }
    )
}

@Composable
private fun ToolBar(onAction: ExploreActionHandler) {
    LisuToolBar(title = stringResource(R.string.label_explore)) {
        IconButton(onClick = { onAction(ExploreAction.NavToGlobalSearch) }) {
            Icon(LisuIcons.TravelExplore, contentDescription = "global search")
        }
    }
}

@Composable
private fun ProviderList(
    viewState: ViewState,
    providers: Map<String, List<ProviderDto>>,
    lastUsedProvider: ProviderDto?,
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
    provider: ProviderDto,
    onAction: ExploreActionHandler
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onAction(ExploreAction.NavToProvider(provider))
                PR.lastUsedProvider.setBlocking(provider.id)
            }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val painter =
            if (provider.lang == "local") rememberVectorPainter(LisuIcons.LocalLibrary)
            else rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(provider.icon)
                    .size(OriginalSize)
                    .crossfade(true)
                    .build()
            )

        Image(
            modifier = Modifier.size(32.dp),
            painter = painter,
            contentDescription = null
        )
        Text(
            text = provider.id,
            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
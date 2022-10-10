package com.fishhawk.lisu.ui.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.datastore.setBlocking
import com.fishhawk.lisu.data.network.model.BoardId
import com.fishhawk.lisu.data.network.model.ProviderDto
import com.fishhawk.lisu.ui.main.*
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.widget.*
import org.koin.androidx.compose.viewModel
import java.util.*

private sealed interface ExploreAction {
    object NavToGlobalSearch : ExploreAction
    data class NavToProvider(val providerId: String, val boardId: BoardId) : ExploreAction
    data class NavToLoginWebsite(val providerId: String) : ExploreAction
    data class NavToLoginCookies(val providerId: String) : ExploreAction
    data class NavToLoginPassword(val providerId: String) : ExploreAction

    object Reload : ExploreAction

    data class Logout(val provider: ProviderDto) : ExploreAction
    data class Disable(val provider: ProviderDto) : ExploreAction
}

@Composable
fun ExploreScreen(navController: NavHostController) {
    val viewModel by viewModel<ExploreViewModel>()
    val providersResult by viewModel.providersLoadState.collectAsState()
    val lastUsedProvider by viewModel.lastUsedProvider.collectAsState()

    val onAction: (ExploreAction) -> Unit = { action ->
        when (action) {
            ExploreAction.NavToGlobalSearch ->
                navController.navToGlobalSearch()
            is ExploreAction.NavToProvider ->
                navController.navToProvider(action.providerId, action.boardId)
            is ExploreAction.NavToLoginWebsite ->
                navController.navToLoginWebsite(action.providerId)
            is ExploreAction.NavToLoginCookies ->
                navController.navToLoginCookies(action.providerId)
            is ExploreAction.NavToLoginPassword ->
                navController.navToLoginPassword(action.providerId)

            ExploreAction.Reload -> viewModel.reload()
            is ExploreAction.Logout -> viewModel.logout(action.provider.id)
            is ExploreAction.Disable -> Unit
        }
    }

    ExploreScaffold(
        providersResult = providersResult,
        lastUsedProvider = lastUsedProvider,
        onAction = onAction,
    )
}

@Composable
private fun ExploreScaffold(
    providersResult: Result<Map<String, List<ProviderDto>>>?,
    lastUsedProvider: ProviderDto?,
    onAction: (ExploreAction) -> Unit,
) {
    Scaffold(
        topBar = {
            LisuToolBar(title = stringResource(R.string.label_explore)) {
                IconButton(onClick = { onAction(ExploreAction.NavToGlobalSearch) }) {
                    Icon(LisuIcons.TravelExplore, stringResource(R.string.action_global_search))
                }
            }
        },
        content = { paddingValues ->
            LisuTransition {
                StateView(
                    result = providersResult,
                    onRetry = { onAction(ExploreAction.Reload) },
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                ) { providers, modifier ->
                    if (providers.isEmpty()) {
                        EmptyView(modifier = modifier)
                    } else {
                        ProviderList(
                            providers = providers,
                            lastUsedProvider = lastUsedProvider,
                            onAction = onAction,
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun ProviderList(
    providers: Map<String, List<ProviderDto>>,
    lastUsedProvider: ProviderDto?,
    onAction: (ExploreAction) -> Unit,
) {
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ProviderListItem(
    provider: ProviderDto,
    onAction: (ExploreAction) -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable {
            if (provider.boardModels.containsKey(BoardId.Main)) {
                onAction(ExploreAction.NavToProvider(provider.id, BoardId.Main))
            }
        },
        icon = {
            val painter =
                if (provider.lang == "local") {
                    rememberVectorPainter(LisuIcons.LocalLibrary)
                } else {
                    rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(provider.icon)
                            .size(Size.ORIGINAL)
                            .crossfade(true)
                            .build()
                    )
                }
            Surface(
                shape = RoundedCornerShape(4.dp),
                elevation = 2.dp,
            ) {
                Image(
                    painter = painter,
                    contentDescription = provider.id,
                    modifier = Modifier.size(32.dp),
                )
            }
        },
        text = {
            Text(
                text = provider.id,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        secondaryText = {
            Text(
                text = buildAnnotatedString {
                    append(Locale(provider.lang).displayLanguage)
                    provider.isLogged?.let { isLogged ->
                        append(" ")
                        if (isLogged) {
                            appendInlineContent("logged", "logged")
                        } else {
                            appendInlineContent("not logged", "not logged")
                        }
                    }
                },
                inlineContent = mapOf(
                    "logged" to InlineTextContent(
                        Placeholder(
                            width = 2.em,
                            height = MaterialTheme.typography.body2.fontSize,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                        )
                    ) {
                        Icon(imageVector = LisuIcons.HowToReg, contentDescription = null)
                    },
                    "not logged" to InlineTextContent(
                        Placeholder(
                            width = 2.em,
                            height = MaterialTheme.typography.body2.fontSize,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                        )
                    ) {
                        Icon(imageVector = LisuIcons.PersonOff, contentDescription = null)
                    },
                ),
            )
        },
        trailing = {
            OverflowMenuButton {
                provider.boardModels.keys.forEach { boardId ->
                    val text = when (boardId) {
                        BoardId.Search -> "Search"
                        BoardId.Rank -> "Rank"
                        else -> return@forEach
                    }
                    DropdownMenuItem(
                        onClick = { onAction(ExploreAction.NavToProvider(provider.id, boardId)) },
                    ) {
                        Text(text)
                    }
                }

                provider.cookiesLogin?.let {
                    DropdownMenuItem(onClick = {
                        onAction(ExploreAction.NavToLoginWebsite(provider.id))
                    }) {
                        Text("Login by website")
                    }

                    DropdownMenuItem(onClick = {
                        onAction(ExploreAction.NavToLoginCookies(provider.id))
                    }) {
                        Text("Login by cookies")
                    }
                }
                if (provider.passwordLogin) {
                    DropdownMenuItem(onClick = {
                        onAction(ExploreAction.NavToLoginPassword(provider.id))
                    }) {
                        Text("Login by passwords")
                    }
                }
                if (provider.cookiesLogin != null || provider.passwordLogin) {
                    DropdownMenuItem(onClick = {
                        onAction(ExploreAction.Logout(provider))
                    }) {
                        Text("Logout")
                    }
                }
            }
        },
    )
}
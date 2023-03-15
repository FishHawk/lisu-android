package com.fishhawk.lisu.ui.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.*
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.LoremIpsum
import com.fishhawk.lisu.data.network.model.BoardId
import com.fishhawk.lisu.data.network.model.ProviderDto
import com.fishhawk.lisu.ui.main.*
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.theme.LisuTheme
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.ui.theme.MediumEmphasis
import com.fishhawk.lisu.widget.*
import org.koin.androidx.compose.koinViewModel
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
fun ExploreScreen(
    navController: NavHostController,
    viewModel: ExploreViewModel = koinViewModel(),
) {
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
    LisuScaffold(
        topBar = {
            LisuToolBar(title = stringResource(R.string.label_explore)) {
                TooltipIconButton(
                    tooltip = stringResource(R.string.action_global_search),
                    icon = LisuIcons.TravelExplore,
                    onClick = { onAction(ExploreAction.NavToGlobalSearch) },
                )
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
                            modifier = modifier,
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
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        lastUsedProvider?.let {
            item { LisuListHeader(stringResource(R.string.explore_last_used)) }
            item { ProviderListItem(it, onAction) }
        }
        providers.forEach { (lang, list) ->
            item { LisuListHeader(Locale(lang).displayLanguage) }
            items(list) { ProviderListItem(it, onAction) }
        }
    }
}

@Composable
private fun ProviderListItem(
    provider: ProviderDto,
    onAction: (ExploreAction) -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(
                text = provider.id,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        modifier = Modifier.clickable {
            if (provider.boardModels.containsKey(BoardId.Main)) {
                onAction(ExploreAction.NavToProvider(provider.id, BoardId.Main))
            }
        },
        leadingContent = {
            val painter = if (provider.lang == "local") {
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
            Surface(shape = RoundedCornerShape(4.dp)) {
                Image(
                    painter = painter,
                    contentDescription = provider.id,
                    modifier = Modifier.size(32.dp),
                )
            }
        },
        supportingContent = {
            MediumEmphasis {
                val textStyle = MaterialTheme.typography.bodyMedium
                fun inlineTextContent(imageVector: ImageVector): InlineTextContent {
                    return InlineTextContent(
                        Placeholder(
                            width = 2.em,
                            height = textStyle.fontSize,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
                        )
                    ) {
                        Icon(
                            imageVector = imageVector,
                            contentDescription = null,
                        )
                    }
                }
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
                        "logged" to inlineTextContent(LisuIcons.HowToReg),
                        "not logged" to inlineTextContent(LisuIcons.PersonOff),
                    ),
                    style = textStyle,
                )
            }
        },
        trailingContent = {
            OverflowMenuButton {
                provider.boardModels.keys.forEach { boardId ->
                    val text = when (boardId) {
                        BoardId.Search -> "Search"
                        BoardId.Rank -> "Rank"
                        else -> return@forEach
                    }
                    DropdownMenuItem(
                        text = { Text(text) },
                        onClick = { onAction(ExploreAction.NavToProvider(provider.id, boardId)) },
                    )
                }

                provider.cookiesLogin?.let {
                    DropdownMenuItem(
                        text = { Text("Login by website") },
                        onClick = { onAction(ExploreAction.NavToLoginWebsite(provider.id)) },
                    )

                    DropdownMenuItem(
                        text = { Text("Login by cookies") },
                        onClick = { onAction(ExploreAction.NavToLoginCookies(provider.id)) },
                    )
                }
                if (provider.passwordLogin) {
                    DropdownMenuItem(
                        text = { Text("Login by passwords") },
                        onClick = { onAction(ExploreAction.NavToLoginPassword(provider.id)) },
                    )
                }
                if (provider.cookiesLogin != null || provider.passwordLogin) {
                    DropdownMenuItem(
                        text = { Text("Logout") },
                        onClick = { onAction(ExploreAction.Logout(provider)) },
                    )
                }
            }
        },
    )
}

@Preview
@Composable
private fun ExploreScaffoldPreview() {
    fun dummyProviderList(): Map<String, List<ProviderDto>> {
        return List(40) {
            LoremIpsum.provider()
        }.groupBy { it.lang }
    }

    LisuTheme {
        ExploreScaffold(
            providersResult = Result.success(dummyProviderList()),
            lastUsedProvider = null,
            onAction = { println(it) },
        )
    }
}
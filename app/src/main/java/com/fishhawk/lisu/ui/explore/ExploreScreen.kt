package com.fishhawk.lisu.ui.explore

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.*
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.datastore.setBlocking
import com.fishhawk.lisu.data.remote.model.ProviderDto
import com.fishhawk.lisu.ui.main.navToGlobalSearch
import com.fishhawk.lisu.ui.main.navToProvider
import com.fishhawk.lisu.ui.main.navToProviderLogin
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.theme.LisuTransition
import com.fishhawk.lisu.ui.widget.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.viewModel
import java.util.*

internal typealias ExploreActionHandler = (ExploreAction) -> Unit

internal sealed interface ExploreAction {
    object NavToGlobalSearch : ExploreAction
    data class NavToProvider(val provider: ProviderDto) : ExploreAction
    data class NavToProviderLogin(val provider: ProviderDto) : ExploreAction

    object Reload : ExploreAction
    data class Logout(val provider: ProviderDto) : ExploreAction
    data class Disable(val provider: ProviderDto) : ExploreAction
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
            is ExploreAction.NavToProviderLogin -> navController.navToProviderLogin(action.provider)
            ExploreAction.Reload -> viewModel.reload()
            is ExploreAction.Logout -> viewModel.logout(action.provider.id)
            is ExploreAction.Disable -> Unit
        }
    }

    Scaffold(
        topBar = { ToolBar(onAction) },
        content = { paddingValues ->
            LisuTransition {
                ProviderList(
                    viewState,
                    providers,
                    lastUsedProvider,
                    onAction,
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                )
            }
        }
    )
}

@Composable
private fun ToolBar(onAction: ExploreActionHandler) {
    LisuToolBar(title = stringResource(R.string.label_explore)) {
        IconButton(onClick = { onAction(ExploreAction.NavToGlobalSearch) }) {
            Icon(LisuIcons.TravelExplore, stringResource(R.string.action_global_search))
        }
    }
}

@Composable
private fun ProviderList(
    viewState: ViewState,
    providers: Map<String, List<ProviderDto>>,
    lastUsedProvider: ProviderDto?,
    onAction: ExploreActionHandler,
    modifier: Modifier = Modifier
) {
    StateView(
        modifier = modifier,
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

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
private fun ProviderListItem(
    provider: ProviderDto,
    onAction: ExploreActionHandler
) {
    val scope = rememberCoroutineScope()
    val bottomSheetHelper = LocalBottomSheetHelper.current

    ListItem(
        modifier = Modifier.combinedClickable(
            onClick = {
                onAction(ExploreAction.NavToProvider(provider))
                PR.lastUsedProvider.setBlocking(provider.id)
            },
            onLongClick = {
                val sheet = ExploreSheet(provider, onAction)
                scope.launch { bottomSheetHelper.open(sheet) }
            },
        ),
        icon = {
            val painter =
                if (provider.lang == "local") rememberVectorPainter(LisuIcons.LocalLibrary)
                else rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(provider.icon)
                        .size(Size.ORIGINAL)
                        .crossfade(true)
                        .build()
                )
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
        text = { OneLineText(text = provider.id) },
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
    )
}
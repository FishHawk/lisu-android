package com.fishhawk.driftinglibraryandroid.ui.gallery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.remote.model.*
import com.fishhawk.driftinglibraryandroid.ui.activity.setArgument
import com.fishhawk.driftinglibraryandroid.ui.base.StateView
import com.fishhawk.driftinglibraryandroid.ui.base.copyToClipboard
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationToolBar
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTransition
import com.fishhawk.driftinglibraryandroid.ui.theme.MaterialColors

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GalleryScreen(navController: NavHostController) {
    navController.setArgument<MangaOutline>("outline")
    navController.setArgument<ProviderInfo>("provider")

    ApplicationTransition {
        val viewModel = hiltViewModel<GalleryViewModel>()
        val detail by viewModel.detail.collectAsState()

        val scrollState = rememberScrollState()
        MangaDetail(navController, scrollState, detail)

        val toolBarVisibleState = remember { MutableTransitionState(false) }
        toolBarVisibleState.targetState = scrollState.value > 100
        AnimatedVisibility(
            visibleState = toolBarVisibleState,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow))
        ) {
            ApplicationToolBar(
                title = detail.title,
                navController = navController
            )
        }

    }
}

@Composable
private fun MangaDetail(
    navController: NavHostController,
    scrollState: ScrollState,
    detail: MangaDetail
) {
    val viewModel = hiltViewModel<GalleryViewModel>()
    val viewState by viewModel.viewState.collectAsState()

    fun search(keywords: String) {
        navController.currentBackStackEntry?.arguments =
            bundleOf(
                "keywords" to keywords,
                "provider" to detail.provider
            )
        if (detail.provider == null) navController.navigate("library-search")
        else navController.navigate("search/${detail.provider.id}")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        MangaHeader(navController, detail)
        StateView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            viewState = viewState,
            onRetry = { viewModel.reloadManga() }
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                detail.source?.let { MangaSource(it) }
                if (!detail.metadata.description.isNullOrBlank()) {
                    MangaDescription(detail.metadata.description)
                }
                detail.metadata.tags?.let { tags ->
                    val context = LocalContext.current
                    MangaTagGroups(tags,
                        onTagClick = { search(it) },
                        onTagLongClick = {
                            context.copyToClipboard(it, R.string.toast_manga_tag_saved)
                        }
                    )
                }
                MangaContent(viewModel, detail)
            }
        }
    }
}

@Composable
private fun MangaSource(source: Source) {
    Text(
        text = "From ${source.providerId} - ${source.mangaId} ${source.state}",
        color = when (source.state) {
            SourceState.DOWNLOADING -> MaterialColors.Blue400
            SourceState.WAITING -> MaterialColors.Green400
            SourceState.ERROR -> MaterialTheme.colors.error
            SourceState.UPDATED -> MaterialTheme.colors.onSurface
        }
    )
}

@Composable
private fun MangaDescription(description: String) {
    SelectionContainer {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = description,
                style = MaterialTheme.typography.body2
            )
        }
    }
}


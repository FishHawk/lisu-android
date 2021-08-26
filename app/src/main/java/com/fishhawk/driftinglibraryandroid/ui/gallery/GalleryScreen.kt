package com.fishhawk.driftinglibraryandroid.ui.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTransition
import com.fishhawk.driftinglibraryandroid.ui.theme.MaterialColors
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState

@Composable
fun GalleryScreen(navController: NavHostController) {
    navController.setArgument<MangaOutline>("outline")
    navController.setArgument<ProviderInfo>("provider")

    ApplicationTransition {
        val viewModel = hiltViewModel<GalleryViewModel>()
        val detail by viewModel.detail.collectAsState()
        CollapsingToolbarScaffold(
            modifier = Modifier.fillMaxSize(),
            state = rememberCollapsingToolbarScaffoldState(),
            scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
            toolbar = { MangaHeader(navController, detail) }
        ) {
            val viewState by viewModel.viewState.collectAsState()
            StateView(
                viewState = viewState,
                onRetry = { viewModel.reloadManga() }
            ) {
                MangaBody(navController, detail)
            }
        }
    }
}

@Composable
private fun MangaBody(navController: NavHostController, detail: MangaDetail) {
    val viewModel = hiltViewModel<GalleryViewModel>()

    fun search(keywords: String) {
        navController.currentBackStackEntry?.arguments =
            bundleOf(
                "keywords" to keywords,
                "provider" to detail.provider
            )
        if (detail.provider == null) navController.navigate("library-search")
        else navController.navigate("search/${detail.provider.id}")
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
//        when (val state = mangaList.loadState.refresh) {
//            is LoadState.Error -> item {
//                ErrorView(
//                    modifier = Modifier.fillParentMaxSize(),
//                    exception = state.error
//                ) { mangaList.retry() }
//            }
////            is LoadState.NotLoading -> {
////                if (mangaList.itemCount == 0) item { EmptyView() }
////            }
//        }

        detail.source?.let { item { MangaSource(it) } }
        detail.metadata.description?.let { item { MangaDescription(it) } }
        detail.metadata.tags?.let { tags ->
            item {
                val context = LocalContext.current
                MangaTagGroups(tags,
                    onTagClick = { search(it) },
                    onTagLongClick = {
                        context.copyToClipboard(it, R.string.toast_manga_tag_saved)
                    }
                )
            }
        }
        item { MangaContent(viewModel, detail) }
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


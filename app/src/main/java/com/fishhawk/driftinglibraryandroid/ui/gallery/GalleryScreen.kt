package com.fishhawk.driftinglibraryandroid.ui.gallery

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import com.fishhawk.driftinglibraryandroid.ui.base.copyToClipboard
import com.fishhawk.driftinglibraryandroid.ui.theme.ApplicationTransition
import com.fishhawk.driftinglibraryandroid.ui.theme.MaterialColors
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun GalleryScreen(navController: NavHostController) {
    navController.setArgument<MangaOutline>("outline")
    navController.setArgument<ProviderInfo>("provider")

    ApplicationTransition {
        Column {
            val viewModel = hiltViewModel<GalleryViewModel>()
            val detail by viewModel.detail.collectAsState()
            val isRefreshing by viewModel.isRefreshing.collectAsState()

            MangaHeader(navController, detail)
            SwipeRefresh(
                modifier = Modifier.fillMaxSize(),
                state = rememberSwipeRefreshState(isRefreshing),
                onRefresh = { viewModel.refreshManga() },
            ) { MangaBody(navController, detail) }
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

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        detail.source?.let { MangaSource(it) }
        detail.metadata.description?.let { MangaDescription(it) }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MangaDescription(description: String) {
    val context = LocalContext.current
    Text(
        text = description,
        style = MaterialTheme.typography.body2,
        modifier = Modifier.combinedClickable(
            onClick = {},
            onLongClick = {
                context.copyToClipboard(description, R.string.toast_manga_description_copied)
            }
        )
    )
}


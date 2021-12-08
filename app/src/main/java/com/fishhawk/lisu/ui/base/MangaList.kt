package com.fishhawk.lisu.ui.base

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.GridItemSpan
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.OriginalSize
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun RefreshableMangaList(
    mangaList: LazyPagingItems<MangaDto>,
    onCardClick: (manga: MangaDto) -> Unit = {},
    onCardLongClick: (manga: MangaDto) -> Unit = {}
) {
    val state = mangaList.loadState.refresh
    StateView(
        modifier = Modifier.fillMaxSize(),
        viewState = state.let {
            when (it) {
                LoadState.Loading -> ViewState.Loading
                is LoadState.NotLoading -> ViewState.Loaded
                is LoadState.Error -> ViewState.Failure(it.error)
            }
        },
        onRetry = { mangaList.retry() }
    ) {
        val isRefreshing = mangaList.loadState.refresh is LoadState.Loading
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { mangaList.refresh() },
        ) {
            MangaList(mangaList, onCardClick, onCardLongClick)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MangaList(
    mangaList: LazyPagingItems<MangaDto>,
    onCardClick: (manga: MangaDto) -> Unit = {},
    onCardLongClick: (manga: MangaDto) -> Unit = {}
) {
    LazyVerticalGrid(
        cells = GridCells.Adaptive(minSize = 96.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(mangaList.itemCount) { index ->
            MangaListCard(
                mangaList[index],
                onCardClick,
                onCardLongClick
            )
        }
        fun itemFullWidth(content: @Composable () -> Unit) {
            item(span = { GridItemSpan(maxCurrentLineSpan) }) { Box {} }
            item(span = { GridItemSpan(maxCurrentLineSpan) }) { content() }
        }
        when (val state = mangaList.loadState.append) {
            LoadState.Loading -> itemFullWidth { LoadingItem() }
            is LoadState.Error -> itemFullWidth { ErrorItem(state.error) { mangaList.retry() } }
            else -> Unit
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MangaListCard(
    manga: MangaDto?,
    onCardClick: (manga: MangaDto) -> Unit = {},
    onCardLongClick: (manga: MangaDto) -> Unit = {}
) {
    Card(
        modifier = Modifier.combinedClickable(
            onClick = { manga?.let { onCardClick(it) } },
            onLongClick = { manga?.let { onCardLongClick(it) } }
        )
    ) {
        Box(
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Max)
        ) {
            MangaCover(manga?.cover)

            val textStyle = MaterialTheme.typography.subtitle2.copy(
                shadow = Shadow(Color.White, Offset.Zero, 2f)
            )
            val textLineHeight = with(LocalDensity.current) {
                (textStyle.fontSize * 4 / 3).toDp() + 4.dp
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(textLineHeight * 2)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xAA000000)
                            )
                        )
                    )
            )
            Text(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                text = manga?.title ?: manga?.id ?: "",
                style = MaterialTheme.typography.subtitle2.copy(
                    shadow = Shadow(Color.White, Offset.Zero, 2f)
                ),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun MangaCover(
    cover: String?,
    modifier: Modifier = Modifier
) {
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(cover)
            .size(OriginalSize)
            .crossfade(true)
            .crossfade(500)
            .build()
    )

    Image(
        modifier = modifier
            .aspectRatio(0.75f)
            .placeholder(
                visible = painter.state is AsyncImagePainter.State.Loading,
                highlight = PlaceholderHighlight.fade()
            ),
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Crop
    )
}

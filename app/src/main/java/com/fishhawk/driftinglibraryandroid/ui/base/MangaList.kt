package com.fishhawk.driftinglibraryandroid.ui.base

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.imageloading.ImageLoadState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun RefreshableMangaList(
    mangaList: LazyPagingItems<MangaOutline>,
    onCardClick: (outline: MangaOutline) -> Unit = {},
    onCardLongClick: (outline: MangaOutline) -> Unit = {}
) {
    val isRefreshing = mangaList.loadState.refresh is LoadState.Loading
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = { mangaList.refresh() },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = mangaList.loadState.refresh) {
                is LoadState.Loading -> LoadingView()
                is LoadState.Error -> ErrorView(
                    message = state.error.localizedMessage!!,
                    onClickRetry = { mangaList.retry() }
                )
                is LoadState.NotLoading -> {
                    if (mangaList.itemCount == 0) EmptyView()
                    else MangaList(
                        mangaList, onCardClick, onCardLongClick
                    )
                }
            }
        }
    }
}

@Composable
private fun MangaList(
    mangaList: LazyPagingItems<MangaOutline>,
    onCardClick: (outline: MangaOutline) -> Unit = {},
    onCardLongClick: (outline: MangaOutline) -> Unit = {}
) {
    val nColumns = 3
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val rows = (mangaList.itemCount + nColumns - 1) / nColumns
        items(rows) { rowIndex ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (columnIndex in 0 until nColumns) {
                    val itemIndex = rowIndex * nColumns + columnIndex
                    if (itemIndex < mangaList.itemCount) {
                        Box(
                            modifier = Modifier.weight(1f, fill = true),
                            propagateMinConstraints = true
                        ) {
                            MangaListCard(
                                mangaList.getAsState(itemIndex).value,
                                onCardClick,
                                onCardLongClick
                            )
                        }
                    } else {
                        Spacer(Modifier.weight(1f, fill = true))
                    }
                }
            }
        }

        when (val state = mangaList.loadState.append) {
            is LoadState.Loading -> item { LoadingItem() }
            is LoadState.Error -> item {
                ErrorItem(
                    message = state.error.localizedMessage!!,
                    onClickRetry = { mangaList.retry() }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MangaListCard(
    outline: MangaOutline?,
    onCardClick: (outline: MangaOutline) -> Unit = {},
    onCardLongClick: (outline: MangaOutline) -> Unit = {}
) {
    Card(
        modifier = Modifier.combinedClickable(
            onClick = { outline?.let { onCardClick(it) } },
            onLongClick = { outline?.let { onCardLongClick(it) } }
        )
    ) {
        Box {
            MangaCover(cover = outline?.cover)
            Box(modifier = Modifier.matchParentSize()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .fillMaxHeight(0.4f)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xAA000000)),
                            )
                        ),
                ) {
                    Text(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.BottomStart),
                        text = outline?.title ?: "",
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
    }
}

@Composable
fun MangaCover(modifier: Modifier = Modifier, cover: String?) {
    Box(modifier = modifier.aspectRatio(0.75f)) {
        val painter = rememberCoilPainter(cover, fadeIn = true)
        Image(
            modifier = Modifier.matchParentSize(),
            painter = painter,
            contentDescription = "cover",
            contentScale = ContentScale.Crop
        )
        when (painter.loadState) {
            is ImageLoadState.Loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
//            is ImageLoadState.Error ->
//            ImageLoadState.Empty ->
            else -> Unit
        }
    }
}

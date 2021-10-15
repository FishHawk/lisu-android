package com.fishhawk.lisu.ui.base

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
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

@Composable
fun MangaList(
    mangaList: LazyPagingItems<MangaDto>,
    onCardClick: (manga: MangaDto) -> Unit = {},
    onCardLongClick: (manga: MangaDto) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val nColumns = 3
        val rows = (mangaList.itemCount + nColumns - 1) / nColumns
        items(rows) { rowIndex ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (columnIndex in 0 until nColumns) {
                    val itemIndex = rowIndex * nColumns + columnIndex
                    if (itemIndex < mangaList.itemCount) {
                        Box(
                            modifier = Modifier.weight(1f),
                            propagateMinConstraints = true
                        ) {
                            MangaListCard(
                                mangaList[itemIndex],
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
                ErrorItem(state.error) { mangaList.retry() }
            }
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
        Box {
            MangaCover(cover = manga?.cover)
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
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun MangaCover(modifier: Modifier = Modifier, cover: String?) {
    val painter = rememberImagePainter(cover) {
        crossfade(true)
        crossfade(500)
    }
    Image(
        modifier = modifier
            .aspectRatio(0.75f)
            .placeholder(
                visible = painter.state is ImagePainter.State.Loading,
                highlight = PlaceholderHighlight.fade()
            ),
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Crop
    )
}

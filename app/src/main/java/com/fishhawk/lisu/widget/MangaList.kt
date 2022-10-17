package com.fishhawk.lisu.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.fishhawk.lisu.data.network.base.PagedList
import com.fishhawk.lisu.data.network.model.MangaDto
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RefreshableMangaList(
    mangaList: PagedList<MangaDto>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onRequestNextPage: () -> Unit,
    onCardClick: (manga: MangaDto) -> Unit,
    onCardLongClick: (manga: MangaDto) -> Unit,
    aboveCover: @Composable BoxScope.(manga: MangaDto) -> Unit = {},
    behindCover: @Composable BoxScope.(manga: MangaDto) -> Unit = {},
) {
    var maxAccessed by rememberSaveable { mutableStateOf(0) }
    var hasRefreshed by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) hasRefreshed = true
        if (hasRefreshed && !isRefreshing) maxAccessed = 0
    }

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = onRefresh,
    ) {
        if (mangaList.list.isEmpty()) {
            EmptyView(modifier = Modifier.fillMaxSize())
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 96.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(mangaList.list) { index, manga ->
                if (index > maxAccessed) {
                    maxAccessed = index
                    if (
                        mangaList.appendState?.isSuccess == true &&
                        maxAccessed < mangaList.list.size + 30
                    ) onRequestNextPage()
                }
                Box {
                    behindCover(manga)
                    MangaCard(
                        manga = manga,
                        modifier = Modifier.combinedClickable(
                            onClick = { onCardClick(manga) },
                            onLongClick = { onCardLongClick(manga) },
                        )
                    )
                    aboveCover(manga)
                }
            }

            fun itemFullWidth(content: @Composable () -> Unit) {
                item(span = { GridItemSpan(maxCurrentLineSpan) }) { Box {} }
                item(span = { GridItemSpan(maxCurrentLineSpan) }) { content() }
            }
            mangaList.appendState
                ?.onFailure { itemFullWidth { ErrorItem(it) { onRequestNextPage() } } }
                ?: itemFullWidth { LoadingItem() }
        }
    }
}

@Composable
fun MangaCard(
    manga: MangaDto,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        Box {
            MangaCover(manga.cover)

            val textStyle = MaterialTheme.typography.bodySmall.copy(
                shadow = Shadow(Color.White, Offset.Zero, 1f)
            )
            Canvas(modifier = Modifier.matchParentSize()) {
                val shadowHeight = ((textStyle.fontSize * 4 / 3).toPx() + 8.dp.toPx()) * 2
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xAA000000)
                        ),
                        startY = size.height - shadowHeight,
                    ),
                )
            }
            Text(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                text = manga.title ?: manga.id,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = textStyle,
            )
        }
    }
}

@Composable
fun MangaCard(
    cover: String?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        Box {
            MangaCover(cover)
        }
    }
}

@Composable
private fun MangaCover(
    cover: String?,
    modifier: Modifier = Modifier,
) {
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(cover)
            .crossfade(true)
            .crossfade(500)
            .build()
    )

    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier
            .aspectRatio(0.75f)
            .placeholder(
                visible = painter.state is AsyncImagePainter.State.Loading,
                highlight = PlaceholderHighlight.fade()
            ),
        contentScale = ContentScale.Crop
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxScope.MangaBadge(
    text: String,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
) {
    Badge(
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(4.dp),
        containerColor = backgroundColor,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(2.dp),
        )
    }
}

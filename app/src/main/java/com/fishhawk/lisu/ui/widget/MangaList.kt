package com.fishhawk.lisu.ui.widget

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.Badge
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.fishhawk.lisu.data.remote.util.PagedList
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.data.remote.model.MangaKeyDto
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun RefreshableMangaList(
    result: Result<PagedList<MangaDto>>?,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onRequestNextPage: () -> Unit,
    modifier: Modifier = Modifier,
    selectedMangaList: SnapshotStateList<MangaKeyDto>? = null,
    decorator: @Composable BoxScope.(manga: MangaDto?) -> Unit = {},
    onCardClick: (manga: MangaDto) -> Unit = {},
    onCardLongClick: (manga: MangaDto) -> Unit = {}
) {
    StateView(
        result = result,
        onRetry = onRetry,
        modifier = modifier.fillMaxSize(),
    ) {
        val isRefreshing = result == null
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = onRefresh,
        ) {
            MangaList(
                mangaList = it,
                onRequestNextPage = onRequestNextPage,
                selectedMangaList = selectedMangaList,
                badge = decorator,
                onCardClick = onCardClick,
                onCardLongClick = onCardLongClick
            )
        }
    }
}

@Composable
fun MangaList(
    mangaList: PagedList<MangaDto>,
    onRequestNextPage: () -> Unit,
    selectedMangaList: SnapshotStateList<MangaKeyDto>? = null,
    badge: @Composable BoxScope.(manga: MangaDto?) -> Unit = {},
    onCardClick: (manga: MangaDto) -> Unit = {},
    onCardLongClick: (manga: MangaDto) -> Unit = {}
) {
    var maxAccessed by remember { mutableStateOf(0) }
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
                ) {
                    onRequestNextPage()
                }
            }
            Box {
                MangaCard(
                    manga = manga,
                    selected = selectedMangaList?.contains(manga.key) ?: false,
                    onClick = onCardClick,
                    onLongClick = onCardLongClick
                )
                badge(manga)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MangaCard(
    manga: MangaDto?,
    selected: Boolean = false,
    onClick: (manga: MangaDto) -> Unit = {},
    onLongClick: (manga: MangaDto) -> Unit = {}
) {
    Card(
        modifier = Modifier.combinedClickable(
            onClick = { manga?.let { onClick(it) } },
            onLongClick = { manga?.let { onLongClick(it) } }
        )
    ) {
        Box {
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
                style = MaterialTheme.typography.caption.copy(
                    shadow = Shadow(Color.White, Offset.Zero, 1f)
                ),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (selected) {
                val color = MaterialTheme.colors.primary.copy(alpha = 0.3f)
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawRect(
                        color = color,
                        size = size,
                        blendMode = BlendMode.SrcOver
                    )
                }
            }
        }
    }
}

@Composable
fun MangaCover(
    cover: String?,
    modifier: Modifier = Modifier
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


@Composable
fun BoxScope.MangaBadge(
    text: String,
    backgroundColor: Color = MaterialTheme.colors.primary,
) {
    Badge(
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(4.dp),
        backgroundColor = backgroundColor
    ) {
        Text(text = text, modifier = Modifier.padding(2.dp))
    }
}

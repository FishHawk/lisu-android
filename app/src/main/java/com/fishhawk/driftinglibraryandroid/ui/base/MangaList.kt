package com.fishhawk.driftinglibraryandroid.ui.base

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asLiveData
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.MetadataOutline
import com.fishhawk.driftinglibraryandroid.util.setNext
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.imageloading.ImageLoadState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MangaDisplayModeButton() {
    IconButton(onClick = { GlobalPreference.run { displayMode.setNext() } }) {
        val mode by GlobalPreference.displayMode.asFlow().collectAsState(
            GlobalPreference.DisplayMode.GRID
        )
        val icon = when (mode) {
            GlobalPreference.DisplayMode.GRID -> Icons.Filled.ViewModule
            GlobalPreference.DisplayMode.LINEAR -> Icons.Filled.ViewList
        }
        Icon(icon, contentDescription = "Display mode")
    }
}

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
    ) { MangaList(mangaList, onCardClick, onCardLongClick) }
}

@Composable
fun MangaList(
    mangaList: LazyPagingItems<MangaOutline>,
    onCardClick: (outline: MangaOutline) -> Unit = {},
    onCardLongClick: (outline: MangaOutline) -> Unit = {}
) {
    val mode by GlobalPreference.displayMode.asFlow().asLiveData().observeAsState()
    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = mangaList.loadState.refresh) {
            is LoadState.Loading -> LoadingView()
            is LoadState.Error -> ErrorView(
                message = state.error.localizedMessage!!,
                onClickRetry = { mangaList.retry() }
            )
            is LoadState.NotLoading -> {
                if (mangaList.itemCount == 0) EmptyView()
                else {
                    when (mode) {
                        GlobalPreference.DisplayMode.GRID -> MangaListGrid(
                            mangaList, onCardClick, onCardLongClick
                        )
                        GlobalPreference.DisplayMode.LINEAR -> MangaListLinear(
                            mangaList, onCardClick, onCardLongClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MangaListLinear(
    mangaList: LazyPagingItems<MangaOutline>,
    onCardClick: (outline: MangaOutline) -> Unit = {},
    onCardLongClick: (outline: MangaOutline) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(mangaList) { MangaCardLinear(it, onCardClick, onCardLongClick) }
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
@Preview(widthDp = 300)
@Composable
fun MangaCardLinear(
    outline: MangaOutline? = outlineExample,
    onCardClick: (outline: MangaOutline) -> Unit = {},
    onCardLongClick: (outline: MangaOutline) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .height(95.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = { outline?.let { onCardClick(it) } },
                onLongClick = { outline?.let { onCardLongClick(it) } }
            )
    ) {
        Row {
            MangaCover(cover = outline?.cover)
            Column(
                Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                outline?.title?.let {
                    Text(
                        text = it,
                        style = typography.subtitle1,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                outline?.metadata?.authors?.let {
                    Text(
                        text = it.joinToString(";"),
                        style = typography.body2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.weight(1f, fill = true))
                outline?.updateTime?.let {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = dateFormat.format(Date(it)),
                        style = typography.body2.copy(textAlign = TextAlign.End),
                    )
                }
            }
        }
    }
}

@Composable
fun MangaListGrid(
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
                            MangaCardGrid(
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

val outlineExample =
    MangaOutline(
        "id", null, 1, null,
        MetadataOutline("title", listOf("author"), null), true
    )

@OptIn(ExperimentalFoundationApi::class)
@Preview(widthDp = 120)
@Composable
fun MangaCardGrid(
    outline: MangaOutline? = outlineExample,
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
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color(0xAA000000)
                            ),
                        )
                    ),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = outline?.title ?: "",
                    style = typography.subtitle2.copy(
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

@Composable
fun MangaCover(cover: String?) {
    Box(modifier = Modifier.aspectRatio(0.75f)) {
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

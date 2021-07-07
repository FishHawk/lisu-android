package com.fishhawk.driftinglibraryandroid.ui.base

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asLiveData
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.MetadataOutline
import com.fishhawk.driftinglibraryandroid.util.setNext
import com.google.accompanist.coil.rememberCoilPainter
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

@OptIn(ExperimentalFoundationApi::class)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MangaList(
    mangaList: LazyPagingItems<MangaOutline>,
    onCardClick: (outline: MangaOutline) -> Unit = {},
    onCardLongClick: (outline: MangaOutline) -> Unit = {}
) {
    val mode by GlobalPreference.displayMode.asFlow().asLiveData().observeAsState()
    when (mode) {
        GlobalPreference.DisplayMode.GRID -> MangaListGrid(
            mangaList, onCardClick, onCardLongClick
        )
        GlobalPreference.DisplayMode.LINEAR -> MangaListLinear(
            mangaList, onCardClick, onCardLongClick
        )
    }
}

@ExperimentalFoundationApi
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

@ExperimentalFoundationApi
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
            Box(Modifier.aspectRatio(0.75f)) {
                val request = ImageRequest.Builder(LocalContext.current)
                    .data(outline?.cover)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
                Image(
                    painter = rememberCoilPainter(request, fadeIn = true),
                    contentDescription = outline?.id,
                    contentScale = ContentScale.Crop
                )
            }
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

@ExperimentalFoundationApi
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
                        ) { MangaCardGrid(mangaList.peek(itemIndex), onCardClick, onCardLongClick) }
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
        Box(
            modifier = Modifier.aspectRatio(0.75f),
            contentAlignment = Alignment.BottomStart
        ) {
            if (outline?.cover != null) {
                val request = ImageRequest.Builder(LocalContext.current)
                    .data(outline.cover)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
                Image(
                    painter = rememberCoilPainter(request, fadeIn = true),
                    contentDescription = outline.id,
                    contentScale = ContentScale.Crop
                )
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color(0xaa000000)
                            ),
                        )
                    )
            )
            Text(
                modifier = Modifier.padding(8.dp),
                text = outline?.title ?: "",
                style = typography.subtitle2.copy(
                    shadow = Shadow(Color.White, Offset.Zero, 2f)
                ),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

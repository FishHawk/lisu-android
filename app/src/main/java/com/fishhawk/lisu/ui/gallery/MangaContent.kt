package com.fishhawk.lisu.ui.gallery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material.icons.outlined.ViewModule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.database.model.ReadingHistory
import com.fishhawk.lisu.data.datastore.ChapterDisplayMode
import com.fishhawk.lisu.data.datastore.ChapterDisplayOrder
import com.fishhawk.lisu.data.datastore.collectAsState
import com.fishhawk.lisu.data.datastore.setNext
import com.fishhawk.lisu.data.network.model.Chapter
import com.fishhawk.lisu.data.network.model.MangaDetailDto
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.ui.theme.MediumEmphasis
import com.fishhawk.lisu.widget.itemsVerticalGrid
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

internal fun LazyListScope.mangaContent(
    mode: ChapterDisplayMode,
    order: ChapterDisplayOrder,
    detail: MangaDetailDto,
    history: ReadingHistory?,
    onAction: (GalleryAction) -> Unit,
) {
    val enablePreview = detail.collections.keys.size == 1 &&
            detail.collections.keys.first().isEmpty() &&
            detail.collections.values.first().size == 1 &&
            detail.collections.values.first().first().id.isEmpty()

    if (enablePreview) {
        if (detail.chapterPreviews.isEmpty()) {
            mangaContentEmpty()
        } else {
            mangaContentPreview(
                previews = detail.chapterPreviews,
                onPageClick = { page ->
                    onAction(GalleryAction.NavToReader("", "", page))
                },
            )
        }
    } else {
        if (detail.collections.isEmpty()) {
            mangaContentEmpty()
        } else {
            val isMarked = { collectionId: String, chapterId: String ->
                history
                    ?.let { it.collectionId == collectionId && it.chapterId == chapterId }
                    ?: false
            }
            mangaContentCollections(
                mode = mode,
                order = order,
                collections = detail.collections,
                isMarked = isMarked,
                onChapterClick = { collectionId, chapterId ->
                    val page = if (isMarked(collectionId, chapterId)) history!!.page else 0
                    onAction(GalleryAction.NavToReader(collectionId, chapterId, page))
                },
            )
        }
    }
}

private fun LazyListScope.mangaContentEmpty() {
    item {
        MediumEmphasis {
            Text(
                text = stringResource(R.string.gallery_no_content_hint),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

private fun LazyListScope.mangaContentPreview(
    previews: List<String>,
    onPageClick: (Int) -> Unit = {},
) {
    itemsVerticalGrid(
        items = previews.withIndex(),
        nColumns = 3,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) { (index, url) ->
        PreviewPage(
            url = url,
            page = index + 1,
            onPageClick = onPageClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PreviewPage(
    url: String,
    page: Int,
    onPageClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier
                .clickable { onPageClick(page) }
                .fillMaxWidth()
                .aspectRatio(0.75f),
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .crossfade(true)
                    .crossfade(500)
                    .build()
            ),
            contentDescription = null,
            contentScale = ContentScale.Fit,
        )
        MediumEmphasis {
            Text(
                text = page.toString(),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun LazyListScope.mangaContentCollections(
    mode: ChapterDisplayMode,
    order: ChapterDisplayOrder,
    collections: Map<String, List<Chapter>>,
    isMarked: (String, String) -> Boolean,
    onChapterClick: (String, String) -> Unit,
) {
    item { MangaContentChapterHeader() }
    collections.onEach { (collectionId, chapters) ->
        if (collectionId.isNotEmpty()) {
            item {
                MediumEmphasis {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(2.dp),
                        text = collectionId,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        val orderedChapters = when (order) {
            ChapterDisplayOrder.Ascend -> chapters
            ChapterDisplayOrder.Descend -> chapters.reversed()
        }
        when (mode) {
            ChapterDisplayMode.Grid -> {
                itemsVerticalGrid(
                    items = orderedChapters,
                    nColumns = 4,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) { chapter ->
                    ChapterGrid(
                        chapter = chapter,
                        isMarked = isMarked(collectionId, chapter.id),
                        onChapterClick = { onChapterClick(collectionId, chapter.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            ChapterDisplayMode.Linear -> {
                items(orderedChapters) { chapter ->
                    ChapterLinear(
                        chapter = chapter,
                        isMarked = isMarked(collectionId, chapter.id),
                        onChapterClick = { onChapterClick(collectionId, chapter.id) },
                    )
                    if (chapter != orderedChapters.lastOrNull()) {
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MangaContentChapterHeader() {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Chapters:",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.weight(1f))

        val mode by PR.chapterDisplayMode.collectAsState()
        val scope = rememberCoroutineScope()
        IconButton(onClick = { scope.launch { PR.chapterDisplayOrder.setNext() } }) {
            Icon(LisuIcons.Sort, stringResource(R.string.action_switch_display_mode))
        }
        IconButton(onClick = { scope.launch { PR.chapterDisplayMode.setNext() } }) {
            val icon = when (mode) {
                ChapterDisplayMode.Grid -> LisuIcons.ViewModule
                ChapterDisplayMode.Linear -> LisuIcons.ViewList
            }
            Icon(icon, stringResource(R.string.action_switch_sort_mode))
        }
    }
}

@Composable
private fun ChapterGrid(
    chapter: Chapter,
    isMarked: Boolean,
    onChapterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLocked = chapter.isLocked

    val color = MaterialTheme.colorScheme
        .run { if (isMarked) primary else surface }
        .let { if (isLocked) it.copy(alpha = 0.1f) else it }
    val border = if (isLocked) BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
    ) else null
    val elevation = if (isLocked) 0.dp else 2.dp

    val textColor = MaterialTheme.colorScheme
        .run { if (isMarked) onPrimary else onSurface }
        .let { if (isLocked) it.copy(alpha = 0.1f) else it }

    Surface(
        modifier = if (isLocked) modifier else modifier.clickable { onChapterClick() },
        shape = RectangleShape,
        color = color,
        border = border,
        shadowElevation = elevation,
    ) {
        Box {
            if (isLocked) {
                Icon(
                    imageVector = LisuIcons.Lock,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(2.dp)
                        .size(12.dp)
                        .align(Alignment.TopStart),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                )
            } else {
                ChapterNewMark(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopStart),
                    updateTime = chapter.updateTime
                )
            }
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 8.dp),
                text = chapter.name ?: chapter.id,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ChapterLinear(
    chapter: Chapter,
    isMarked: Boolean,
    onChapterClick: () -> Unit = {},
) {
    val isLocked = chapter.isLocked
    val modifier = Modifier
        .fillMaxWidth()
        .let {
            if (!isLocked) it.clickable { onChapterClick() }
            else it
        }
        .padding(vertical = 12.dp, horizontal = 16.dp)

    val textColor = MaterialTheme.colorScheme
        .run { if (isMarked) primary else onSurface }
        .let { if (isLocked) it.copy(alpha = 0.1f) else it }

    ProvideTextStyle(value = MaterialTheme.typography.bodyMedium.copy(color = textColor)) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "¶")
            Text(
                text = listOf(chapter.name, chapter.title).joinToString(" "),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private val TriangleShape = GenericShape { size, _ ->
    lineTo(0f, size.height)
    lineTo(size.width, 0f)
}

@Composable
private fun ChapterNewMark(modifier: Modifier, updateTime: Long?) {
    updateTime?.let {
        val date = Instant.ofEpochSecond(updateTime).atZone(ZoneId.systemDefault()).toLocalDate()
        val days = ChronoUnit.DAYS.between(date, LocalDate.now())
        if (days <= 5L) Box(
            modifier = modifier
                .clip(TriangleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

package com.fishhawk.lisu.ui.gallery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material.icons.outlined.ViewModule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.datastore.ChapterDisplayMode
import com.fishhawk.lisu.data.datastore.ChapterDisplayOrder
import com.fishhawk.lisu.data.datastore.collectAsState
import com.fishhawk.lisu.data.datastore.setNext
import com.fishhawk.lisu.data.network.model.Chapter
import com.fishhawk.lisu.ui.theme.LisuIcons
import com.fishhawk.lisu.widget.VerticalGrid
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Composable
internal fun MangaContentCollections(
    collections: Map<String, List<Chapter>>,
    isMarked: (String, String) -> Boolean,
    onChapterClick: (String, String) -> Unit,
) {
    MangaContentChapterHeader()
    collections.onEach { (collectionId, chapters) ->
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp),
            text = collectionId,
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center
        )
        ChapterList(
            chapters,
            { isMarked(collectionId, it) },
            { onChapterClick(collectionId, it) }
        )
    }
}

@Composable
internal fun MangaContentChapters(
    chapters: List<Chapter>,
    isMarked: (String, String) -> Boolean,
    onChapterClick: (String, String) -> Unit,
) {
    MangaContentChapterHeader()
    ChapterList(chapters, { isMarked(" ", it) }, { onChapterClick(" ", it) })
}

@Composable
private fun MangaContentChapterHeader() {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Chapters:",
            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium)
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
private fun ChapterList(
    chapters: List<Chapter>,
    isMarked: (String) -> Boolean,
    onChapterClick: (String) -> Unit = {},
) {
    val mode by PR.chapterDisplayMode.collectAsState()
    val order by PR.chapterDisplayOrder.collectAsState()
    val orderedChapters = when (order) {
        ChapterDisplayOrder.Ascend -> chapters
        ChapterDisplayOrder.Descend -> chapters.reversed()
    }
    when (mode) {
        ChapterDisplayMode.Grid -> ChapterListGrid(orderedChapters, isMarked, onChapterClick)
        ChapterDisplayMode.Linear -> ChapterListLinear(orderedChapters, isMarked, onChapterClick)
    }
}

@Composable
private fun ChapterListGrid(
    chapters: List<Chapter>,
    isMarked: (String) -> Boolean,
    onChapterClick: (String) -> Unit = {},
) {
    VerticalGrid(
        items = chapters,
        nColumns = 4,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) { _, it ->
        Box(modifier = Modifier.weight(1f)) {
            ChapterGrid(it, isMarked(it.id)) {
                onChapterClick(it.id)
            }
        }
    }
}

@Composable
private fun ChapterGrid(
    chapter: Chapter,
    isMarked: Boolean,
    onChapterClick: () -> Unit = {},
) {
    val isLocked = chapter.isLocked

    val modifier =
        if (isLocked) Modifier
        else Modifier.clickable { onChapterClick() }
    val color = MaterialTheme.colors
        .run { if (isMarked) primary else surface }
        .let { if (isLocked) it.copy(alpha = ContentAlpha.disabled) else it }
    val border = if (isLocked) BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colors.onSurface.copy(alpha = 0.05f)
    ) else null
    val elevation = if (isLocked) 0.dp else 2.dp

    val textColor = MaterialTheme.colors
        .run { if (isMarked) onPrimary else onSurface }
        .let { if (isLocked) it.copy(alpha = ContentAlpha.disabled) else it }

    Surface(
        modifier = modifier,
        shape = RectangleShape,
        color = color,
        border = border,
        elevation = elevation,
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
                    tint = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled),
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
                style = MaterialTheme.typography.body1.copy(fontSize = 12.sp),
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ChapterListLinear(
    chapters: List<Chapter>,
    isMarked: (String) -> Boolean,
    onChapterClick: (String) -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        chapters.onEach {
            ChapterLinear(it, isMarked(it.id)) {
                onChapterClick(it.id)
            }
            if (it != chapters.lastOrNull()) {
                Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.06f))
            }
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

    val textColor = MaterialTheme.colors
        .run { if (isMarked) primary else onSurface }
        .let { if (isLocked) it.copy(alpha = ContentAlpha.disabled) else it }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = listOf("¶", chapter.name, chapter.title).joinToString(" "),
            color = textColor,
            style = MaterialTheme.typography.caption,
        )
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
                .background(MaterialTheme.colors.primary)
        )
    }
}

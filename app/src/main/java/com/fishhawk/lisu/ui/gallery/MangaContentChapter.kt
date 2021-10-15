package com.fishhawk.lisu.ui.gallery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.data.datastore.ChapterDisplayMode
import com.fishhawk.lisu.data.datastore.ChapterDisplayOrder
import com.fishhawk.lisu.data.datastore.collectAsState
import com.fishhawk.lisu.data.datastore.setNext
import com.fishhawk.lisu.data.remote.model.ChapterDto
import kotlinx.coroutines.launch

@Composable
internal fun MangaContentCollections(
    collections: Map<String, List<ChapterDto>>,
    isMarked: (String, String) -> Boolean,
    onChapterClick: (String, String) -> Unit
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
    chapters: List<ChapterDto>,
    isMarked: (String, String) -> Boolean,
    onChapterClick: (String, String) -> Unit
) {
    MangaContentChapterHeader()
    ChapterList(chapters, { isMarked(" ", it) }, { onChapterClick(" ", it) })
}

@Composable
private fun MangaContentChapterHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "Chapters:",
            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium)
        )
        Spacer(modifier = Modifier.weight(1f, fill = true))

        val mode by PR.chapterDisplayMode.collectAsState()
        val scope = rememberCoroutineScope()
        IconButton(onClick = { scope.launch { PR.chapterDisplayOrder.setNext() } }) {
            Icon(Icons.Default.Sort, contentDescription = "Order")
        }
        IconButton(onClick = { scope.launch { PR.chapterDisplayMode.setNext() } }) {
            val icon = when (mode) {
                ChapterDisplayMode.Grid -> Icons.Default.ViewModule
                ChapterDisplayMode.Linear -> Icons.Default.ViewList
            }
            Icon(icon, contentDescription = "Display mode")
        }
    }
}

@Composable
private fun ChapterList(
    chapters: List<ChapterDto>,
    isMarked: (String) -> Boolean,
    onChapterClick: (String) -> Unit = {}
) {
    val mode by PR.chapterDisplayMode.collectAsState()
    when (mode) {
        ChapterDisplayMode.Grid -> ChapterListGrid(chapters, isMarked, onChapterClick)
        ChapterDisplayMode.Linear -> ChapterListLinear(chapters, isMarked, onChapterClick)
    }
}

@Composable
private fun ChapterListGrid(
    chapters: List<ChapterDto>,
    isMarked: (String) -> Boolean,
    onChapterClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val order by PR.chapterDisplayOrder.collectAsState()
        val nColumns = 4
        val rows = (chapters.size + nColumns - 1) / nColumns
        (0..rows).map { rowIndex ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (columnIndex in 0 until nColumns) {
                    val cellIndex = rowIndex * nColumns + columnIndex
                    if (cellIndex < chapters.size) {
                        val chapterIndex = when (order) {
                            ChapterDisplayOrder.Ascend -> cellIndex
                            ChapterDisplayOrder.Descend -> chapters.size - cellIndex - 1
                        }
                        Box(
                            modifier = Modifier.weight(1f),
                            propagateMinConstraints = true
                        ) {
                            val chapter = chapters[chapterIndex]
                            ChapterGrid(chapter, isMarked(chapter.id)) {
                                onChapterClick(chapter.id)
                            }
                        }
                    } else {
                        Spacer(Modifier.weight(1f, fill = true))
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterGrid(
    chapter: ChapterDto,
    isMarked: Boolean,
    onChapterClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier.clickable { onChapterClick() },
        shape = RectangleShape,
        elevation = 2.dp,
        color = MaterialTheme.colors.run { if (isMarked) primary else surface }
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 8.dp),
            text = chapter.name,
            style = MaterialTheme.typography.body1.copy(fontSize = 12.sp),
            color = if (isMarked) Color.White else MaterialTheme.colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
private fun ChapterListLinear(
    chapters: List<ChapterDto>,
    isMarked: (String) -> Boolean,
    onChapterClick: (String) -> Unit = {}
) {
    val order by PR.chapterDisplayOrder.collectAsState()
    Column(modifier = Modifier.fillMaxWidth()) {
        when (order) {
            ChapterDisplayOrder.Ascend -> chapters
            ChapterDisplayOrder.Descend -> chapters.reversed()
        }.map { ChapterLinear(it, isMarked(it.id)) { onChapterClick(it.id) } }
    }
}

@Composable
private fun ChapterLinear(
    chapter: ChapterDto,
    isMarked: Boolean,
    onChapterClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChapterClick() },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.padding(vertical = 16.dp),
            text = chapter.name,
            style = MaterialTheme.typography.subtitle2,
            color = if (isMarked) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
        )
        Text(
            modifier = Modifier.padding(vertical = 16.dp),
            text = chapter.title,
            style = MaterialTheme.typography.subtitle2
        )
    }
}
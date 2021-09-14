package com.fishhawk.driftinglibraryandroid.ui.gallery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.fishhawk.driftinglibraryandroid.PR
import com.fishhawk.driftinglibraryandroid.data.datastore.*
import com.fishhawk.driftinglibraryandroid.data.remote.model.Chapter
import com.fishhawk.driftinglibraryandroid.data.remote.model.ChapterCollection
import kotlinx.coroutines.launch

internal typealias OnChapterClickListener = (collection: String, chapter: String, page: Int) -> Unit

internal data class ChapterMark(
    val collection: String,
    val chapter: String,
    val page: Int
)

@Composable
internal fun MangaContentChapter(
    collections: List<ChapterCollection>,
    chapterMark: ChapterMark? = null,
    onChapterClick: OnChapterClickListener
) {
    val mode by PR.chapterDisplayMode.collectAsState()
    val viewModel = hiltViewModel<GalleryViewModel>()

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "Chapters:",
            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium)
        )
        Spacer(modifier = Modifier.weight(1f, fill = true))
        IconButton(onClick = { viewModel.viewModelScope.launch { PR.chapterDisplayOrder.setNext() } }) {
            Icon(Icons.Filled.Sort, contentDescription = "Order")
        }

        IconButton(onClick = { viewModel.viewModelScope.launch { PR.chapterDisplayMode.setNext() } }) {
            val icon = when (mode) {
                ChapterDisplayMode.Grid -> Icons.Filled.ViewModule
                ChapterDisplayMode.Linear -> Icons.Filled.ViewList
            }
            Icon(icon, contentDescription = "Display mode")
        }
    }

    when (mode) {
        ChapterDisplayMode.Grid -> ChapterListGrid(
            collections, chapterMark, onChapterClick
        )
        ChapterDisplayMode.Linear -> ChapterListLinear(
            collections, chapterMark, onChapterClick
        )
    }
}

@Composable
private fun ChapterListLinear(
    collections: List<ChapterCollection>,
    chapterMark: ChapterMark? = null,
    onChapterClick: OnChapterClickListener
) {
    val order by PR.chapterDisplayOrder.collectAsState()
    val chapters = collections.flatMap { collection ->
        collection.chapters.map { chapter ->
            Pair(collection.id, chapter)
        }.let {
            if (order == ChapterDisplayOrder.Descend) it.asReversed() else it
        }
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        chapters.map { (collection, chapter) ->
            if (chapterMark != null &&
                chapterMark.collection == collection &&
                chapterMark.chapter == chapter.id
            ) {
                ChapterLinear(chapter, true) {
                    onChapterClick(collection, chapter.id, chapterMark.page)
                }
            } else {
                ChapterLinear(chapter, false) {
                    onChapterClick(collection, chapter.id, 0)
                }
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChapterClick() },
        horizontalArrangement = Arrangement.spacedBy(4.dp)
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
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
private fun ChapterListGrid(
    collections: List<ChapterCollection>,
    chapterMark: ChapterMark? = null,
    onChapterClick: OnChapterClickListener
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val order by PR.chapterDisplayOrder.collectAsState()
        collections.map { collection ->
            if (collection.chapters.isEmpty()) return@map

            if (collection.id.isNotBlank()) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(2.dp),
                    text = collection.id,
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center
                )
            }

            val nColumns = 4
            val rows = (collection.chapters.size + nColumns - 1) / nColumns
            (0..rows).map { rowIndex ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (columnIndex in 0 until nColumns) {
                        val cellIndex = rowIndex * nColumns + columnIndex
                        if (cellIndex < collection.chapters.size) {
                            val chapterIndex = when (order) {
                                ChapterDisplayOrder.Ascend -> cellIndex
                                ChapterDisplayOrder.Descend -> collection.chapters.size - cellIndex - 1
                            }

                            Box(
                                modifier = Modifier.weight(1f),
                                propagateMinConstraints = true
                            ) {
                                val chapter = collection.chapters[chapterIndex]
                                val isMarked = chapterMark != null &&
                                        chapterMark.collection == collection.id &&
                                        chapterMark.chapter == chapter.id

                                ChapterGrid(chapter, isMarked) {
                                    onChapterClick(
                                        collection.id,
                                        chapter.id,
                                        if (isMarked) chapterMark!!.page else 0
                                    )
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
}

@Composable
private fun ChapterGrid(
    chapter: Chapter,
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

package com.fishhawk.driftinglibraryandroid.ui.gallery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.asLiveData
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import com.fishhawk.driftinglibraryandroid.data.remote.model.Chapter
import com.fishhawk.driftinglibraryandroid.data.remote.model.ChapterCollection
import com.fishhawk.driftinglibraryandroid.util.setNext

typealias OnChapterClickListener = (collectionIndex: Int, chapterIndex: Int, pageIndex: Int) -> Unit

data class ChapterMark(
    val collectionIndex: Int,
    val chapterIndex: Int,
    val pageIndex: Int
)

@Composable
fun MangaContentChapter(
    collections: List<ChapterCollection>,
    chapterMark: ChapterMark? = null,
    onChapterClick: OnChapterClickListener
) {
    val mode by GlobalPreference.chapterDisplayMode.asFlow().asLiveData().observeAsState(
        GlobalPreference.chapterDisplayMode.get()
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Chapters:", style = MaterialTheme.typography.subtitle1)
        Spacer(modifier = Modifier.weight(1f, fill = true))
        IconButton(onClick = { GlobalPreference.run { chapterDisplayOrder.setNext() } }) {
            Icon(Icons.Filled.Sort, contentDescription = "Order")
        }

        IconButton(onClick = { GlobalPreference.run { chapterDisplayMode.setNext() } }) {
            val icon = when (mode) {
                GlobalPreference.ChapterDisplayMode.GRID -> Icons.Filled.ViewModule
                GlobalPreference.ChapterDisplayMode.LINEAR -> Icons.Filled.ViewList
            }
            Icon(icon, contentDescription = "Display mode")
        }
    }

    when (mode) {
        GlobalPreference.ChapterDisplayMode.GRID -> ChapterListGrid(
            collections, chapterMark, onChapterClick
        )
        GlobalPreference.ChapterDisplayMode.LINEAR -> ChapterListLinear(
            collections, chapterMark, onChapterClick
        )
    }
}

@Composable
fun ChapterListLinear(
    collections: List<ChapterCollection>,
    chapterMark: ChapterMark? = null,
    onChapterClick: OnChapterClickListener
) {
    val order by GlobalPreference.chapterDisplayOrder.asFlow().asLiveData().observeAsState()
    val chapters = collections.flatMapIndexed { collectionIndex, collection ->
        collection.chapters.mapIndexed { chapterIndex, chapter ->
            Triple(collectionIndex, chapterIndex, chapter)
        }.let {
            if (order == GlobalPreference.ChapterDisplayOrder.DESCEND) it.asReversed() else it
        }
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        chapters.map { (collectionIndex, chapterIndex, chapter) ->
            if (chapterMark != null &&
                chapterMark.collectionIndex == collectionIndex &&
                chapterMark.chapterIndex == chapterIndex
            ) {
                ChapterLinear(chapter, true) {
                    onChapterClick(collectionIndex, chapterIndex, chapterMark.pageIndex)
                }
            } else {
                ChapterLinear(chapter, false) {
                    onChapterClick(collectionIndex, chapterIndex, 0)
                }
            }
        }
    }
}

@Composable
fun ChapterLinear(
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
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
            text = chapter.name,
            style = MaterialTheme.typography.subtitle2,
            color = if (isMarked) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
        )
        Text(
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
            text = chapter.title,
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
fun ChapterListGrid(
    collections: List<ChapterCollection>,
    chapterMark: ChapterMark? = null,
    onChapterClick: OnChapterClickListener
) {
    val nColumns = 4
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        collections.mapIndexed { collectionIndex, it ->
            if (it.chapters.isEmpty()) return@mapIndexed
            if (it.id.isNotBlank()) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(2.dp),
                    text = it.id,
                    textAlign = TextAlign.Center
                )
            }

            val rows = (it.chapters.size + nColumns - 1) / nColumns
            (0..rows).map { rowIndex ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (columnIndex in 0 until nColumns) {
                        val chapterIndex = rowIndex * nColumns + columnIndex
                        if (chapterIndex < it.chapters.size) {
                            Box(
                                modifier = Modifier.weight(1f, fill = true),
                                propagateMinConstraints = true
                            ) {
                                val chapter = it.chapters[chapterIndex]
                                if (chapterMark != null &&
                                    chapterMark.collectionIndex == collectionIndex &&
                                    chapterMark.chapterIndex == chapterIndex
                                ) {
                                    ChapterGrid(chapter, true) {
                                        onChapterClick(
                                            collectionIndex,
                                            chapterIndex,
                                            chapterMark.pageIndex
                                        )
                                    }
                                } else {
                                    ChapterGrid(chapter, false) {
                                        onChapterClick(collectionIndex, chapterIndex, 0)
                                    }
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
fun ChapterGrid(
    chapter: Chapter,
    isMarked: Boolean,
    onChapterClick: () -> Unit = {},
) {
    Surface(
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(1.dp, if (isMarked) MaterialTheme.colors.primary else Color.Gray),
        color = if (isMarked) MaterialTheme.colors.primary else Color.Transparent
    ) {
        Text(
            modifier = Modifier
                .padding(top = 8.dp, bottom = 8.dp, start = 2.dp, end = 2.dp)
                .fillMaxWidth()
                .clickable { onChapterClick() },
            text = chapter.name,
            style = MaterialTheme.typography.body1.copy(fontSize = 12.sp),
            color = if (isMarked) Color.White else MaterialTheme.colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

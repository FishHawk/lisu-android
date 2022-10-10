package com.fishhawk.lisu.ui.gallery

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.database.model.ReadingHistory
import com.fishhawk.lisu.data.datastore.ChapterDisplayMode
import com.fishhawk.lisu.data.datastore.ChapterDisplayOrder
import com.fishhawk.lisu.data.network.model.MangaDetailDto

internal fun LazyListScope.mangaContent(
    mode: ChapterDisplayMode,
    order: ChapterDisplayOrder,
    detail: MangaDetailDto,
    history: ReadingHistory?,
    onAction: (GalleryAction) -> Unit,
) {
    val collections = detail.collections
    if (collections.isEmpty()) {
        item {
            Text(
                text = stringResource(R.string.gallery_no_chapters_hint),
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colors.primary,
                textAlign = TextAlign.Center,
            )
        }
    } else if (
        detail.chapterPreviews.isNotEmpty() &&
        collections.keys.size == 1 &&
        collections.keys.first().isEmpty() &&
        collections.values.first().size == 1 &&
        collections.values.first().first().id.isEmpty()
    ) {
        mangaContentPreview(
            previews = detail.chapterPreviews,
            onPageClick = { page ->
                onAction(GalleryAction.NavToReader(" ", " ", page))
            },
        )
    } else {
        val isMarked = { collectionId: String, chapterId: String ->
            history?.let { it.collectionId == collectionId && it.chapterId == chapterId } ?: false
        }
        mangaContentCollections(
            mode = mode,
            order = order,
            collections = collections,
            isMarked = isMarked,
            onChapterClick = { collectionId, chapterId ->
                val page = if (isMarked(collectionId, chapterId)) history!!.page else 0
                onAction(GalleryAction.NavToReader(collectionId, chapterId, page))
            },
        )
    }
}
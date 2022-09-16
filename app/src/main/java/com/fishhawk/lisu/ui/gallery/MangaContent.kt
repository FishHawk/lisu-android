package com.fishhawk.lisu.ui.gallery

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.fishhawk.lisu.R
import com.fishhawk.lisu.data.database.model.ReadingHistory
import com.fishhawk.lisu.data.network.model.MangaContent
import com.fishhawk.lisu.data.network.model.MangaDetailDto

@Composable
internal fun MangaContent(
    detail: MangaDetailDto,
    history: ReadingHistory?,
    onAction: GalleryActionHandler,
) {
    val isMarked = { collectionId: String, chapterId: String ->
        history?.let {
            it.collectionId == collectionId
                    && it.chapterId == chapterId
        } ?: false
    }
    val onChapterClick = { collectionId: String, chapterId: String ->
        val page = if (isMarked(collectionId, chapterId)) history!!.page else 0
        onAction(GalleryAction.NavToReader(collectionId, chapterId, page))
    }
    val onPageClick = { page: Int ->
        onAction(GalleryAction.NavToReader(" ", " ", page))
    }

    when (val content = detail.content) {
        is MangaContent.Collections ->
            if (content.collections.isNotEmpty())
                MangaContentCollections(content.collections, isMarked, onChapterClick)
            else MangaNoChapter()
        is MangaContent.Chapters ->
            if (content.chapters.isNotEmpty())
                MangaContentChapters(content.chapters, isMarked, onChapterClick)
            else MangaNoChapter()
        is MangaContent.SingleChapter ->
            if (content.preview.isNotEmpty())
                MangaContentPreview(content.preview, onPageClick)
            else MangaNoChapter()
    }
}

@Composable
private fun MangaNoChapter() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.gallery_no_chapters_hint),
            color = MaterialTheme.colors.primary
        )
    }
}

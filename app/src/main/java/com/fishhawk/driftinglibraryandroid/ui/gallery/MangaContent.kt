package com.fishhawk.driftinglibraryandroid.ui.gallery

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.database.model.ReadingHistory
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaDetailDto

@Composable
internal fun MangaContent(
    detail: MangaDetailDto,
    history: ReadingHistory?,
    onAction: GalleryActionHandler
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
    detail.collections?.also { MangaContentCollections(it, isMarked, onChapterClick) }
        ?: detail.chapters?.also { MangaContentChapters(it, isMarked, onChapterClick) }
        ?: detail.preview?.also { MangaContentPreview(it, onPageClick) }
        ?: MangaNoChapter()
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

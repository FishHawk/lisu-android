package com.fishhawk.driftinglibraryandroid.ui.gallery

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.fishhawk.driftinglibraryandroid.R
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaDetail
import com.fishhawk.driftinglibraryandroid.ui.base.navToReaderActivity

@Composable
fun MangaContent(viewModel: GalleryViewModel, detail: MangaDetail) {
    val context = LocalContext.current
    val hasPreview = detail.collections.size == 1 && !detail.preview.isNullOrEmpty()
    val hasChapter = detail.collections.isNotEmpty()
    when {
        hasPreview -> MangaContentPreview(
            preview = detail.preview!!,
            onPageClick = {
                context.navToReaderActivity(detail, 0, 0, it)
            })
        hasChapter -> {
            val history by viewModel.history.collectAsState()
            MangaContentChapter(
                collections = detail.collections,
                chapterMark = history?.let {
                    ChapterMark(it.collectionIndex, it.chapterIndex, it.pageIndex)
                },
                onChapterClick = { collectionIndex, chapterIndex, pageIndex ->
                    context.navToReaderActivity(detail, collectionIndex, chapterIndex, pageIndex)
                }
            )
        }
        else -> MangaNoChapter()
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
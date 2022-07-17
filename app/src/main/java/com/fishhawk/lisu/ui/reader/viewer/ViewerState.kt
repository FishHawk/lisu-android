package com.fishhawk.lisu.ui.reader.viewer

import androidx.annotation.IntRange
import androidx.compose.foundation.lazy.LazyListState
import com.fishhawk.lisu.ui.reader.ReaderPage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState

sealed class ViewerState(
    val pages: List<ReaderPage>,
    val requestMoveToPrevChapter: () -> Unit,
    val requestMoveToNextChapter: () -> Unit,
) {
    @get:IntRange(from = 0)
    abstract val position: Int

    abstract val isRtl: Boolean

    val size = pages.size

    val imageSize = pages.count { it is ReaderPage.Image || it is ReaderPage.Empty }

    val imagePosition
        get() = when (val page = pages[position]) {
            is ReaderPage.Image -> page.index
            ReaderPage.Empty -> 0
            is ReaderPage.NextChapterState -> imageSize - 1
            is ReaderPage.PrevChapterState -> 0
        }

    abstract suspend fun scrollToPage(@IntRange(from = 0) page: Int)

    suspend fun scrollToImagePage(@IntRange(from = 0) index: Int) {
        pages.filterIsInstance<ReaderPage.Image>()
            .getOrNull(index)
            ?.let { pages.indexOf(it) }
            .takeIf { it != -1 }
            ?.let { scrollToPage(it) }
    }

    abstract suspend fun toPrev()
    abstract suspend fun toNext()

    suspend fun toLeft() = if (isRtl) toNext() else toPrev()
    suspend fun toRight() = if (isRtl) toPrev() else toNext()

    @OptIn(ExperimentalPagerApi::class)
    class Pager(
        val state: PagerState,
        override val isRtl: Boolean,
        pages: List<ReaderPage>,
        requestMoveToPrevChapter: () -> Unit,
        requestMoveToNextChapter: () -> Unit,
    ) : ViewerState(
        pages = pages,
        requestMoveToPrevChapter = requestMoveToPrevChapter,
        requestMoveToNextChapter = requestMoveToNextChapter,
    ) {
        override val position: Int
            get() = state.currentPage

        override suspend fun scrollToPage(page: Int) {
            state.scrollToPage(page)
        }

        override suspend fun toPrev() {
            if (position > 0) scrollToPage(position - 1)
            else requestMoveToPrevChapter()
        }

        override suspend fun toNext() {
            if (position < size - 1) scrollToPage(position + 1)
            else requestMoveToNextChapter()
        }
    }

    class Webtoon(
        val state: LazyListState,
        pages: List<ReaderPage>,
        requestMoveToPrevChapter: () -> Unit,
        requestMoveToNextChapter: () -> Unit,
    ) : ViewerState(
        pages = pages,
        requestMoveToPrevChapter = requestMoveToPrevChapter,
        requestMoveToNextChapter = requestMoveToNextChapter,
    ) {
        override val isRtl = false

        override val position: Int
            get() = state.firstVisibleItemIndex

        override suspend fun scrollToPage(page: Int) {
            state.scrollToItem(page)
        }

        override suspend fun toPrev() {
            if (position > 0) scrollToPage(position - 1)
            else requestMoveToPrevChapter()
        }

        override suspend fun toNext() {
            if (position < size - 1) scrollToPage(position + 1)
            else requestMoveToNextChapter()
        }
    }
}

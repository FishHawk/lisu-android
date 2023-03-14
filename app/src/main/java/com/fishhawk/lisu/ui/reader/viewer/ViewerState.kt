package com.fishhawk.lisu.ui.reader.viewer

import androidx.annotation.IntRange
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import com.fishhawk.lisu.ui.reader.ReaderPage

sealed class ViewerState(
    val pages: List<ReaderPage>,
    val requestMoveToPrevChapter: () -> Unit,
    val requestMoveToNextChapter: () -> Unit,
) {
    @get:IntRange(from = 0)
    abstract val position: Int

    abstract val isRtl: Boolean

    abstract suspend fun scrollToPage(@IntRange(from = 0) page: Int)

    suspend fun scrollToImagePage(@IntRange(from = 0) index: Int) {
        scrollToPage(
            pages.indexOfFirst {
                it is ReaderPage.Image && it.index == index
            }
        )
    }

    suspend fun toPrev() {
        if (position > 0) scrollToPage(position - 1)
        else requestMoveToPrevChapter()
    }

    suspend fun toNext() {
        if (position < pages.size - 1) scrollToPage(position + 1)
        else requestMoveToNextChapter()
    }

    suspend fun toLeft() = if (isRtl) toNext() else toPrev()
    suspend fun toRight() = if (isRtl) toPrev() else toNext()

    @OptIn(ExperimentalFoundationApi::class)
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
    }
}

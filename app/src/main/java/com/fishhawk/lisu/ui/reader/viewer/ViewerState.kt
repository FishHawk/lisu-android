package com.fishhawk.lisu.ui.reader.viewer

import androidx.annotation.IntRange
import androidx.compose.foundation.lazy.LazyListState
import com.fishhawk.lisu.ui.reader.ReaderPage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState

sealed class ViewerState(val pages: List<ReaderPage>) {
    @get:IntRange(from = 0)
    abstract val position: Int

    val size = pages.size

    val imageSize = pages.count { it is ReaderPage.Image || it is ReaderPage.Empty }

    val imagePosition
        get() = when (val page = pages[position]) {
            is ReaderPage.Image -> page.index
            else -> 0
        }

    abstract suspend fun scrollToPage(@IntRange(from = 0) page: Int)

    suspend fun scrollToImagePage(@IntRange(from = 0) index: Int) {
        pages.filterIsInstance<ReaderPage.Image>()
            .getOrNull(index)
            ?.let { pages.indexOf(it) }
            .takeIf { it != -1 }
            ?.let { scrollToPage(it) }
    }

    @OptIn(ExperimentalPagerApi::class)
    class Pager(
        pages: List<ReaderPage>,
        val state: PagerState
    ) : ViewerState(pages) {
        override val position: Int
            get() = state.currentPage

        override suspend fun scrollToPage(page: Int) {
            state.scrollToPage(page)
        }
    }

    class Webtoon(
        pages: List<ReaderPage>,
        val state: LazyListState,
    ) : ViewerState(pages) {
        override val position: Int
            get() = state.firstVisibleItemIndex

        override suspend fun scrollToPage(page: Int) {
            state.scrollToItem(page)
        }
    }
}

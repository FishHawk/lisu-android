package com.fishhawk.driftinglibraryandroid.ui.reader.viewer

import androidx.annotation.IntRange
import androidx.compose.foundation.lazy.LazyListState
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState

sealed interface ViewerState {
    @get:IntRange(from = 0)
    val position: Int

    @get:IntRange(from = 0)
    val size: Int

    suspend fun scrollToPage(@IntRange(from = 0) page: Int)

    @OptIn(ExperimentalPagerApi::class)
    class Pager(val state: PagerState) : ViewerState {
        override val position: Int
            get() = state.currentPage

        override val size: Int
            get() = state.pageCount

        override suspend fun scrollToPage(page: Int) = state.scrollToPage(page)
    }

    class List(val state: LazyListState) : ViewerState {
        override val position: Int
            get() = state.firstVisibleItemIndex

        override val size: Int
            get() = state.layoutInfo.totalItemsCount

        override suspend fun scrollToPage(page: Int) = state.scrollToItem(page)
    }
}

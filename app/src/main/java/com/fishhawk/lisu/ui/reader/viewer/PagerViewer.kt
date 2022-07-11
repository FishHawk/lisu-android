package com.fishhawk.lisu.ui.reader.viewer

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.data.datastore.collectAsState
import com.fishhawk.lisu.ui.reader.ReaderPage
import com.fishhawk.lisu.widget.zoomable
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
internal fun PagerViewer(
    modifier: Modifier = Modifier,
    isMenuOpened: MutableState<Boolean>,
    state: ViewerState.Pager,
    onLongPress: (page: ReaderPage.Image) -> Unit,
) {
    val scope = rememberCoroutineScope()

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val nestedScrollConnection = remember(state.isRtl) {
        nestedScrollConnection(
            requestMoveToPrevChapter = state.requestMoveToPrevChapter,
            requestMoveToNextChapter = state.requestMoveToNextChapter,
            isPrepareToPrev = { if (state.isRtl) it.x < -10 else it.x > 10 },
            isPrepareToNext = { if (state.isRtl) it.x > 10 else it.x < -10 },
        )
    }

    Box(
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable()
            .nestedScroll(nestedScrollConnection),
    ) {
        fun onTap(offset: Offset, size: IntSize) {
            if (isMenuOpened.value) isMenuOpened.value = false
            else {
                val x = offset.x / size.width
                when {
                    x < 0.25 -> scope.launch { state.toLeft() }
                    x > 0.75 -> scope.launch { state.toRight() }
                    else -> isMenuOpened.value = true
                }
            }
        }

        val isPageIntervalEnabled by PR.isPageIntervalEnabled.collectAsState()

        val layoutDirection = if (state.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            HorizontalPager(
                count = state.pages.size,
                modifier = Modifier.fillMaxSize(),
                state = state.state,
                key = {
                    when (val page = state.pages[it]) {
                        ReaderPage.Empty -> 0
                        is ReaderPage.Image -> page.index
                        is ReaderPage.NextChapterState -> -1
                        is ReaderPage.PrevChapterState -> state.pages.size
                    }
                },
                itemSpacing = if (isPageIntervalEnabled) 16.dp else 0.dp
            ) { index ->
                val pageModifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onTap(it, size) })
                    }
                when (val page = state.pages[index]) {
                    is ReaderPage.Image -> {
                        ImagePage(
                            page = page,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .zoomable(
                                    onTap = { onTap(it, size) },
                                    onLongPress = { onLongPress(page) },
                                ),
                            stateModifier = pageModifier,
                        )
                    }
                    ReaderPage.Empty ->
                        EmptyPage(modifier = pageModifier)
                    is ReaderPage.NextChapterState ->
                        NextChapterStatePage(page = page, modifier = pageModifier)
                    is ReaderPage.PrevChapterState ->
                        PrevChapterStatePage(page = page, modifier = pageModifier)
                }
            }
        }
    }
}
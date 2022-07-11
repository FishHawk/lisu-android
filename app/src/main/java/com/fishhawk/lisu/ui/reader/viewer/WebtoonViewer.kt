package com.fishhawk.lisu.ui.reader.viewer

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.data.datastore.collectAsState
import com.fishhawk.lisu.ui.reader.ReaderPage
import com.fishhawk.lisu.widget.zoomable

@Composable
internal fun WebtoonViewer(
    modifier: Modifier = Modifier,
    isMenuOpened: MutableState<Boolean>,
    state: ViewerState.Webtoon,
    onLongPress: (page: ReaderPage.Image) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val nestedScrollConnection = remember {
        nestedScrollConnection(
            requestMoveToPrevChapter = state.requestMoveToPrevChapter,
            requestMoveToNextChapter = state.requestMoveToNextChapter,
            isPrepareToPrev = { it.y > 10 },
            isPrepareToNext = { it.y < -10 },
        )
    }

    Box(
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable()
            .nestedScroll(nestedScrollConnection)
            .zoomable(
                onLongPress = { offset ->
                    state.state.layoutInfo.visibleItemsInfo.forEach {
                        if (it.offset <= offset.y && it.offset + it.size >= offset.y) {
                            val page = state.pages[it.index]
                            if (page is ReaderPage.Image) onLongPress(page)
                        }
                    }
                },
                onTap = {
                    isMenuOpened.value = !isMenuOpened.value
                },
            )
    ) {
        val isPageIntervalEnabled by PR.isPageIntervalEnabled.collectAsState()
        val itemSpacing = if (isPageIntervalEnabled) 16.dp else 0.dp

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state.state,
            verticalArrangement = Arrangement.spacedBy(itemSpacing)
        ) {
            items(state.pages) { page ->
                val pageModifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                when (page) {
                    is ReaderPage.Image -> ImagePage(
                        page = page,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        stateModifier = pageModifier,
                    )
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
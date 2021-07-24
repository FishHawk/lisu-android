package com.fishhawk.driftinglibraryandroid.ui.reader

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fishhawk.driftinglibraryandroid.data.datastore.PR
import com.fishhawk.driftinglibraryandroid.data.datastore.ReaderMode
import com.fishhawk.driftinglibraryandroid.data.datastore.collectAsState
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch

//        reader.onPageLongClicked = { position, url ->
//            if (P.isLongTapDialogEnabled.get())
//                ReaderPageSheet(this, object : ReaderPageSheet.Listener {
//                    override fun onRefresh() {
//                        reader.refreshPage(position)
//                    }
//
//                    override fun onSave() {
//                        val prefix = viewModel.makeImageFilenamePrefix()
//                            ?: return toast(R.string.toast_chapter_not_loaded)
////                        saveImage(url, "$prefix-$position")
//                    }
//
//                    override fun onShare() {
//                        val prefix = viewModel.makeImageFilenamePrefix()
//                            ?: return toast(R.string.toast_chapter_not_loaded)
////                        lifecycleScope.shareImage(this, url, "$prefix-$position")
//                    }
//                }).show()

@OptIn(ExperimentalPagerApi::class, ExperimentalComposeUiApi::class, InternalCoroutinesApi::class)
@Composable
fun ReaderContent(
    pagerState: PagerState,
    pointer: ReaderViewModel.ReaderChapterPointer
) {
    val viewModel = viewModel<ReaderViewModel>()
    val scope = rememberCoroutineScope()

    LaunchedEffect(pointer) {
        if (pagerState.pageCount > 0) {
            val page = pointer.startPage.coerceAtMost(pagerState.pageCount - 1)
            pagerState.scrollToPage(page)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.updateReadingHistory(pagerState.currentPage)
    }

    fun toNext() {
        if (pagerState.currentPage < pagerState.pageCount - 1)
            scope.launch { pagerState.scrollToPage(pagerState.currentPage + 1) }
        else viewModel.moveToNextChapter()
    }

    fun toPrev() {
        if (pagerState.currentPage > 0)
            scope.launch { pagerState.scrollToPage(pagerState.currentPage - 1) }
        else viewModel.moveToPrevChapter()
    }

    val readerDirection by PR.readerMode.collectAsState()
    val isRtl = readerDirection == ReaderMode.Rtl
    fun toLeft() = if (isRtl) toNext() else toPrev()
    fun toRight() = if (isRtl) toPrev() else toNext()

    val useVolumeKey by PR.useVolumeKey.collectAsState()
    val invertVolumeKey by PR.invertVolumeKey.collectAsState()

    val focusRequester = remember { FocusRequester() }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            var prepareToNext = false
            var prepareToPrev = false

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source == NestedScrollSource.Drag) {
                    when (readerDirection) {
                        ReaderMode.Ltr -> {
                            prepareToNext = available.x < -10
                            prepareToPrev = available.x > 10
                        }
                        ReaderMode.Rtl -> {
                            prepareToNext = available.x > 10
                            prepareToPrev = available.x < -10
                        }
                        ReaderMode.Vertical -> {
                            prepareToNext = available.y < -10
                            prepareToPrev = available.y > 10
                        }
                        else -> Unit
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (prepareToNext) {
                    prepareToNext = false
                    viewModel.moveToNextChapter()
                } else if (prepareToPrev) {
                    prepareToPrev = false
                    viewModel.moveToPrevChapter()
                }
                return Velocity.Zero
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .nestedScroll(nestedScrollConnection)
        .focusRequester(focusRequester)
        .focusable()
        .onPreviewKeyEvent {
            when (it.type) {
                KeyEventType.KeyDown -> {
                    when (it.key) {
                        Key.VolumeUp ->
                            if (useVolumeKey && !viewModel.isMenuOpened.value) {
                                if (invertVolumeKey) toNext() else toPrev()
                            } else return@onPreviewKeyEvent false
                        Key.VolumeDown ->
                            if (useVolumeKey && !viewModel.isMenuOpened.value) {
                                if (invertVolumeKey) toPrev() else toNext()
                            } else return@onPreviewKeyEvent false
                        else -> return@onPreviewKeyEvent false
                    }
                }
                KeyEventType.KeyUp -> {
                    when (it.key) {
                        Key.Menu -> viewModel.isMenuOpened.value = !viewModel.isMenuOpened.value

                        Key.N -> viewModel.moveToNextChapter()
                        Key.P -> viewModel.moveToPrevChapter()

                        Key.DirectionUp, Key.PageUp -> toPrev()
                        Key.DirectionDown, Key.PageDown -> toNext()

                        Key.DirectionLeft -> toLeft()
                        Key.DirectionRight -> toRight()
                        else -> return@onPreviewKeyEvent false
                    }
                }
                else -> return@onPreviewKeyEvent false
            }
            true
        }
    ) {
        PagerReader(
            pagerState,
            pointer.currChapter.images,
            onTap = { viewModel.isMenuOpened.value = !viewModel.isMenuOpened.value }
        )
    }

    DisposableEffect(Unit) {
        focusRequester.requestFocus()
        onDispose { }
    }
}

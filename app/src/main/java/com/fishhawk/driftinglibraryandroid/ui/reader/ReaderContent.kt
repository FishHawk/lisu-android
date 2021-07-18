package com.fishhawk.driftinglibraryandroid.ui.reader

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fishhawk.driftinglibraryandroid.data.preference.P
import com.fishhawk.driftinglibraryandroid.data.preference.collectAsState
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

    val readerDirection by P.readingDirection.collectAsState()
    val isRtl = readerDirection == P.ReadingDirection.RTL
    fun toLeft() = if (isRtl) toNext() else toPrev()
    fun toRight() = if (isRtl) toPrev() else toNext()

    val useVolumeKey by P.useVolumeKey.collectAsState()
    val invertVolumeKey by P.invertVolumeKey.collectAsState()

    val focusRequester = remember { FocusRequester() }
    Box(modifier = Modifier
        .fillMaxSize()
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

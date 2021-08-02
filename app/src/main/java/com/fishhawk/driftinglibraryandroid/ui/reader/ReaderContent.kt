package com.fishhawk.driftinglibraryandroid.ui.reader

import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.fishhawk.driftinglibraryandroid.PR
import com.fishhawk.driftinglibraryandroid.data.datastore.ReaderMode
import com.fishhawk.driftinglibraryandroid.data.datastore.collectAsState
import com.fishhawk.driftinglibraryandroid.util.interceptor.ProgressInterceptor
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.VerticalPager
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

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
        val onTap = { offset: Offset ->
            if (viewModel.isMenuOpened.value) viewModel.isMenuOpened.value = false
            else when {
                offset.x < 0.25 -> toLeft()
                offset.x > 0.75 -> toRight()
                else -> viewModel.isMenuOpened.value = !viewModel.isMenuOpened.value
            }
        }

        val isPageIntervalEnabled by PR.isPageIntervalEnabled.collectAsState()
        val itemSpacing = if (isPageIntervalEnabled) 16.dp else 0.dp

        val layoutDirection =
            if (readerDirection == ReaderMode.Rtl) LayoutDirection.Rtl
            else LayoutDirection.Ltr

        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            if (readerDirection == ReaderMode.Vertical) {
                VerticalPager(state = pagerState, itemSpacing = itemSpacing) { index ->
                    Page(
                        position = index.plus(1),
                        url = pointer.currChapter.images[index],
                        onTap = onTap
                    )
                }
            } else {
                HorizontalPager(state = pagerState, itemSpacing = itemSpacing) { index ->
                    Page(
                        position = index.plus(1),
                        url = pointer.currChapter.images[index],
                        onTap = onTap
                    )
                }
            }
        }
    }

    DisposableEffect(Unit) {
        focusRequester.requestFocus()
        onDispose { }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun Page(
    position: Int,
    url: String,
    onTap: ((Offset) -> Unit)? = null
) {
    val painter = rememberImagePainter(url)
    var layout: LayoutCoordinates? = null
    Box(
        modifier = Modifier
            .onGloballyPositioned { layout = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { /* Called when the gesture starts */ },
                    onDoubleTap = { /* Called on Double Tap */ },
                    onLongPress = { /* Called on Long Press */ },
                    onTap = { offset ->
                        layout?.let {
                            onTap?.invoke(
                                Offset(
                                    x = offset.x / it.parentCoordinates!!.size.width,
                                    y = offset.y / it.parentCoordinates!!.size.height
                                )
                            )
                        }
                    }
                )
            }
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Fit
        )
        when (val state = painter.state) {
            is ImagePainter.State.Loading -> {
                var progress by remember { mutableStateOf<Float?>(null) }
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = position.toString(),
                        style = MaterialTheme.typography.h3
                    )
                    progress?.let { CircularProgressIndicator(progress = it) }
                        ?: CircularProgressIndicator()
                }
                DisposableEffect(state) {
                    val key = url.toHttpUrlOrNull().toString()
                    ProgressInterceptor.addListener(key) { progress = it }
                    onDispose {
                        ProgressInterceptor.removeListener(key)
                        progress = null
                    }
                }
            }
            is ImagePainter.State.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = position.toString(),
                        style = MaterialTheme.typography.h3
                    )
                    Text(
                        text = state.throwable.message ?: "",
                        style = MaterialTheme.typography.body1
                    )
                    TextButton(onClick = { }) { Text("retry") }
                }
            }
            else -> Unit
        }
    }
}

package com.fishhawk.driftinglibraryandroid.ui.reader

import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
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
import coil.size.OriginalSize
import com.fishhawk.driftinglibraryandroid.PR
import com.fishhawk.driftinglibraryandroid.data.datastore.collectAsState
import com.fishhawk.driftinglibraryandroid.util.interceptor.ProgressInterceptor
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
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
fun PagerReader(
    state: PagerState,
    pointer: ReaderViewModel.ReaderChapterPointer,
    isRtl: Boolean
) {
    val viewModel = viewModel<ReaderViewModel>()
    val scope = rememberCoroutineScope()

    fun toNext() {
        if (state.currentPage < state.pageCount - 1)
            scope.launch { state.scrollToPage(state.currentPage + 1) }
        else viewModel.moveToNextChapter()
    }

    fun toPrev() {
        if (state.currentPage > 0)
            scope.launch { state.scrollToPage(state.currentPage - 1) }
        else viewModel.moveToPrevChapter()
    }

    fun toLeft() = if (isRtl) toNext() else toPrev()
    fun toRight() = if (isRtl) toPrev() else toNext()

    val useVolumeKey by PR.useVolumeKey.collectAsState()
    val invertVolumeKey by PR.invertVolumeKey.collectAsState()

    val focusRequester = remember { FocusRequester() }

    val nestedScrollConnection = remember(isRtl) {
        nestedScrollConnection(
            viewModel,
            { if (isRtl) it.x < -10 else it.x > 10 },
            { if (isRtl) it.x > 10 else it.x < -10 }
        )
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
        val isPageIntervalEnabled by PR.isPageIntervalEnabled.collectAsState()
        val itemSpacing = if (isPageIntervalEnabled) 16.dp else 0.dp

        val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = state,
                itemSpacing = itemSpacing
            ) { index ->
                Page(
                    position = index.plus(1),
                    url = pointer.currChapter.images[index],
                    onTap = { offset ->
                        if (viewModel.isMenuOpened.value) viewModel.isMenuOpened.value = false
                        else when {
                            offset.x < 0.25 -> toLeft()
                            offset.x > 0.75 -> toRight()
                            else -> viewModel.isMenuOpened.value = !viewModel.isMenuOpened.value
                        }
                    }
                )
            }
        }
    }

    DisposableEffect(Unit) {
        focusRequester.requestFocus()
        onDispose { }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ListReader(
    state: ReaderState.List,
    pointer: ReaderViewModel.ReaderChapterPointer
) {
    val viewModel = viewModel<ReaderViewModel>()
    val scope = rememberCoroutineScope()

    fun toNext() {
        if (state.position < state.size - 1)
            scope.launch { state.scrollToPage(state.position + 1) }
        else viewModel.moveToNextChapter()
    }

    fun toPrev() {
        if (state.position > 0)
            scope.launch { state.scrollToPage(state.position - 1) }
        else viewModel.moveToPrevChapter()
    }

    val useVolumeKey by PR.useVolumeKey.collectAsState()
    val invertVolumeKey by PR.invertVolumeKey.collectAsState()

    val focusRequester = remember { FocusRequester() }

    val nestedScrollConnection = remember {
        nestedScrollConnection(viewModel, { it.y > 10 }, { it.y < -10 })
    }

    Box(
        modifier = Modifier.nestedScroll(nestedScrollConnection)
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
//
//                            Key.DirectionUp, Key.PageUp -> toPrev()
//                            Key.DirectionDown, Key.PageDown -> toNext()
//
//                            Key.DirectionLeft -> toLeft()
//                            Key.DirectionRight -> toRight()
                            else -> return@onPreviewKeyEvent false
                        }
                    }
                    else -> return@onPreviewKeyEvent false
                }
                true
            }
    ) {
        val isPageIntervalEnabled by PR.isPageIntervalEnabled.collectAsState()
        val itemSpacing = if (isPageIntervalEnabled) 16.dp else 0.dp

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state.state,
            verticalArrangement = Arrangement.spacedBy(itemSpacing)
        ) {
            itemsIndexed(pointer.currChapter.images) { index, url ->
                Page(
                    position = index.plus(1),
                    url = url,
                    onTap = {
                        viewModel.isMenuOpened.value = !viewModel.isMenuOpened.value
                    }
                )
            }
        }
    }

    DisposableEffect(Unit) {
        focusRequester.requestFocus()
        onDispose { }
    }
}

private fun nestedScrollConnection(
    viewModel: ReaderViewModel,
    isPrepareToPrev: (offset: Offset) -> Boolean,
    isPrepareToNext: (offset: Offset) -> Boolean
) =
    object : NestedScrollConnection {
        var prepareToNext = false
        var prepareToPrev = false

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            if (source == NestedScrollSource.Drag) {
                prepareToPrev = isPrepareToPrev(available)
                prepareToNext = isPrepareToNext(available)
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

@OptIn(ExperimentalCoilApi::class)
@Composable
fun Page(
    position: Int,
    url: String,
    onTap: ((Offset) -> Unit)
) {
    val painter = rememberImagePainter(url) { size(OriginalSize) }
    var layout: LayoutCoordinates? = null
    Box(
        modifier = Modifier
            .wrapContentHeight()
            .onGloballyPositioned { layout = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { /* Called when the gesture starts */ },
                    onDoubleTap = { /* Called on Double Tap */ },
                    onLongPress = { /* Called on Long Press */ },
                    onTap = { offset ->
                        onTap(
                            layout?.let {
                                Offset(
                                    x = offset.x / it.parentCoordinates!!.size.width,
                                    y = offset.y / it.parentCoordinates!!.size.height
                                )
                            } ?: offset
                        )
                    }
                )
            }
    ) {
        Image(
            modifier = Modifier.fillMaxWidth(),
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.FillWidth
        )

        when (val state = painter.state) {
            is ImagePainter.State.Loading -> {
                var progress by remember { mutableStateOf<Float?>(null) }
                Column(
                    modifier = Modifier
                        .height(240.dp)
                        .align(Alignment.Center),
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
                    modifier = Modifier
                        .height(240.dp)
                        .align(Alignment.Center),
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

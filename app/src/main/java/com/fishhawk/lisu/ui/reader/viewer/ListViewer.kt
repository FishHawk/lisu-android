package com.fishhawk.lisu.ui.reader.viewer

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.data.datastore.collectAsState
import com.fishhawk.lisu.ui.reader.ReaderViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun ListViewer(
    state: ViewerState.List,
    pointer: ReaderViewModel.ReaderChapterPointer,
    onLongPress: ((drawable: Drawable, position: Int) -> Unit)
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
        modifier = Modifier
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
                    },
                    onLongPress = onLongPress
                )
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
private fun Page(
    position: Int,
    url: String,
    onTap: ((Offset) -> Unit),
    onLongPress: ((drawable: Drawable, position: Int) -> Unit)
) {
    var retryHash by remember { mutableStateOf(0) }
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(url)
            .size(Size.ORIGINAL)
            .setParameter("retry_hash", retryHash, memoryCacheKey = null)
            .build()
    )

    var layout: LayoutCoordinates? = null

    Box(
        modifier = Modifier
            .wrapContentHeight()
            .onGloballyPositioned { layout = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { /* Called when the gesture starts */ },
                    onDoubleTap = { /* Called on Double Tap */ },
                    onLongPress = {
                        (painter.state as? AsyncImagePainter.State.Success)
                            ?.let { onLongPress(it.result.drawable, position) }
                    },
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
        PageState(
            modifier = Modifier
                .height(240.dp)
                .align(Alignment.Center),
            state = painter.state,
            position = position,
            url = url,
            onRetry = { retryHash++ }
        )
    }
}

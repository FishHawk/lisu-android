package com.fishhawk.driftinglibraryandroid.ui.reader.viewer

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import coil.size.OriginalSize
import com.fishhawk.driftinglibraryandroid.PR
import com.fishhawk.driftinglibraryandroid.data.datastore.ScaleType
import com.fishhawk.driftinglibraryandroid.data.datastore.collectAsState
import com.fishhawk.driftinglibraryandroid.ui.reader.ReaderAction
import com.fishhawk.driftinglibraryandroid.ui.reader.ReaderActionHandler
import com.fishhawk.driftinglibraryandroid.ui.reader.ReaderViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
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
//                        saveImage(url, "$prefix-$position")
//                    }
//
//                    override fun onShare() {
//                        val prefix = viewModel.makeImageFilenamePrefix()
//                            ?: return toast(R.string.toast_chapter_not_loaded)
//                        lifecycleScope.shareImage(this, url, "$prefix-$position")
//                    }
//                }).show()

@OptIn(ExperimentalPagerApi::class, ExperimentalComposeUiApi::class, InternalCoroutinesApi::class)
@Composable
internal fun PagerViewer(
    state: ViewerState.Pager,
    pointer: ReaderViewModel.ReaderChapterPointer,
    isRtl: Boolean,
    onAction: ReaderActionHandler
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

    fun toLeft() = if (isRtl) toNext() else toPrev()
    fun toRight() = if (isRtl) toPrev() else toNext()

    val useVolumeKey by PR.useVolumeKey.collectAsState()
    val invertVolumeKey by PR.invertVolumeKey.collectAsState()
    val scaleType by PR.scaleType.collectAsState()

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
                state = state.state,
                itemSpacing = itemSpacing
            ) { index ->
                Page(
                    position = index.plus(1),
                    url = pointer.currChapter.images[index],
                    contentScale = when (scaleType) {
                        ScaleType.FitScreen -> ContentScale.Fit
                        ScaleType.FitWidth -> ContentScale.FillWidth
                        ScaleType.FitHeight -> ContentScale.FillHeight
                        ScaleType.OriginalSize -> ContentScale.None
                    },
                    onTap = { offset ->
                        if (viewModel.isMenuOpened.value) viewModel.isMenuOpened.value = false
                        else when {
                            offset.x < 0.25 -> toLeft()
                            offset.x > 0.75 -> toRight()
                            else -> viewModel.isMenuOpened.value = !viewModel.isMenuOpened.value
                        }
                    },
                    onLongPress = { onAction(ReaderAction.OpenPageSheet(it)) }
                )
            }
        }
    }

    DisposableEffect(Unit) {
        focusRequester.requestFocus()
        onDispose { }
    }
}

@OptIn(ExperimentalCoilApi::class, ExperimentalFoundationApi::class)
@Composable
private fun Page(
    position: Int,
    url: String,
    contentScale: ContentScale,
    onTap: ((Offset) -> Unit),
    onLongPress: ((String) -> Unit)
) {
    Box(modifier = Modifier.clipToBounds()) {
        val painter = rememberImagePainter(url) { size(OriginalSize) }

        var layout: LayoutCoordinates? = null

        var scale by remember { mutableStateOf(1f) }
        var translation by remember { mutableStateOf(Offset.Zero) }
        val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
            scale *= zoomChange
            translation += panChange.times(scale)
        }

        val scope = rememberCoroutineScope()

        Image(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { layout = it }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = translation.x,
                    translationY = translation.y
                )
                .transformable(state = transformableState)
                .pointerInput(Unit) {
//                    detectDragGestures { change, dragAmount ->
//                        val maxX = layout!!.size.width * (scale - 1) / 2f
//                        val newX = translation.x + dragAmount.x
//                        if (scale > 1 && newX <= maxX && newX >= -maxX) {
//                            change.consumeAllChanges()
//                            val maxY = layout!!.size.height * (scale - 1) / 2f
//                            val newY = (translation.y + dragAmount.y).coerceIn(-maxY, maxY)
//                            translation = Offset(newX, newY)
//                        }
//                    }
                    detectTapGestures(
                        onLongPress = {
                            if (painter.state is ImagePainter.State.Success)
                                onLongPress(url)
                        },
                        onDoubleTap = {
                            val maxScale = 2f
                            val midScale = 1.5f
                            val minScale = 1f
                            val targetScale = when {
                                scale >= maxScale -> minScale
                                scale >= midScale -> maxScale
                                scale >= minScale -> midScale
                                else -> minScale
                            }
                            scope.launch { transformableState.animateZoomBy(targetScale / scale) }
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
                },
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Fit
        )

        LaunchedEffect(transformableState.isTransformInProgress) {
            if (!transformableState.isTransformInProgress) {
                if (scale < 1f) {
                    val originScale = scale
                    val originTranslation = translation
                    AnimationState(initialValue = 0f).animateTo(
                        1f,
                        SpringSpec(stiffness = Spring.StiffnessLow)
                    ) {
                        scale = originScale + (1 - originScale) * this.value
                        translation = originTranslation * (1 - this.value)
                    }
                } else {
                    val maxX = layout!!.size.width * (scale - 1) / 2f
                    val maxY = layout!!.size.height * (scale - 1) / 2f
                    val target = Offset(
                        translation.x.coerceIn(-maxX, maxX),
                        translation.y.coerceIn(-maxY, maxY)
                    )
                    AnimationState(
                        typeConverter = Offset.VectorConverter,
                        initialValue = translation
                    ).animateTo(target, SpringSpec(stiffness = Spring.StiffnessLow)) {
                        translation = this.value
                    }
                }
            }
        }

        PageState(
            modifier = Modifier.fillMaxSize(),
            state = painter.state,
            position = position,
            url = url
        )
    }
}

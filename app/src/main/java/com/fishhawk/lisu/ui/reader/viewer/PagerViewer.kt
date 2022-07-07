package com.fishhawk.lisu.ui.reader.viewer

import android.graphics.drawable.Drawable
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateZoomBy
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.fishhawk.lisu.PR
import com.fishhawk.lisu.data.datastore.collectAsState
import com.fishhawk.lisu.ui.reader.ReaderPage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import kotlinx.coroutines.launch
import coil.size.Size as CoilSize

@OptIn(ExperimentalPagerApi::class, ExperimentalComposeUiApi::class)
@Composable
internal fun PagerViewer(
    isMenuOpened: MutableState<Boolean>,
    state: ViewerState.Pager,
    isRtl: Boolean,
    requestMoveToPrevChapter: () -> Unit,
    requestMoveToNextChapter: () -> Unit,
    onLongPress: (drawable: Drawable, position: Int) -> Unit,
) {
    val scope = rememberCoroutineScope()

    fun toNext() {
        if (state.position < state.size - 1)
            scope.launch { state.scrollToPage(state.position + 1) }
        else requestMoveToNextChapter()
    }

    fun toPrev() {
        if (state.position > 0)
            scope.launch { state.scrollToPage(state.position - 1) }
        else requestMoveToPrevChapter()
    }

    fun toLeft() = if (isRtl) toNext() else toPrev()
    fun toRight() = if (isRtl) toPrev() else toNext()

    val useVolumeKey by PR.useVolumeKey.collectAsState()
    val invertVolumeKey by PR.invertVolumeKey.collectAsState()

    val focusRequester = remember { FocusRequester() }

    val nestedScrollConnection = remember(isRtl) {
        nestedScrollConnection(
            requestMoveToPrevChapter = requestMoveToPrevChapter,
            requestMoveToNextChapter = requestMoveToNextChapter,
            isPrepareToPrev = { if (isRtl) it.x < -10 else it.x > 10 },
            isPrepareToNext = { if (isRtl) it.x > 10 else it.x < -10 },
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
                            if (useVolumeKey && !isMenuOpened.value) {
                                if (invertVolumeKey) toNext() else toPrev()
                            } else return@onPreviewKeyEvent false
                        Key.VolumeDown ->
                            if (useVolumeKey && !isMenuOpened.value) {
                                if (invertVolumeKey) toPrev() else toNext()
                            } else return@onPreviewKeyEvent false
                        else -> return@onPreviewKeyEvent false
                    }
                }
                KeyEventType.KeyUp -> {
                    when (it.key) {
                        Key.Menu -> isMenuOpened.value = !isMenuOpened.value

                        Key.N -> requestMoveToNextChapter()
                        Key.P -> requestMoveToPrevChapter()

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

        val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
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
                when (val page = state.pages[index]) {
                    is ReaderPage.Image ->
                        ImagePage(
                            page = page,
                            onTap = { offset ->
                                if (isMenuOpened.value) isMenuOpened.value = false
                                else when {
                                    offset.x < 0.25 -> toLeft()
                                    offset.x > 0.75 -> toRight()
                                    else -> isMenuOpened.value = true
                                }
                            },
                            onLongPress = onLongPress
                        )
                    ReaderPage.Empty ->
                        EmptyPage(modifier = Modifier.fillMaxSize())
                    is ReaderPage.NextChapterState ->
                        NextChapterStatePage(page = page, modifier = Modifier.fillMaxSize())
                    is ReaderPage.PrevChapterState ->
                        PrevChapterStatePage(page = page, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }

    DisposableEffect(Unit) {
        focusRequester.requestFocus()
        onDispose { }
    }
}

@Composable
private fun ImagePage(
    page: ReaderPage.Image,
    onTap: ((Offset) -> Unit),
    onLongPress: ((drawable: Drawable, position: Int) -> Unit),
) {
    Box {
        var retryHash by remember { mutableStateOf(0) }
        val painter = rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current)
                .data(page.url)
                .size(CoilSize.ORIGINAL)
                .setParameter("retry_hash", retryHash, memoryCacheKey = null)
                .build()
        )
        ZoomableImage(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            onLongPress = {
                (painter.state as? AsyncImagePainter.State.Success)
                    ?.let { onLongPress(it.result.drawable, page.index + 1) }
            },
            onTap = onTap
        )
        PageState(
            modifier = Modifier.fillMaxSize(),
            state = painter.state,
            page = page,
            onRetry = { retryHash++ }
        )
    }
}


private const val maxScale = 3.0f
private const val midScale = 1.5f
private const val minScale = 1.0f

@Composable
private fun ZoomableImage(
    painter: AsyncImagePainter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
) {
    val srcSize = painter.intrinsicSize
    var dstSize = Size.Unspecified

    val scope = rememberCoroutineScope()

    var scale by remember { mutableStateOf(1f) }
    var translation by remember { mutableStateOf(Offset.Zero) }
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(minScale * 0.5f, maxScale)
        translation += panChange.times(scale)
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .onPlaced { dstSize = it.size.toSize() }
            .transformable(state = transformableState)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = onLongPress,
                    onDoubleTap = {
                        val targetScale = when {
                            scale >= maxScale - 1e-4f -> minScale
                            scale >= midScale - 1e-4f -> maxScale
                            scale >= minScale - 1e-4f -> midScale
                            else -> minScale
                        }
                        scope.launch {
                            transformableState.animateZoomBy(
                                targetScale / scale
                            )
                        }
                    },
                    onTap = {
                        if (onTap != null) {
                            onTap(Offset(it.x / size.width, it.y / size.height))
                        }
                    }
                )
            }
//            .pointerInput(Unit) {
//                forEachGesture {
//                    awaitPointerEventScope {
//                        val down = awaitFirstDown(requireUnconsumed = false)
//                        println("test")
//                        drag(down.id) {
//                            val rect = (size.toSize() * (scale - 1))
//                                .toRect()
//                                .run { translate(center) }
//                            val targetTranslation = (it.positionChange() + translation)
//                            if (rect.contains(targetTranslation)) {
//                                translation = targetTranslation
//                                it.consumePositionChange()
//                            }
//                        }
//                    }
//                }
//            }
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = translation.x,
                    translationY = translation.y
                ),
            contentScale = ContentScale.Fit
        )
    }

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
                if (dstSize == Size.Unspecified) return@LaunchedEffect
                val maxX = dstSize.width * (scale - 1) / 2f
                val maxY = dstSize.height * (scale - 1) / 2f
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
}
